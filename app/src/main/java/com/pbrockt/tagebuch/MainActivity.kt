package com.pbrockt.tagebuch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.pbrockt.tagebuch.navigation.AppNavigation
import com.pbrockt.tagebuch.ui.auth.AuthScreen
import com.pbrockt.tagebuch.ui.theme.TagebuchTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appLockManager: AppLockManager
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ProcessLifecycleOwner feuert ON_STOP wenn die App WIRKLICH in den
        // Hintergrund geht (nicht bei System-Dialogen/Benachrichtigungen).
        // Zuverlässiger als Activity.onPause/onStop für App-Wechsel.
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    appLockManager.lockApp()
                }
            }
        )

        setContent {
            val themeChoice by mainViewModel.themeChoice.collectAsState()
            val accentColor by mainViewModel.accentColor.collectAsState()
            val fontChoice by mainViewModel.fontChoice.collectAsState()
            val isLocked by appLockManager.isLocked.collectAsState()

            TagebuchTheme(
                themeChoice = themeChoice,
                accentColor = accentColor,
                fontChoice = fontChoice
            ) {
                if (isLocked) {
                    AuthScreen(onAuthenticated = { appLockManager.unlockApp() })
                } else {
                    AppNavigation(onThemeChanged = { mainViewModel.refresh() })
                }
            }
        }
    }
}
