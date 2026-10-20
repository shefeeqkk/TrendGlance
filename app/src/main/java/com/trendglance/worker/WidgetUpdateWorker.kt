package com.trendglance.worker

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.trendglance.data.local.PreferencesRepository
import com.trendglance.data.repository.TrendsRepositoryImpl
import com.trendglance.widget.TrendGlanceWidget
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * WorkManager CoroutineWorker responsible for background fetching Google Trends RSS feeds
 * and updating Jetpack Glance Widget state reliably and efficiently.
 */
class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        val TOPICS_FORMATTED_KEY = stringPreferencesKey("widget_topics_formatted")
        val LAST_UPDATED_KEY = stringPreferencesKey("widget_last_updated")
        val ERROR_MESSAGE_KEY = stringPreferencesKey("widget_error_message")
    }

    override suspend fun doWork(): Result {
        return try {
            val prefsRepo = PreferencesRepository(context)
            val userPrefs = prefsRepo.userPreferencesFlow.first()

            val trendsRepo = TrendsRepositoryImpl()
            val result = trendsRepo.getTrendingTopics(
                geoRegion = userPrefs.geoRegion,
                limit = userPrefs.itemCount
            )

            val glanceManager = GlanceAppWidgetManager(context)
            val glanceIds = glanceManager.getGlanceIds(TrendGlanceWidget::class.java)

            if (glanceIds.isEmpty()) {
                return Result.success()
            }

            result.fold(
                onSuccess = { topics ->
                    val formattedTopics = topics.joinToString("\n") { topic ->
                        val title = topic.title.replace("\n", " ").replace("|", "").trim()
                        val traffic = topic.approxTraffic.replace("\n", " ").replace("|", "").trim()
                        val url = (topic.newsUrl ?: "").replace("\n", " ").replace("|", "").trim()
                        val rawDesc = topic.description.replace("\n", " ").replace("|", "").trim()
                        val desc = if (rawDesc.isNotBlank()) rawDesc else (topic.newsTitle ?: "Real-time search volume and news updates.")
                        "$title|$traffic|$url|$desc"
                    }
                    val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                    val widget = TrendGlanceWidget()
                    for (glanceId in glanceIds) {
                        updateAppWidgetState(context, glanceId) { prefs ->
                            prefs[TOPICS_FORMATTED_KEY] = formattedTopics
                            prefs[LAST_UPDATED_KEY] = timestamp
                            prefs.remove(ERROR_MESSAGE_KEY)
                        }
                        widget.update(context, glanceId)
                    }
                    Result.success()
                },
                onFailure = { error ->
                    val widget = TrendGlanceWidget()
                    for (glanceId in glanceIds) {
                        updateAppWidgetState(context, glanceId) { prefs ->
                            prefs[ERROR_MESSAGE_KEY] = error.localizedMessage ?: "Fetch error"
                        }
                        widget.update(context, glanceId)
                    }
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
