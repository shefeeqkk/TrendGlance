package com.trendglance.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.trendglance.widget.TrendGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Utility to schedule battery-friendly periodic background refreshes and instant one-time widget updates using WorkManager.
 */
object WorkManagerScheduler {

    private const val WORK_NAME = "TrendGlanceWidgetUpdateWorker"

    /**
     * Schedules or reschedules periodic background worker using user-configured refresh interval in minutes.
     * Supports 30-minute repeat intervals (minimum WorkManager periodic constraint is 15 minutes).
     */
    fun scheduleWidgetUpdate(context: Context, intervalMinutes: Int = 180) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val validInterval = intervalMinutes.coerceAtLeast(15).toLong()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            validInterval, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            periodicWorkRequest
        )
    }

    /**
     * Triggers an immediate one-time background sync AND instantly re-renders the Glance widget UI.
     */
    fun triggerImmediateUpdate(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)

        // Instant widget UI refresh
        CoroutineScope(Dispatchers.IO).launch {
            try {
                TrendGlanceWidget().updateAll(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
