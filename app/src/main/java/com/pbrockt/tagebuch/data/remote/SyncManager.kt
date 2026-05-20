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

sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
    object NotConfigured : SyncResult()
}

@Singleton
class SyncManager @Inject constructor(
    private val webDavClient: WebDavClient,
    private val dao: DiaryDao,
    private val securePrefs: SecurePrefs,
    private val kdf: PassphraseKdf
) {
    private val remoteBasePath = "/tagebuch/"
    private val json = Json { ignoreUnknownKeys = true }

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

    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
        if (!securePrefs.syncEnabled) return@withContext SyncResult.NotConfigured

        val url = securePrefs.webDavUrl
        val user = securePrefs.webDavUser
        val pass = securePrefs.webDavPassword
        val encPass = securePrefs.webDavEncryptionPassphrase

        if (url.isEmpty()) return@withContext SyncResult.NotConfigured
        if (encPass.isEmpty()) return@withContext SyncResult.Error("Verschlüsselungs-Passphrase fehlt")

        try {
            webDavClient.configure(url, user, pass)
            val encKey = kdf.deriveKey(encPass)

            // Verzeichnis erstellen (ignoriert 405 = already exists)
            webDavClient.createDirectory(remoteBasePath)

            // Lokale ungesyncte Seiten hochladen
            val unsyncedPages = dao.getUnsyncedPages()
            for (page in unsyncedPages) {
                val pageJson = json.encodeToString(page)
                val encrypted = kdf.encryptData(pageJson.toByteArray(), encKey)
                val remotePath = "$remoteBasePath${page.id}.enc"
                val putResult = webDavClient.putFile(remotePath, encrypted)
                if (putResult.isSuccess) {
                    dao.markPageSynced(page.id, System.currentTimeMillis())
                }
                // Einzelner Fehler stoppt nicht den ganzen Sync
            }

            // Remote-Seiten holen die lokal fehlen oder neuer sind
            val remoteResources = webDavClient.listResources(remoteBasePath)
            if (remoteResources.isFailure) {
                return@withContext SyncResult.Error(
                    "PROPFIND fehlgeschlagen: ${remoteResources.exceptionOrNull()?.message}"
                )
            }

            for (resource in remoteResources.getOrDefault(emptyList())) {
                val pageId = resource.path.substringAfterLast("/").removeSuffix(".enc")
                if (pageId.isBlank()) continue

                val localPage = try { dao.getPageById(pageId) } catch (e: Exception) { null }
                val needsDownload = localPage == null ||
                        (localPage.syncedAt != null && resource.lastModified > localPage.syncedAt!!)

                if (!needsDownload) continue

                val dataResult = webDavClient.getFile(resource.path)
                if (dataResult.isFailure) continue

                try {
                    val decrypted = kdf.decryptData(dataResult.getOrThrow(), encKey)
                    val remotePage = json.decodeFromString<DiaryPage>(String(decrypted))
                    if (localPage == null || remotePage.updatedAt > localPage.updatedAt) {
                        dao.upsertDay(DiaryDay(date = remotePage.dayDate))
                        dao.upsertPage(remotePage.copy(syncedAt = System.currentTimeMillis()))
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Einzelne fehlerhafte Datei überspringen
                    continue
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
