package com.pbrockt.tagebuch.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.pbrockt.tagebuch.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "tagebuch_reminder"
    private const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Tagebuch Erinnerung",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Tägliche Erinnerung, um deinen Tag festzuhalten"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun showReminder(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Tagebuch")
            .setContentText("Hast du deinen Tag schon festgehalten? ✏️")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }
}
