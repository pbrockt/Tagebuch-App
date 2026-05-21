package com.pbrockt.tagebuch.data.remote

import com.pbrockt.tagebuch.data.local.crypto.PassphraseKdf
import com.pbrockt.tagebuch.data.local.dao.DiaryDao
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import com.pbrockt.tagebuch.data.model.DiaryDay
import com.pbrockt.tagebuch.data.model.DiaryPage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** Mögliche Ergebnisse einer Synchronisation */
sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
    object NotConfigured : SyncResult()  // WebDAV nicht eingerichtet
}

/**
 * Koordiniert die bidirektionale Synchronisation zwischen Gerät und WebDAV-Server.
 *
 * Sync-Strategie:
 * 1. Lokale Änderungen hochladen: Alle Seiten die seit dem letzten Sync geändert
 *    wurden werden verschlüsselt auf den Server geladen.
 * 2. Server-Änderungen herunterladen: Alle Dateien auf dem Server werden geprüft.
 *    Wenn eine Datei neuer ist als die lokale Version, wird sie heruntergeladen.
 * 3. Konflikt-Auflösung: Bei gleichzeitigen Änderungen auf Gerät und Server
 *    gewinnt immer die neuere Version (Timestamp-basiert).
 *
 * Alle Daten werden vor dem Upload verschlüsselt und nach dem Download entschlüsselt.
 * Der Server sieht ausschließlich unlesbaren Ciphertext.
 *
 * Coroutines: Diese Klasse nutzt Kotlin Coroutines für asynchrone Operationen.
 * withContext(Dispatchers.IO) führt Netzwerk- und Datenbankoperationen auf einem
 * Hintergrund-Thread aus — der UI-Thread bleibt reaktionsfähig.
 */
@Singleton
class SyncManager @Inject constructor(
    private val webDavClient: WebDavClient,
    private val dao: DiaryDao,
    private val securePrefs: SecurePrefs,
    private val kdf: PassphraseKdf  // Key Derivation Function für Verschlüsselungs-Schlüssel
) {
    private val remoteBasePath = "/tagebuch/"  // Verzeichnis auf dem WebDAV-Server
    private val json = Json { ignoreUnknownKeys = true }  // JSON-Serialisierer

    /**
     * Testet die WebDAV-Verbindung ohne Daten zu übertragen.
     *
     * @param url      WebDAV-Server URL
     * @param user     Benutzername
     * @param pass     Passwort
     */
    suspend fun testConnection(url: String, user: String, pass: String): SyncResult =
        withContext(Dispatchers.IO) {
            try {
                webDavClient.configure(url, user, pass)
                val result = webDavClient.testConnection()
                if (result.isSuccess) SyncResult.Success
                else SyncResult.Error(result.exceptionOrNull()?.message ?: "Unbekannter Fehler")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                SyncResult.Error(e.message ?: "Verbindungsfehler")
            }
        }

    /**
     * Führt die vollständige bidirektionale Synchronisation durch.
     */
    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
        // Prüfen ob Sync überhaupt aktiviert ist
        if (!securePrefs.syncEnabled) return@withContext SyncResult.NotConfigured

        val url = securePrefs.webDavUrl
        val encPass = securePrefs.webDavEncryptionPassphrase
        if (url.isEmpty()) return@withContext SyncResult.NotConfigured
        if (encPass.isEmpty()) return@withContext SyncResult.Error("Verschlüsselungs-Passphrase fehlt")

        try {
            webDavClient.configure(url, securePrefs.webDavUser, securePrefs.webDavPassword)

            // Aus der Passphrase den 256-Bit Verschlüsselungsschlüssel ableiten
            val encKey = kdf.deriveKey(encPass)

            // Sicherstellen dass das Verzeichnis auf dem Server existiert
            webDavClient.createDirectory(remoteBasePath)

            // ===== Phase 1: Lokale Änderungen hochladen =====
            val unsyncedPages = dao.getUnsyncedPages()
            for (page in unsyncedPages) {
                val pageJson = json.encodeToString(page)        // DiaryPage → JSON-String
                val encrypted = kdf.encryptData(pageJson.toByteArray(), encKey)  // Verschlüsseln
                val remotePath = "$remoteBasePath${page.id}.enc"  // Dateiname = Page-ID + .enc
                val putResult = webDavClient.putFile(remotePath, encrypted)
                if (putResult.isSuccess) {
                    dao.markPageSynced(page.id, System.currentTimeMillis())
                }
                // Einzelne Upload-Fehler stoppen nicht den gesamten Sync
            }

            // ===== Phase 2: Server-Änderungen herunterladen =====
            val remoteResources = webDavClient.listResources(remoteBasePath)
            if (remoteResources.isFailure) {
                return@withContext SyncResult.Error(
                    "Verzeichnis-Listing fehlgeschlagen: ${remoteResources.exceptionOrNull()?.message}"
                )
            }

            for (resource in remoteResources.getOrDefault(emptyList())) {
                val pageId = resource.path.substringAfterLast("/").removeSuffix(".enc")
                if (pageId.isBlank()) continue

                val localPage = try { dao.getPageById(pageId) } catch (e: Exception) { null }

                // Download nötig wenn: lokal nicht vorhanden ODER Server-Version neuer
                val needsDownload = localPage == null ||
                        (localPage.syncedAt != null && resource.lastModified > localPage.syncedAt!!)
                if (!needsDownload) continue

                val dataResult = webDavClient.getFile(resource.path)
                if (dataResult.isFailure) continue

                try {
                    val decrypted = kdf.decryptData(dataResult.getOrThrow(), encKey)
                    val remotePage = json.decodeFromString<DiaryPage>(String(decrypted))
                    // Server-Version übernehmen wenn sie neuer ist
                    if (localPage == null || remotePage.updatedAt > localPage.updatedAt) {
                        dao.upsertDay(DiaryDay(date = remotePage.dayDate))
                        dao.upsertPage(remotePage.copy(syncedAt = System.currentTimeMillis()))
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    continue  // Fehlerhafte Datei überspringen, Rest weiter verarbeiten
                }
            }

            SyncResult.Success

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Sync fehlgeschlagen")
        }
    }
}
