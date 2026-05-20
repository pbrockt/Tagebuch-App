package com.pbrockt.tagebuch.data.remote

import com.pbrockt.tagebuch.data.local.crypto.PassphraseKdf
import com.pbrockt.tagebuch.data.local.dao.DiaryDao
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import com.pbrockt.tagebuch.data.model.DiaryPage
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

    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
        if (!securePrefs.syncEnabled) return@withContext SyncResult.NotConfigured

        val url = securePrefs.webDavUrl
        val user = securePrefs.webDavUser
        val pass = securePrefs.webDavPassword
        val encPass = securePrefs.webDavEncryptionPassphrase

        if (url.isEmpty() || encPass.isEmpty()) return@withContext SyncResult.NotConfigured

        webDavClient.configure(url, user, pass)
        val encKey = kdf.deriveKey(encPass)

        // Ensure remote directory exists
        webDavClient.createDirectory(remoteBasePath)

        // Upload unsynced pages
        val unsyncedPages = dao.getUnsyncedPages()
        for (page in unsyncedPages) {
            val json = Json.encodeToString(page)
            val encrypted = kdf.encryptData(json.toByteArray(), encKey)
            val remotePath = "$remoteBasePath${page.id}.enc"
            val result = webDavClient.putFile(remotePath, encrypted)
            if (result.isSuccess) {
                dao.markPageSynced(page.id, System.currentTimeMillis())
            }
        }

        // Download remote pages not present locally
        val remoteResources = webDavClient.listResources(remoteBasePath)
        if (remoteResources.isFailure) {
            return@withContext SyncResult.Error(remoteResources.exceptionOrNull()?.message ?: "PROPFIND failed")
        }

        for (resource in remoteResources.getOrDefault(emptyList())) {
            val pageId = resource.path.substringAfterLast("/").removeSuffix(".enc")
            val localPage = dao.getPageById(pageId)

            val needsDownload = localPage == null ||
                    (localPage.syncedAt != null && resource.lastModified > localPage.syncedAt)

            if (needsDownload) {
                val dataResult = webDavClient.getFile(resource.path)
                if (dataResult.isSuccess) {
                    runCatching {
                        val decrypted = kdf.decryptData(dataResult.getOrThrow(), encKey)
                        val remotePage = Json.decodeFromString<DiaryPage>(String(decrypted))
                        // Server wins if newer
                        if (localPage == null || remotePage.updatedAt > (localPage.updatedAt)) {
                            dao.upsertDay(
                                com.pbrockt.tagebuch.data.model.DiaryDay(date = remotePage.dayDate)
                            )
                            dao.upsertPage(remotePage.copy(syncedAt = System.currentTimeMillis()))
                        }
                    }
                }
            }
        }

        SyncResult.Success
    }
}
