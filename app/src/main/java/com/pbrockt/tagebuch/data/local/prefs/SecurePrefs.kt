package com.pbrockt.tagebuch.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Verschlüsselte Einstellungsspeicherung für sensible App-Daten.
 *
 * Normale SharedPreferences speichern Daten als Klartext in einer XML-Datei
 * im App-Verzeichnis. EncryptedSharedPreferences verschlüsselt sowohl
 * die Schlüssel als auch die Werte mit AES-256-GCM.
 *
 * Der MasterKey ist im Android Keystore gespeichert — einem hardwaregesicherten
 * Speicher der auf modernen Geräten nicht extrahiert werden kann.
 *
 * Was wird hier gespeichert?
 * - PIN-Hash (nie den PIN selbst!), Authentifizierungs-Methode
 * - WebDAV-Zugangsdaten und Verschlüsselungs-Passphrase
 * - App-Design-Einstellungen (Theme, Farbe, Schrift)
 * - Reminder-Einstellungen
 * - Geburtstagsdaten
 */
@Singleton
class SecurePrefs @Inject constructor(@ApplicationContext context: Context) {

    // MasterKey: Der übergeordnete Schlüssel für EncryptedSharedPreferences
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM) // 256-bit AES im GCM-Modus
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "tagebuch_secure_prefs",           // Dateiname (verschlüsselt gespeichert)
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,  // Schlüssel-Verschlüsselung
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM  // Wert-Verschlüsselung
    )

    // ===== Authentifizierung =====

    /**
     * SHA-256 Hash des PINs — nie der PIN selbst.
     * SHA-256 ist eine Einwegfunktion: aus dem Hash kann man den PIN nicht zurückrechnen.
     */
    var pinHash: String?
        get() = prefs.getString(KEY_PIN_HASH, null)
        set(value) = prefs.edit().putString(KEY_PIN_HASH, value).apply()

    /**
     * Welche Authentifizierungsmethode aktiv ist.
     * Mögliche Werte: AUTH_NONE, AUTH_PIN
     */
    var authMethod: String
        get() = prefs.getString(KEY_AUTH_METHOD, AUTH_NONE) ?: AUTH_NONE
        set(value) = prefs.edit().putString(KEY_AUTH_METHOD, value).apply()

    // ===== WebDAV-Konfiguration =====

    var webDavUrl: String
        get() = prefs.getString(KEY_WEBDAV_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEBDAV_URL, value).apply()

    var webDavUser: String
        get() = prefs.getString(KEY_WEBDAV_USER, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEBDAV_USER, value).apply()

    var webDavPassword: String
        get() = prefs.getString(KEY_WEBDAV_PASS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEBDAV_PASS, value).apply()

    /**
     * Die Passphrase für die clientseitige Verschlüsselung der WebDAV-Daten.
     * Aus dieser Passphrase wird via PBKDF2 der eigentliche Verschlüsselungsschlüssel abgeleitet.
     */
    var webDavEncryptionPassphrase: String
        get() = prefs.getString(KEY_WEBDAV_ENC_PASS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_WEBDAV_ENC_PASS, value).apply()

    var syncEnabled: Boolean
        get() = prefs.getBoolean(KEY_SYNC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SYNC_ENABLED, value).apply()

    // ===== Design-Einstellungen =====

    /** "system", "light" oder "dark" — welches Farbschema verwendet wird */
    var themeChoice: String
        get() = prefs.getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    /** Welche Akzentfarbe aktiv ist: "purple", "blue", "green", etc. */
    var accentColor: String
        get() = prefs.getString(KEY_ACCENT_COLOR, ACCENT_PURPLE) ?: ACCENT_PURPLE
        set(value) = prefs.edit().putString(KEY_ACCENT_COLOR, value).apply()

    /** Schriftart: "default", "serif", "mono" oder "cursive" */
    var fontChoice: String
        get() = prefs.getString(KEY_FONT_CHOICE, FONT_DEFAULT) ?: FONT_DEFAULT
        set(value) = prefs.edit().putString(KEY_FONT_CHOICE, value).apply()

    /** Was im Kalender angezeigt wird: "mood", "weather" oder "both" */
    var calendarIconMode: String
        get() = prefs.getString(KEY_CALENDAR_ICON_MODE, CALENDAR_MODE_MOOD) ?: CALENDAR_MODE_MOOD
        set(value) = prefs.edit().putString(KEY_CALENDAR_ICON_MODE, value).apply()

    // ===== Erinnerungen =====

    var reminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDER_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_REMINDER_ENABLED, value).apply()

    var reminderHour: Int
        get() = prefs.getInt(KEY_REMINDER_HOUR, 21) // Standard: 21:00 Uhr
        set(value) = prefs.edit().putInt(KEY_REMINDER_HOUR, value).apply()

    var reminderMinute: Int
        get() = prefs.getInt(KEY_REMINDER_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_REMINDER_MINUTE, value).apply()

    // ===== Geburtstage =====

    /** Eigener Geburtstag im Format "TT.MM", z.B. "15.03". Leer = nicht gesetzt */
    var ownBirthdayDate: String
        get() = prefs.getString(KEY_OWN_BIRTHDAY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_OWN_BIRTHDAY, value).apply()

    var contactsBirthdaysEnabled: Boolean
        get() = prefs.getBoolean(KEY_CONTACTS_BIRTHDAYS, false)
        set(value) = prefs.edit().putBoolean(KEY_CONTACTS_BIRTHDAYS, value).apply()

    // ===== Weitere Einstellungen =====

    var periodTrackingEnabled: Boolean
        get() = prefs.getBoolean(KEY_PERIOD_TRACKING, false)
        set(value) = prefs.edit().putBoolean(KEY_PERIOD_TRACKING, value).apply()

    /**
     * Komma-getrennte Liste der Streak-Meilensteine die bereits angezeigt wurden.
     * Beispiel: "7,30" bedeutet: 7-Tage- und 30-Tage-Belohnung wurden gezeigt.
     * So erscheint jede Belohnung nur einmal.
     */
    var shownStreakMilestones: String
        get() = prefs.getString(KEY_STREAK_MILESTONES, "") ?: ""
        set(value) = prefs.edit().putString(KEY_STREAK_MILESTONES, value).apply()

    // ===== Konstanten =====

    companion object {
        // Schlüssel-Namen für SharedPreferences (intern)
        const val KEY_PIN_HASH = "pin_hash"
        const val KEY_AUTH_METHOD = "auth_method"
        const val KEY_WEBDAV_URL = "webdav_url"
        const val KEY_WEBDAV_USER = "webdav_user"
        const val KEY_WEBDAV_PASS = "webdav_pass"
        const val KEY_WEBDAV_ENC_PASS = "webdav_enc_pass"
        const val KEY_SYNC_ENABLED = "sync_enabled"
        const val KEY_THEME = "theme"
        const val KEY_ACCENT_COLOR = "accent_color"
        const val KEY_FONT_CHOICE = "font_choice"
        const val KEY_CALENDAR_ICON_MODE = "calendar_icon_mode"
        const val KEY_REMINDER_ENABLED = "reminder_enabled"
        const val KEY_REMINDER_HOUR = "reminder_hour"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
        const val KEY_OWN_BIRTHDAY = "own_birthday_date"
        const val KEY_CONTACTS_BIRTHDAYS = "contacts_birthdays_enabled"
        const val KEY_PERIOD_TRACKING = "period_tracking_enabled"
        const val KEY_STREAK_MILESTONES = "shown_streak_milestones"

        // Authentifizierungs-Methoden
        const val AUTH_NONE = "none"    // Kein Schutz
        const val AUTH_PIN = "pin"      // PIN-Schutz aktiv

        // Theme-Optionen
        const val THEME_SYSTEM = "system"  // Folgt Android-Systemeinstellung
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        // Akzentfarben
        const val ACCENT_PURPLE = "purple"
        const val ACCENT_BLUE = "blue"
        const val ACCENT_GREEN = "green"
        const val ACCENT_ORANGE = "orange"
        const val ACCENT_PINK = "pink"
        const val ACCENT_TEAL = "teal"

        // Schriftarten (System-Fontfamilies — kein Internet nötig)
        const val FONT_DEFAULT = "default"   // Roboto (Android Standard)
        const val FONT_SERIF = "serif"       // Klassisches Tagebuch-Feeling
        const val FONT_MONO = "mono"         // Schreibmaschinen-Stil
        const val FONT_CURSIVE = "cursive"   // Handschrift-Stil

        // Kalender-Anzeigemodus
        const val CALENDAR_MODE_MOOD = "mood"        // Nur Stimmungs-Emoji
        const val CALENDAR_MODE_WEATHER = "weather"  // Nur Wetter-Emoji
        const val CALENDAR_MODE_BOTH = "both"        // Beides
    }
}
