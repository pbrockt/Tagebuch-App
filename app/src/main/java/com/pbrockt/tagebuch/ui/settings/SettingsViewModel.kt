package com.pbrockt.tagebuch.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import com.pbrockt.tagebuch.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: SecurePrefs,
    private val syncRepo: SyncRepository
) : ViewModel() {

    private val _authMethod = MutableStateFlow(prefs.authMethod)
    val authMethod: StateFlow<String> = _authMethod

    private val _biometricEnabled = MutableStateFlow(prefs.biometricEnabled)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled

    private val _webDavUrl = MutableStateFlow(prefs.webDavUrl)
    val webDavUrl: StateFlow<String> = _webDavUrl

    private val _webDavUser = MutableStateFlow(prefs.webDavUser)
    val webDavUser: StateFlow<String> = _webDavUser

    private val _webDavPassword = MutableStateFlow(prefs.webDavPassword)
    val webDavPassword: StateFlow<String> = _webDavPassword

    private val _webDavEncPass = MutableStateFlow(prefs.webDavEncryptionPassphrase)
    val webDavEncPass: StateFlow<String> = _webDavEncPass

    private val _syncEnabled = MutableStateFlow(prefs.syncEnabled)
    val syncEnabled: StateFlow<Boolean> = _syncEnabled

    private val _themeChoice = MutableStateFlow(prefs.themeChoice)
    val themeChoice: StateFlow<String> = _themeChoice

    val syncState = syncRepo.syncState

    fun setPin(pin: String) {
        val hash = sha256(pin)
        prefs.pinHash = hash
        prefs.authMethod = SecurePrefs.AUTH_PIN
        _authMethod.value = SecurePrefs.AUTH_PIN
    }

    fun clearPin() {
        prefs.pinHash = null
        prefs.authMethod = SecurePrefs.AUTH_NONE
        _authMethod.value = SecurePrefs.AUTH_NONE
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.biometricEnabled = enabled
        if (enabled) prefs.authMethod = SecurePrefs.AUTH_BIOMETRIC
        _biometricEnabled.value = enabled
        _authMethod.value = prefs.authMethod
    }

    fun saveWebDavConfig(url: String, user: String, pass: String, encPass: String) {
        prefs.webDavUrl = url
        prefs.webDavUser = user
        prefs.webDavPassword = pass
        prefs.webDavEncryptionPassphrase = encPass
        _webDavUrl.value = url
        _webDavUser.value = user
        _webDavPassword.value = pass
        _webDavEncPass.value = encPass
    }

    fun setSyncEnabled(enabled: Boolean) {
        prefs.syncEnabled = enabled
        _syncEnabled.value = enabled
    }

    fun setTheme(theme: String) {
        prefs.themeChoice = theme
        _themeChoice.value = theme
    }

    fun syncNow() {
        viewModelScope.launch { syncRepo.triggerSync() }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
