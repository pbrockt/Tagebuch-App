package com.pbrockt.tagebuch.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pbrockt.tagebuch.ui.calendar.CalendarScreen
import com.pbrockt.tagebuch.ui.search.SearchScreen
import com.pbrockt.tagebuch.ui.settings.SettingsScreen
import com.pbrockt.tagebuch.ui.stats.StatsScreen

sealed class Screen(val route: String) {
    object Calendar : Screen("calendar")
    object Settings : Screen("settings")
    object Search : Screen("search")
    object Stats : Screen("stats")
}

@Composable
fun AppNavigation(
    onThemeChanged: () -> Unit = {},
    calendarIconMode: String = "mood"
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Calendar.route) {
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                calendarIconMode = calendarIconMode
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { onThemeChanged(); navController.popBackStack() })
        }
        composable(Screen.Search.route) {
            SearchScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Stats.route) {
            StatsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
