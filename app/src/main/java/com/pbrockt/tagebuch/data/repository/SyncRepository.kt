package com.pbrockt.tagebuch.data.repository

import com.pbrockt.tagebuch.data.remote.SyncManager
import com.pbrockt.tagebuch.data.remote.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository für Sync-Zustand und Verbindungstest.
 *
 * Trennt den Sync-Status von der Sync-Logik:
 * - SyncManager: Enthält die eigentliche Sync-Logik
 * - SyncRepository: Stellt den Status als StateFlow bereit und
 *   koordiniert wann Syncs ausgelöst werden
 *
 * ViewModels können den syncState beobachten und in der UI anzeigen
 * ohne die technischen Details des SyncManagers zu kennen.
 */
@Singleton
class SyncRepository @Inject constructor(private val syncManager: SyncManager) {

    /** Aktueller Sync-Status: null = noch kein Sync, Success/Error = Ergebnis */
    private val _syncState = MutableStateFlow<SyncResult?>(null)
    val syncState: StateFlow<SyncResult?> = _syncState

    /** Ergebnis des letzten Verbindungstests */
    private val _testState = MutableStateFlow<SyncResult?>(null)
    val testState: StateFlow<SyncResult?> = _testState

    /**
     * Startet einen Sync-Vorgang.
     * Setzt den State vorher auf null damit die UI weiß dass etwas läuft.
     */
    suspend fun triggerSync() {
        _syncState.value = null
        _syncState.value = syncManager.sync()
    }

    /** Testet die Verbindung zum WebDAV-Server ohne Daten zu übertragen */
    suspend fun testConnection(url: String, user: String, pass: String) {
        _testState.value = null
        _testState.value = syncManager.testConnection(url, user, pass)
    }
}
