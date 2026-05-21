package com.pbrockt.tagebuch.ui.theme

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.pbrockt.tagebuch.data.local.prefs.SecurePrefs
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class AccentColors(val light: androidx.compose.material3.ColorScheme, val dark: androidx.compose.material3.ColorScheme)

val accentMap: Map<String, AccentColors> = mapOf(
    SecurePrefs.ACCENT_PURPLE to AccentColors(
        light = lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF6650A4), secondary = androidx.compose.ui.graphics.Color(0xFF625B71), tertiary = androidx.compose.ui.graphics.Color(0xFF7D5260)),
        dark  = darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFFD0BCFF), secondary = androidx.compose.ui.graphics.Color(0xFFCCC2DC), tertiary = androidx.compose.ui.graphics.Color(0xFFEFB8C8))
    ),
    SecurePrefs.ACCENT_BLUE to AccentColors(
        light = lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF0061A4), secondary = androidx.compose.ui.graphics.Color(0xFF535F70), tertiary = androidx.compose.ui.graphics.Color(0xFF6B5778)),
        dark  = darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF9ECAFF), secondary = androidx.compose.ui.graphics.Color(0xFFBBC8DB), tertiary = androidx.compose.ui.graphics.Color(0xFFD4BBDE))
    ),
    SecurePrefs.ACCENT_GREEN to AccentColors(
        light = lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF386A20), secondary = androidx.compose.ui.graphics.Color(0xFF55624C), tertiary = androidx.compose.ui.graphics.Color(0xFF386666)),
        dark  = darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF8EDA68), secondary = androidx.compose.ui.graphics.Color(0xFFBECBB1), tertiary = androidx.compose.ui.graphics.Color(0xFFA0CFCE))
    ),
    SecurePrefs.ACCENT_ORANGE to AccentColors(
        light = lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF9C4500), secondary = androidx.compose.ui.graphics.Color(0xFF775651), tertiary = androidx.compose.ui.graphics.Color(0xFF6E5B2E)),
        dark  = darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFFFFB68D), secondary = androidx.compose.ui.graphics.Color(0xFFE7BDB8), tertiary = androidx.compose.ui.graphics.Color(0xFFD9C391))
    ),
    SecurePrefs.ACCENT_PINK to AccentColors(
        light = lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF8B3A5C), secondary = androidx.compose.ui.graphics.Color(0xFF745570), tertiary = androidx.compose.ui.graphics.Color(0xFF7A4D3A)),
        dark  = darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFFFFB0CB), secondary = androidx.compose.ui.graphics.Color(0xFFDEB9D9), tertiary = androidx.compose.ui.graphics.Color(0xFFEFB99A))
    ),
    SecurePrefs.ACCENT_TEAL to AccentColors(
        light = lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF006874), secondary = androidx.compose.ui.graphics.Color(0xFF4A6267), tertiary = androidx.compose.ui.graphics.Color(0xFF525E7D)),
        dark  = darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF4FD8EB), secondary = androidx.compose.ui.graphics.Color(0xFFB1CBD0), tertiary = androidx.compose.ui.graphics.Color(0xFFBAC3E8))
    )
)

@Composable
fun TagebuchTheme(
    themeChoice: String = SecurePrefs.THEME_SYSTEM,
    accentColor: String = SecurePrefs.ACCENT_PURPLE,
    fontChoice: String = SecurePrefs.FONT_DEFAULT,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeChoice) {
        SecurePrefs.THEME_DARK -> true
        SecurePrefs.THEME_LIGHT -> false
        else -> isSystemInDarkTheme()
    }

    val colors = accentMap[accentColor] ?: accentMap[SecurePrefs.ACCENT_PURPLE]!!
    val colorScheme = if (darkTheme) colors.dark else colors.light
    val typography = getTypography(fontChoice)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

@Composable
fun FloralBackground(modifier: Modifier = Modifier) {
    val petalColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
    val centerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val spacing = 72f
        val petalRadius = 9f
        val petalDist = 13f
        val centerRadius = 4f

        var row = 0
        var y = spacing / 2f
        while (y < size.height + spacing) {
            val xOffset = if (row % 2 == 0) spacing / 2f else 0f
            var x = xOffset
            while (x < size.width + spacing) {
                for (i in 0 until 5) {
                    val angle = (i * 72.0 - 90.0) * PI / 180.0
                    drawCircle(petalColor, petalRadius,
                        Offset(x + cos(angle).toFloat() * petalDist, y + sin(angle).toFloat() * petalDist))
                }
                drawCircle(centerColor, centerRadius, Offset(x, y))
                x += spacing
            }
            y += spacing
            row++
        }
    }
}
