package com.pbrockt.tagebuch.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.pbrockt.tagebuch.ui.calendar.CalendarScreen
import com.pbrockt.tagebuch.ui.search.SearchScreen
import com.pbrockt.tagebuch.ui.settings.SettingsScreen
import com.pbrockt.tagebuch.ui.stats.StatsScreen

/**
 * Definiert alle Bildschirme der App als versiegelte Klasse.
 *
 * sealed class: Nur die hier definierten Unterklassen sind erlaubt.
 * Das verhindert Tippfehler bei Route-Namen — der Compiler prüft sie.
 */
sealed class Screen(val route: String) {
    object Calendar : Screen("calendar")
    object Search : Screen("search")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}

/** Konfiguration eines einzelnen Bottom-Navigation-Tabs */
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

/** Die vier Tabs der Bottom Navigation */
private val bottomNavItems = listOf(
    BottomNavItem(Screen.Calendar, "Kalender", Icons.Default.CalendarMonth),
    BottomNavItem(Screen.Search, "Suche", Icons.Default.Search),
    BottomNavItem(Screen.Stats, "Statistiken", Icons.Default.BarChart),
    BottomNavItem(Screen.Settings, "Einstellungen", Icons.Default.Settings)
)

/**
 * Die Hauptnavigation der App mit Bottom Navigation Bar.
 *
 * Compose Navigation funktioniert mit einem "NavController" der den
 * aktuellen Bildschirm verwaltet, und einem "NavHost" der die Route
 * auf den zugehörigen Composable mappt.
 *
 * Bottom Navigation: Die vier Tabs unten. Beim Wechsel zwischen Tabs
 * wird der Zustand jedes Screens gespeichert (restoreState = true) —
 * die Scroll-Position bleibt z.B. erhalten.
 *
 * @param onThemeChanged Callback wenn Einstellungen geändert wurden,
 *                       damit das Theme neu geladen wird
 */
@Composable
fun AppNavigation(onThemeChanged: () -> Unit = {}) {
    val navController = rememberNavController()

    // Beobachtet den aktuellen Back-Stack-Eintrag für die Tab-Hervorhebung
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        // Tab ist aktiv wenn die aktuelle Route zu diesem Screen gehört
                        selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                // Zurück zum Start-Screen navigieren statt zu stapeln
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true  // Screen-Zustand speichern
                                }
                                launchSingleTop = true  // Keine doppelten Screens
                                restoreState = true     // Gespeicherten Zustand wiederherstellen
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // innerPadding enthält die Höhe der NavigationBar — verhindert dass
        // Inhalte dahinter versteckt werden
        NavHost(
            navController = navController,
            startDestination = Screen.Calendar.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.Calendar.route) {
                CalendarScreen()
            }
            composable(Screen.Search.route) {
                SearchScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Stats.route) {
                StatsScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                // Nach dem Verlassen der Einstellungen Theme-Refresh auslösen
                SettingsScreen(onNavigateBack = { onThemeChanged() })
            }
        }
    }
}
