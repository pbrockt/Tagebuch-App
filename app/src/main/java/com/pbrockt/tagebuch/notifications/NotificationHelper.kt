package com.pbrockt.tagebuch.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.pbrockt.tagebuch.MainActivity

/**
 * Hilfsfunktionen für Push-Benachrichtigungen.
 *
 * Auf Android 8+ (API 26) müssen Benachrichtigungen einem "Channel" zugeordnet
 * sein. Der Nutzer kann pro Channel die Benachrichtigungs-Einstellungen anpassen.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "tagebuch_reminder"
    private const val NOTIFICATION_ID = 1001

    /**
     * Erstellt den Benachrichtigungs-Kanal.
     * Muss beim App-Start aufgerufen werden (in TagebuchApplication.onCreate()).
     * Auf Android < 8 passiert hier nichts.
     */
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Tagebuch Erinnerung",
            NotificationManager.IMPORTANCE_DEFAULT  // Normale Priorität — kein Ton, kein Bildschirm
        ).apply {
            description = "Tägliche Erinnerung, um deinen Tag festzuhalten"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    /**
     * Zeigt die tägliche Erinnerungs-Benachrichtigung an.
     *
     * PendingIntent: Ein "vorbereiteter Intent" der später ausgeführt wird.
     * Hier: Wenn der Nutzer auf die Benachrichtigung tippt, öffnet sich die App.
     */
    fun showReminder(context: Context) {
        // Beim Tippen auf die Benachrichtigung: App öffnen
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
            .setAutoCancel(true)  // Benachrichtigung verschwindet nach Antippen
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }
}
