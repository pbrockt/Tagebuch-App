package com.pbrockt.tagebuch

import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockManager @Inject constructor(private val prefs: SecurePrefs) {

    // Start locked if auth is configured
    private val _isLocked = MutableStateFlow(prefs.authMethod != SecurePrefs.AUTH_NONE)
    val isLocked: StateFlow<Boolean> = _isLocked

    val requiresAuth: Boolean
        get() = prefs.authMethod != SecurePrefs.AUTH_NONE

    fun lockApp() {
        if (requiresAuth) _isLocked.value = true
    }

    fun unlockApp() {
        _isLocked.value = false
    }
}
