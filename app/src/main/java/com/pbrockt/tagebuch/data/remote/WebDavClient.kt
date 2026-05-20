package com.pbrockt.tagebuch.data.remote

import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class WebDavResource(val path: String, val lastModified: Long, val size: Long)

@Singleton
class WebDavClient @Inject constructor() {

    private var client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private var baseUrl: String = ""
    private var credentials: String = ""

    fun configure(url: String, username: String, password: String) {
        baseUrl = url.trimEnd('/')
        credentials = Credentials.basic(username, password)
    }

    fun isConfigured(): Boolean = baseUrl.isNotEmpty()

    fun listResources(path: String = "/"): Result<List<WebDavResource>> = runCatching {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .method("PROPFIND", "<?xml version=\"1.0\"?><propfind xmlns=\"DAV:\"><prop><getlastmodified/><getcontentlength/></prop></propfind>".toRequestBody("application/xml".toMediaType()))
            .header("Authorization", credentials)
            .header("Depth", "1")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful && response.code != 207) {
            throw IOException("PROPFIND failed: ${response.code}")
        }
        val body = response.body?.string() ?: ""
        parseMultiStatus(body)
    }

    fun putFile(path: String, data: ByteArray): Result<Unit> = runCatching {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .put(data.toRequestBody("application/octet-stream".toMediaType()))
            .header("Authorization", credentials)
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("PUT failed: ${response.code}")
    }

    fun getFile(path: String): Result<ByteArray> = runCatching {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .get()
            .header("Authorization", credentials)
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("GET failed: ${response.code}")
        response.body?.bytes() ?: throw IOException("Empty body")
    }

    fun deleteFile(path: String): Result<Unit> = runCatching {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .delete()
            .header("Authorization", credentials)
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("DELETE failed: ${response.code}")
    }

    fun createDirectory(path: String): Result<Unit> = runCatching {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .method("MKCOL", null)
            .header("Authorization", credentials)
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful && response.code != 405) {
            throw IOException("MKCOL failed: ${response.code}")
        }
    }

    private fun parseMultiStatus(xml: String): List<WebDavResource> {
        val resources = mutableListOf<WebDavResource>()
        val hrefPattern = Regex("<[Dd]:href>([^<]+)</[Dd]:href>")
        val modifiedPattern = Regex("<[Dd]:getlastmodified>([^<]+)</[Dd]:getlastmodified>")
        val sizePattern = Regex("<[Dd]:getcontentlength>([^<]+)</[Dd]:getcontentlength>")

        val hrefs = hrefPattern.findAll(xml).map { it.groupValues[1] }.toList()
        val modified = modifiedPattern.findAll(xml).map { it.groupValues[1] }.toList()
        val sizes = sizePattern.findAll(xml).map { it.groupValues[1].toLongOrNull() ?: 0L }.toList()

        hrefs.forEachIndexed { index, href ->
            if (href.endsWith(".enc")) {
                resources.add(
                    WebDavResource(
                        path = href,
                        lastModified = modified.getOrNull(index)?.let {
                            runCatching { java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US).parse(it)?.time }.getOrNull() ?: 0L
                        } ?: 0L,
                        size = sizes.getOrNull(index) ?: 0L
                    )
                )
            }
        }
        return resources
    }
}
