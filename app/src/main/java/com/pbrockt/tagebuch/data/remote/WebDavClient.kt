package com.pbrockt.tagebuch.data.remote

import kotlinx.coroutines.CancellationException
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class WebDavResource(val path: String, val lastModified: Long, val size: Long)

@Singleton
class WebDavClient @Inject constructor() {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var baseUrl: String = ""
    private var credentials: String = ""

    fun configure(url: String, username: String, password: String) {
        baseUrl = url.trimEnd('/')
        credentials = if (username.isNotEmpty()) Credentials.basic(username, password) else ""
    }

    fun isConfigured(): Boolean = baseUrl.isNotEmpty()

    // Testet ob der Server erreichbar ist und WebDAV unterstützt
    fun testConnection(): Result<String> = safeCall {
        val request = Request.Builder()
            .url(baseUrl)
            .method("OPTIONS", null)
            .header("Authorization", credentials)
            .build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful || response.code == 405 || response.code == 207) {
                val dav = response.header("DAV") ?: ""
                if (dav.isEmpty()) "Verbunden (kein DAV-Header — möglicherweise kein WebDAV-Server)"
                else "Verbunden ✓ DAV: $dav"
            } else {
                throw IOException("Server antwortete mit Code ${response.code}")
            }
        }
    }

    fun listResources(path: String = "/"): Result<List<WebDavResource>> = safeCall {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .method(
                "PROPFIND",
                "<?xml version=\"1.0\"?><propfind xmlns=\"DAV:\"><prop><getlastmodified/><getcontentlength/></prop></propfind>"
                    .toRequestBody("application/xml".toMediaType())
            )
            .header("Authorization", credentials)
            .header("Depth", "1")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful && response.code != 207) {
                throw IOException("PROPFIND fehlgeschlagen: ${response.code}")
            }
            parseMultiStatus(response.body?.string() ?: "")
        }
    }

    fun putFile(path: String, data: ByteArray): Result<Unit> = safeCall {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .put(data.toRequestBody("application/octet-stream".toMediaType()))
            .header("Authorization", credentials)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful)
                throw IOException("PUT fehlgeschlagen: ${response.code}")
        }
    }

    fun getFile(path: String): Result<ByteArray> = safeCall {
        // path kann absolute URL oder relativer Pfad sein
        val url = if (path.startsWith("http")) path else "$baseUrl$path"
        val request = Request.Builder()
            .url(url)
            .get()
            .header("Authorization", credentials)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("GET fehlgeschlagen: ${response.code}")
            response.body?.bytes() ?: throw IOException("Leerer Body")
        }
    }

    fun deleteFile(path: String): Result<Unit> = safeCall {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .delete()
            .header("Authorization", credentials)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("DELETE fehlgeschlagen: ${response.code}")
        }
    }

    fun createDirectory(path: String): Result<Unit> = safeCall {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .method("MKCOL", null)
            .header("Authorization", credentials)
            .build()
        client.newCall(request).execute().use { response ->
            // 405 = bereits vorhanden, ist OK
            if (!response.isSuccessful && response.code != 405) {
                throw IOException("MKCOL fehlgeschlagen: ${response.code}")
            }
        }
    }

    // Sicherer Wrapper: fängt Exceptions, re-throws CancellationException
    private fun <T> safeCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e  // Coroutine-Abbruch nie verschlucken
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseMultiStatus(xml: String): List<WebDavResource> {
        val resources = mutableListOf<WebDavResource>()
        val hrefPattern = Regex("<[Dd]:?href[^>]*>([^<]+)</[Dd]:?href>")
        val modPattern = Regex("<[Dd]:?getlastmodified[^>]*>([^<]+)</[Dd]:?getlastmodified>")
        val sizePattern = Regex("<[Dd]:?getcontentlength[^>]*>([^<]+)</[Dd]:?getcontentlength>")

        val hrefs = hrefPattern.findAll(xml).map { it.groupValues[1].trim() }.toList()
        val mods = modPattern.findAll(xml).map { it.groupValues[1].trim() }.toList()
        val sizes = sizePattern.findAll(xml).map { it.groupValues[1].trim().toLongOrNull() ?: 0L }.toList()

        hrefs.forEachIndexed { i, href ->
            if (!href.endsWith(".enc")) return@forEachIndexed
            // Extrahiere nur den Pfad (HREF kann absolute URL oder relativer Pfad sein)
            val path = try {
                if (href.startsWith("http")) URI(href).path else href
            } catch (e: Exception) {
                href
            }
            val lastMod = mods.getOrNull(i)?.let {
                runCatching {
                    java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US)
                        .parse(it)?.time ?: 0L
                }.getOrElse { 0L }
            } ?: 0L

            resources.add(WebDavResource(path = path, lastModified = lastMod, size = sizes.getOrNull(i) ?: 0L))
        }
        return resources
    }
}
