package com.pbrockt.tagebuch.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class ReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        NotificationHelper.showReminder(appContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "tagebuch_daily_reminder"

        fun schedule(context: Context, hour: Int, minute: Int) {
            val now = LocalDateTime.now()
            val target = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute))
            val initialDelay = if (target.isAfter(now)) {
                java.time.Duration.between(now, target).toMinutes()
            } else {
                java.time.Duration.between(now, target.plusDays(1)).toMinutes()
            }

            val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
