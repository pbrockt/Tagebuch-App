package com.pbrockt.tagebuch.notifications

import android.content.Context
import androidx.work.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Hintergrund-Aufgabe für die tägliche Erinnerung.
 *
 * WorkManager ist Androids empfohlene Lösung für zuverlässige
 * Hintergrundaufgaben. Er berücksichtigt:
 * - Akkuoptimierungen (Doze-Modus)
 * - Geräteneustarts (Aufgaben bleiben erhalten)
 * - Netzwerkbedingungen
 *
 * CoroutineWorker: Eine WorkManager-Aufgabe die Coroutines unterstützt.
 * doWork() läuft auf einem Hintergrund-Thread.
 */
class ReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    /** Wird von WorkManager aufgerufen wenn die Aufgabe ausgeführt werden soll */
    override suspend fun doWork(): Result {
        NotificationHelper.showReminder(appContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "tagebuch_daily_reminder"

        /**
         * Plant die tägliche Erinnerung zur angegebenen Uhrzeit.
         *
         * PeriodicWorkRequest: Wiederholt sich alle 24 Stunden.
         * initialDelay: Wartet bis zur nächsten Ziel-Uhrzeit.
         *
         * Beispiel: Jetzt ist 14:00, Erinnerung soll um 21:00 kommen.
         * initialDelay = 7 Stunden. Danach alle 24 Stunden.
         *
         * @param hour   Stunde (0-23)
         * @param minute Minute (0-59)
         */
        fun schedule(context: Context, hour: Int, minute: Int) {
            val now = LocalDateTime.now()
            val target = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute))

            // Wenn die Zielzeit heute schon vorbei ist → morgen um diese Zeit
            val initialDelay = if (target.isAfter(now)) {
                java.time.Duration.between(now, target).toMinutes()
            } else {
                java.time.Duration.between(now, target.plusDays(1)).toMinutes()
            }

            val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MINUTES)
                .build()

            // UPDATE: Bestehenden Plan ersetzen
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        /** Bricht die tägliche Erinnerung ab */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
