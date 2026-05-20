package com.pbrockt.tagebuch.data.repository

import com.pbrockt.tagebuch.data.remote.SyncManager
import com.pbrockt.tagebuch.data.remote.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(private val syncManager: SyncManager) {

    private val _syncState = MutableStateFlow<SyncResult?>(null)
    val syncState: StateFlow<SyncResult?> = _syncState

    private val _testState = MutableStateFlow<SyncResult?>(null)
    val testState: StateFlow<SyncResult?> = _testState

    suspend fun triggerSync() {
        _syncState.value = null
        _syncState.value = syncManager.sync()
    }

    suspend fun testConnection(url: String, user: String, pass: String) {
        _testState.value = null
        _testState.value = syncManager.testConnection(url, user, pass)
    }
}
