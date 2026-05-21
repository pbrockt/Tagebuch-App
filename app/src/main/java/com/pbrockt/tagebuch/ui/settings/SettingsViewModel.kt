package com.pbrockt.tagebuch.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbrockt.tagebuch.BuildConfig
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import com.pbrockt.tagebuch.data.repository.SyncRepository
import com.pbrockt.tagebuch.notifications.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    data class UpToDate(val version: String) : UpdateState()
    data class UpdateAvailable(val current: String, val latest: String) : UpdateState()
    data class Error(val msg: String) : UpdateState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: SecurePrefs,
    private val syncRepo: SyncRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _authMethod = MutableStateFlow(prefs.authMethod)
    val authMethod: StateFlow<String> = _authMethod
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
    private val _accentColor = MutableStateFlow(prefs.accentColor)
    val accentColor: StateFlow<String> = _accentColor
    private val _calendarIconMode = MutableStateFlow(prefs.calendarIconMode)
    val calendarIconMode: StateFlow<String> = _calendarIconMode
    private val _fontChoice = MutableStateFlow(prefs.fontChoice)
    val fontChoice: StateFlow<String> = _fontChoice
    private val _ownBirthday = MutableStateFlow(prefs.ownBirthdayDate)
    val ownBirthday: StateFlow<String> = _ownBirthday
    private val _reminderEnabled = MutableStateFlow(prefs.reminderEnabled)
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled
    private val _reminderHour = MutableStateFlow(prefs.reminderHour)
    val reminderHour: StateFlow<Int> = _reminderHour
    private val _reminderMinute = MutableStateFlow(prefs.reminderMinute)
    val reminderMinute: StateFlow<Int> = _reminderMinute

    val syncState = syncRepo.syncState
    val testState = syncRepo.testState

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    private val updateClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun checkForUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            _updateState.value = UpdateState.Loading
            try {
                val request = Request.Builder()
                    .url("https://api.github.com/repos/pbrockt/Tagebuch-App/releases/latest")
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()
                updateClient.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: throw Exception("Leere Antwort")
                    val tagName = JSONObject(body).getString("tag_name") // z.B. "v0.2a"
                    val current = "v${BuildConfig.VERSION_NAME}"
                    _updateState.value = if (tagName == current)
                        UpdateState.UpToDate(current)
                    else
                        UpdateState.UpdateAvailable(current, tagName)
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("Keine Verbindung")
            }
        }
    }

    fun setPin(pin: String) {
        prefs.pinHash = sha256(pin); prefs.authMethod = SecurePrefs.AUTH_PIN
        _authMethod.value = SecurePrefs.AUTH_PIN
    }
    fun clearPin() {
        prefs.pinHash = null; prefs.authMethod = SecurePrefs.AUTH_NONE
        _authMethod.value = SecurePrefs.AUTH_NONE
    }
    fun saveWebDavConfig(url: String, user: String, pass: String, encPass: String) {
        prefs.webDavUrl = url; prefs.webDavUser = user
        prefs.webDavPassword = pass; prefs.webDavEncryptionPassphrase = encPass
        _webDavUrl.value = url; _webDavUser.value = user
        _webDavPassword.value = pass; _webDavEncPass.value = encPass
    }
    fun setSyncEnabled(enabled: Boolean) { prefs.syncEnabled = enabled; _syncEnabled.value = enabled }
    fun setTheme(theme: String) { prefs.themeChoice = theme; _themeChoice.value = theme }
    fun setAccentColor(color: String) { prefs.accentColor = color; _accentColor.value = color }
    fun setCalendarIconMode(mode: String) { prefs.calendarIconMode = mode; _calendarIconMode.value = mode }
    fun setFont(font: String) { prefs.fontChoice = font; _fontChoice.value = font }
    fun setOwnBirthday(date: String) { prefs.ownBirthdayDate = date; _ownBirthday.value = date }
    fun setReminderEnabled(enabled: Boolean) {
        prefs.reminderEnabled = enabled; _reminderEnabled.value = enabled
        if (enabled) ReminderWorker.schedule(context, prefs.reminderHour, prefs.reminderMinute)
        else ReminderWorker.cancel(context)
    }
    fun saveReminderTime(hour: Int, minute: Int) {
        prefs.reminderHour = hour; prefs.reminderMinute = minute
        _reminderHour.value = hour; _reminderMinute.value = minute
        if (prefs.reminderEnabled) ReminderWorker.schedule(context, hour, minute)
    }
    fun syncNow() { viewModelScope.launch { syncRepo.triggerSync() } }
    fun testConnection(url: String, user: String, pass: String) {
        viewModelScope.launch { syncRepo.testConnection(url, user, pass) }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
