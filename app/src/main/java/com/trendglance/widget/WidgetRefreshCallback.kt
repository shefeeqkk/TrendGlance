package com.trendglance.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.trendglance.worker.WorkManagerScheduler

/**
 * Glance ActionCallback triggered when user taps the manual refresh button in the widget header.
 */
class WidgetRefreshCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        WorkManagerScheduler.triggerImmediateUpdate(context)
    }
}
