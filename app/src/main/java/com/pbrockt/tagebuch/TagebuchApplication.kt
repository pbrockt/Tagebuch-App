package com.pbrockt.tagebuch

import android.app.Application
import com.pbrockt.tagebuch.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TagebuchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
