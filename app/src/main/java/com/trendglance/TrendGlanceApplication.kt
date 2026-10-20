package com.trendglance

import android.app.Application
import com.trendglance.data.local.PreferencesRepository
import com.trendglance.worker.WorkManagerScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Custom Application class for TrendGlance. Initializing background WorkManager worker.
 */
class TrendGlanceApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initDefaultBackgroundWorker()
    }

    private fun initDefaultBackgroundWorker() {
        CoroutineScope(Dispatchers.IO).launch {
            val prefsRepo = PreferencesRepository(this@TrendGlanceApplication)
            val prefs = prefsRepo.userPreferencesFlow.first()
            WorkManagerScheduler.scheduleWidgetUpdate(
                context = this@TrendGlanceApplication,
                intervalMinutes = prefs.refreshIntervalMinutes
            )
        }
    }
}
