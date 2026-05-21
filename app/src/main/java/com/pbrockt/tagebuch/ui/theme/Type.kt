package com.pbrockt.tagebuch.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs

/**
 * Gibt die passende FontFamily für die gewählte Schriftart zurück.
 *
 * Diese Schriftarten sind alle im Android-System eingebaut — kein
 * Internet-Download nötig, funktioniert vollständig offline.
 *
 * @param fontChoice Einer der FONT_*-Konstanten aus SecurePrefs
 */
fun getFontFamily(fontChoice: String): FontFamily = when (fontChoice) {
    SecurePrefs.FONT_SERIF -> FontFamily.Serif       // Klassische Buchstaben mit Serifen
    SecurePrefs.FONT_MONO -> FontFamily.Monospace     // Alle Zeichen gleich breit (Schreibmaschine)
    SecurePrefs.FONT_CURSIVE -> FontFamily.Cursive    // Handschrift-ähnlich
    else -> FontFamily.Default                        // Roboto (Android-Standard)
}

/**
 * Erstellt das vollständige Typografie-System für die gewählte Schriftart.
 *
 * Typografie in Material Design 3 besteht aus verschiedenen "Styles" für
 * unterschiedliche Verwendungszwecke: Überschriften, Fließtext, Beschriftungen.
 *
 * Alle Styles verwenden dieselbe FontFamily — nur Größe und Gewicht unterscheiden sich.
 */
fun getTypography(fontChoice: String): Typography {
    val family = getFontFamily(fontChoice)
    return Typography(
        // Großer Titeltext (z.B. "Tagebuch" auf dem PIN-Screen)
        headlineLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
        headlineMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
        headlineSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
        // Abschnitts-Überschriften
        titleLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
        titleMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
        // Fließtext für Tagebucheinträge
        bodyLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
        bodyMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
        // Kleine Beschriftungen (z.B. Wochentage im Kalender)
        labelSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
        labelMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
    )
}

// Fallback-Typografie mit Standard-Schrift — für Compose-Previews
val Typography = getTypography(SecurePrefs.FONT_DEFAULT)
