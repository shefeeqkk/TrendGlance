package com.trendglance.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.trendglance.data.local.PreferencesRepository
import com.trendglance.worker.WidgetUpdateWorker
import kotlinx.coroutines.flow.first

/**
 * Responsive Jetpack Glance App Widget for TrendGlance.
 * Supports 2x2, 2x3, 2x4, and 4x4 grid sizes with SizeMode.Responsive.
 */
class TrendGlanceWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    companion object {
        private val DEFAULT_2X4 = DpSize(240.dp, 120.dp)   // Default 2x4 (4 cols wide x 2 rows tall)
        private val SMALL_2X2 = DpSize(120.dp, 120.dp)     // 2x2
        private val TALL_2X3 = DpSize(120.dp, 200.dp)      // 2x3
        private val LARGE_4X4 = DpSize(260.dp, 260.dp)     // 4x4
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(DEFAULT_2X4, SMALL_2X2, TALL_2X3, LARGE_4X4)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefsRepo = PreferencesRepository(context)
        val userPrefs = prefsRepo.userPreferencesFlow.first()

        provideContent {
            val glancePrefs = currentState<Preferences>()
            val formattedTopics = glancePrefs[WidgetUpdateWorker.TOPICS_FORMATTED_KEY]
            val lastUpdated = glancePrefs[WidgetUpdateWorker.LAST_UPDATED_KEY]

            val topicList = parseFormattedTopics(formattedTopics)

            TrendGlanceWidgetContent(
                userPrefs = userPrefs,
                topics = topicList,
                lastUpdated = lastUpdated
            )
        }
    }
}

/**
 * AppWidgetReceiver entrypoint registered in AndroidManifest.xml for TrendGlanceWidget.
 */
class TrendGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TrendGlanceWidget()
}
