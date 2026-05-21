package com.pbrockt.tagebuch.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.pbrockt.tagebuch.ui.calendar.CalendarScreen
import com.pbrockt.tagebuch.ui.search.SearchScreen
import com.pbrockt.tagebuch.ui.settings.SettingsScreen
import com.pbrockt.tagebuch.ui.stats.StatsScreen

sealed class Screen(val route: String) {
    object Calendar : Screen("calendar")
    object Search : Screen("search")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Calendar, "Kalender", Icons.Default.CalendarMonth),
    BottomNavItem(Screen.Search, "Suche", Icons.Default.Search),
    BottomNavItem(Screen.Stats, "Statistiken", Icons.Default.BarChart),
    BottomNavItem(Screen.Settings, "Einstellungen", Icons.Default.Settings)
)

@Composable
fun AppNavigation(onThemeChanged: () -> Unit = {}) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Calendar.route,
            modifier = androidx.compose.ui.Modifier.navigationBarsPadding()
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
                SettingsScreen(onNavigateBack = { onThemeChanged() })
            }
        }
    }
}
