package com.xichen.cloudphoto.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Triggers Glance widget redraw after [com.xichen.cloudphoto.widget.WidgetSnapshotSync] writes JSON.
 * Lives in composeApp so Glance @Composable runs with the Compose compiler applied to this module.
 */
object HomeWidgetUpdater {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun updateAll(context: Context) {
        val app = context.applicationContext
        scope.launch {
            RecentPhotosGlanceWidget().updateAll(app)
        }
    }
}
