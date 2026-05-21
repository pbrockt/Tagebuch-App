package com.pbrockt.tagebuch

import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockManager @Inject constructor(private val prefs: SecurePrefs) {

    private val _isLocked = MutableStateFlow(prefs.authMethod != SecurePrefs.AUTH_NONE)
    val isLocked: StateFlow<Boolean> = _isLocked

    // Zeitstempel wann die App pausiert wurde
    private var pausedAt: Long = 0L

    val requiresAuth: Boolean
        get() = prefs.authMethod != SecurePrefs.AUTH_NONE

    /** Aufrufen in onPause() und bei SCREEN_OFF */
    fun onAppPaused() {
        if (requiresAuth) pausedAt = System.currentTimeMillis()
    }

    /** Aufrufen in onResume() — sperrt falls Timeout überschritten */
    fun onAppResumed() {
        if (!requiresAuth) return
        if (pausedAt == 0L) return
        val elapsed = System.currentTimeMillis() - pausedAt
        val timeoutMs = prefs.lockTimeoutSeconds * 1000L
        if (elapsed >= timeoutMs) {
            _isLocked.value = true
        }
        pausedAt = 0L
    }

    /** Sofort sperren (Screen-Off, App-Wechsel ohne Timeout) */
    fun lockApp() {
        if (requiresAuth) _isLocked.value = true
    }

    fun unlockApp() {
        _isLocked.value = false
        pausedAt = 0L
    }
}
