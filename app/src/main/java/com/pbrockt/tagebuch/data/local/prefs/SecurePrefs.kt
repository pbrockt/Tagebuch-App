package com.pbrockt.tagebuch.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePrefs @Inject constructor(@ApplicationContext context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context, "tagebuch_secure_prefs", masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var pinHash: String?
        get() = prefs.getString(KEY_PIN_HASH, null)
        set(value) = prefs.edit().putString(KEY_PIN_HASH, value).apply()

    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC, value).apply()

    var authMethod: String
        get() = prefs.getString(KEY_AUTH_METHOD, AUTH_NONE) ?: AUTH_NONE
        set(value) = prefs.edit().putString(KEY_AUTH_METHOD, value).apply()

    var webDavUrl: String
        get() = prefs.getString(KEY_WEBDAV_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEBDAV_URL, value).apply()

    var webDavUser: String
        get() = prefs.getString(KEY_WEBDAV_USER, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEBDAV_USER, value).apply()

    var webDavPassword: String
        get() = prefs.getString(KEY_WEBDAV_PASS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEBDAV_PASS, value).apply()

    var webDavEncryptionPassphrase: String
        get() = prefs.getString(KEY_WEBDAV_ENC_PASS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEBDAV_ENC_PASS, value).apply()

    var syncEnabled: Boolean
        get() = prefs.getBoolean(KEY_SYNC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SYNC_ENABLED, value).apply()

    var themeChoice: String
        get() = prefs.getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var accentColor: String
        get() = prefs.getString(KEY_ACCENT_COLOR, ACCENT_PURPLE) ?: ACCENT_PURPLE
        set(value) = prefs.edit().putString(KEY_ACCENT_COLOR, value).apply()

    var reminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDER_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_REMINDER_ENABLED, value).apply()

    var reminderHour: Int
        get() = prefs.getInt(KEY_REMINDER_HOUR, 21)
        set(value) = prefs.edit().putInt(KEY_REMINDER_HOUR, value).apply()

    var reminderMinute: Int
        get() = prefs.getInt(KEY_REMINDER_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_REMINDER_MINUTE, value).apply()

    var calendarIconMode: String
        get() = prefs.getString(KEY_CALENDAR_ICON_MODE, CALENDAR_MODE_MOOD) ?: CALENDAR_MODE_MOOD
        set(value) = prefs.edit().putString(KEY_CALENDAR_ICON_MODE, value).apply()

    var fontChoice: String
        get() = prefs.getString(KEY_FONT_CHOICE, FONT_DEFAULT) ?: FONT_DEFAULT
        set(value) = prefs.edit().putString(KEY_FONT_CHOICE, value).apply()

    var ownBirthdayDate: String
        get() = prefs.getString(KEY_OWN_BIRTHDAY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_OWN_BIRTHDAY, value).apply()

    var contactsBirthdaysEnabled: Boolean
        get() = prefs.getBoolean(KEY_CONTACTS_BIRTHDAYS, false)
        set(value) = prefs.edit().putBoolean(KEY_CONTACTS_BIRTHDAYS, value).apply()

    // Sperr-Verzögerung in Sekunden: 0 = sofort, 30, 60, 300
    var lockTimeoutSeconds: Int
        get() = prefs.getInt(KEY_LOCK_TIMEOUT, 0)
        set(value) = prefs.edit().putInt(KEY_LOCK_TIMEOUT, value).apply()

    var periodTrackingEnabled: Boolean
        get() = prefs.getBoolean(KEY_PERIOD_TRACKING, false)
        set(value) = prefs.edit().putBoolean(KEY_PERIOD_TRACKING, value).apply()

    // Komma-getrennte Liste bereits gezeigter Streak-Meilensteine, z.B. "7,30"
    var shownStreakMilestones: String
        get() = prefs.getString(KEY_STREAK_MILESTONES, "") ?: ""
        set(value) = prefs.edit().putString(KEY_STREAK_MILESTONES, value).apply()

    companion object {
        const val KEY_PIN_HASH = "pin_hash"
        const val KEY_BIOMETRIC = "biometric_enabled"
        const val KEY_AUTH_METHOD = "auth_method"
        const val KEY_WEBDAV_URL = "webdav_url"
        const val KEY_WEBDAV_USER = "webdav_user"
        const val KEY_WEBDAV_PASS = "webdav_pass"
        const val KEY_WEBDAV_ENC_PASS = "webdav_enc_pass"
        const val KEY_SYNC_ENABLED = "sync_enabled"
        const val KEY_THEME = "theme"
        const val KEY_ACCENT_COLOR = "accent_color"
        const val KEY_REMINDER_ENABLED = "reminder_enabled"
        const val KEY_REMINDER_HOUR = "reminder_hour"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
        const val KEY_CALENDAR_ICON_MODE = "calendar_icon_mode"
        const val KEY_FONT_CHOICE = "font_choice"
        const val KEY_OWN_BIRTHDAY = "own_birthday_date"
        const val KEY_CONTACTS_BIRTHDAYS = "contacts_birthdays_enabled"
        const val KEY_LOCK_TIMEOUT = "lock_timeout_seconds"
        const val KEY_PERIOD_TRACKING = "period_tracking_enabled"
        const val KEY_STREAK_MILESTONES = "shown_streak_milestones"

        const val AUTH_NONE = "none"
        const val AUTH_PIN = "pin"
        const val AUTH_BIOMETRIC = "biometric"

        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        const val ACCENT_PURPLE = "purple"
        const val ACCENT_BLUE = "blue"
        const val ACCENT_GREEN = "green"
        const val ACCENT_ORANGE = "orange"
        const val ACCENT_PINK = "pink"
        const val ACCENT_TEAL = "teal"

        const val CALENDAR_MODE_MOOD = "mood"
        const val CALENDAR_MODE_WEATHER = "weather"
        const val CALENDAR_MODE_BOTH = "both"

        const val FONT_DEFAULT = "default"
        const val FONT_SERIF = "serif"
        const val FONT_MONO = "mono"
        const val FONT_CURSIVE = "cursive"
    }
}
