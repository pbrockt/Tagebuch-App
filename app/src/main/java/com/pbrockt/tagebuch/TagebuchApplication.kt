package com.pbrockt.tagebuch

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.pbrockt.tagebuch.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TagebuchApplication : Application() {

    // Lazy via Hilt — sicher auch wenn Receiver früh feuert
    @Inject lateinit var appLockManager: AppLockManager

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                // Läuft auf Main-Thread, direkter Zugriff sicher
                if (::appLockManager.isInitialized) {
                    appLockManager.lockApp()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Nach super.onCreate() ist Hilt-Injection abgeschlossen
        NotificationHelper.createChannel(this)
        registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }
}
