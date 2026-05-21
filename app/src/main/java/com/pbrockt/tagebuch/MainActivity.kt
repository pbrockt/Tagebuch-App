package com.pbrockt.tagebuch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.pbrockt.tagebuch.navigation.AppNavigation
import com.pbrockt.tagebuch.ui.auth.AuthScreen
import com.pbrockt.tagebuch.ui.theme.TagebuchTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appLockManager: AppLockManager
    private val mainViewModel: MainViewModel by viewModels()

    // Sperrt sofort wenn Bildschirm ausgeschaltet wird
    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                appLockManager.lockApp()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }

    override fun onStop() {
        super.onStop()
        runCatching { unregisterReceiver(screenOffReceiver) }
    }

    override fun onPause() {
        super.onPause()
        appLockManager.onAppPaused()
    }

    override fun onResume() {
        super.onResume()
        appLockManager.onAppResumed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
