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

/** Repräsentiert eine Datei auf dem WebDAV-Server */
data class WebDavResource(
    val path: String,           // Pfad auf dem Server
    val lastModified: Long,     // Zeitstempel der letzten Änderung
    val size: Long              // Dateigröße in Bytes
)

/**
 * HTTP-Client für WebDAV-Operationen.
 *
 * WebDAV (Web Distributed Authoring and Versioning) ist eine Erweiterung von HTTP
 * die Datei-Operationen ermöglicht. Viele Cloud-Speicher unterstützen WebDAV:
 * Nextcloud, ownCloud, IONOS HiDrive, etc.
 *
 * Verwendete HTTP-Methoden:
 * - PROPFIND: Listet Verzeichnisinhalte auf (wie "ls" auf der Kommandozeile)
 * - PUT:      Lädt eine Datei hoch
 * - GET:      Lädt eine Datei herunter
 * - DELETE:   Löscht eine Datei
 * - MKCOL:    Erstellt ein Verzeichnis
 *
 * OkHttp ist eine bewährte HTTP-Bibliothek für Android/Java von Square.
 */
@Singleton
class WebDavClient @Inject constructor() {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var baseUrl: String = ""      // Server-URL ohne abschließenden Slash
    private var credentials: String = ""  // HTTP Basic Auth Header-Wert

    /**
     * Konfiguriert den Client mit Server-URL und Zugangsdaten.
     * Muss vor allen anderen Methoden aufgerufen werden.
     */
    fun configure(url: String, username: String, password: String) {
        baseUrl = url.trimEnd('/')
        credentials = if (username.isNotEmpty()) Credentials.basic(username, password) else ""
    }

    fun isConfigured(): Boolean = baseUrl.isNotEmpty()

    /**
     * Testet ob der Server erreichbar ist und WebDAV unterstützt.
     * OPTIONS ist eine harmlose HTTP-Methode die keine Daten liest/schreibt.
     *
     * @return Success mit Serverinfo oder Failure mit Fehlermeldung
     */
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

    /**
     * Listet alle Dateien in einem Verzeichnis auf (PROPFIND).
     *
     * PROPFIND mit Depth:1 gibt das Verzeichnis selbst und seinen direkten
     * Inhalt zurück (keine Unterverzeichnisse).
     */
    fun listResources(path: String = "/"): Result<List<WebDavResource>> = safeCall {
        val requestBody = """<?xml version="1.0"?><propfind xmlns="DAV:">
            <prop><getlastmodified/><getcontentlength/></prop></propfind>"""
        val request = Request.Builder()
            .url("$baseUrl$path")
            .method("PROPFIND", requestBody.toRequestBody("application/xml".toMediaType()))
            .header("Authorization", credentials)
            .header("Depth", "1")  // Nur eine Ebene tief
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful && response.code != 207) {
                throw IOException("PROPFIND fehlgeschlagen: ${response.code}")
            }
            parseMultiStatus(response.body?.string() ?: "")
        }
    }

    /** Lädt eine Datei auf den Server hoch */
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

    /** Lädt eine Datei vom Server herunter */
    fun getFile(path: String): Result<ByteArray> = safeCall {
        // Kann sowohl relative Pfade als auch absolute URLs verarbeiten
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

    /** Erstellt ein Verzeichnis auf dem Server (MKCOL = Make Collection) */
    fun createDirectory(path: String): Result<Unit> = safeCall {
        val request = Request.Builder()
            .url("$baseUrl$path")
            .method("MKCOL", null)
            .header("Authorization", credentials)
            .build()
        client.newCall(request).execute().use { response ->
            // 405 = Verzeichnis existiert bereits — kein Fehler
            if (!response.isSuccessful && response.code != 405) {
                throw IOException("MKCOL fehlgeschlagen: ${response.code}")
            }
        }
    }

    /**
     * Sicherer Wrapper für alle HTTP-Operationen.
     *
     * Wichtig: CancellationException darf NIEMALS gefangen werden!
     * Sie signalisiert dass die Coroutine abgebrochen wurde.
     * Würden wir sie verschlucken, würde die Coroutine nie aufhören zu laufen.
     */
    private fun <T> safeCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e  // Immer weiterwerfen!
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parst die XML-Antwort eines PROPFIND-Requests (Multi-Status 207).
     *
     * Die Antwort enthält für jede Datei: Pfad, Änderungsdatum, Größe.
     * Wir filtern auf .enc Dateien — das sind unsere verschlüsselten Einträge.
     */
    private fun parseMultiStatus(xml: String): List<WebDavResource> {
        val resources = mutableListOf<WebDavResource>()
        val hrefPattern = Regex("<[Dd]:?href[^>]*>([^<]+)</[Dd]:?href>")
        val modPattern = Regex("<[Dd]:?getlastmodified[^>]*>([^<]+)</[Dd]:?getlastmodified>")
        val sizePattern = Regex("<[Dd]:?getcontentlength[^>]*>([^<]+)</[Dd]:?getcontentlength>")

        val hrefs = hrefPattern.findAll(xml).map { it.groupValues[1].trim() }.toList()
        val mods = modPattern.findAll(xml).map { it.groupValues[1].trim() }.toList()
        val sizes = sizePattern.findAll(xml).map { it.groupValues[1].trim().toLongOrNull() ?: 0L }.toList()

        hrefs.forEachIndexed { i, href ->
            if (!href.endsWith(".enc")) return@forEachIndexed  // Nur verschlüsselte Dateien

            // PROPFIND kann absolute URLs oder relative Pfade zurückgeben — beides verarbeiten
            val path = try {
                if (href.startsWith("http")) URI(href).path else href
            } catch (e: Exception) { href }

            val lastMod = mods.getOrNull(i)?.let {
                runCatching {
                    java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US)
                        .parse(it)?.time ?: 0L
                }.getOrElse { 0L }
            } ?: 0L

            resources.add(WebDavResource(path, lastMod, sizes.getOrNull(i) ?: 0L))
        }
        return resources
    }
}
