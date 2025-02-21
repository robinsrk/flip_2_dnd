package dev.robin.flip_2_dnd.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dev.robin.flip_2_dnd.R
import dev.robin.flip_2_dnd.services.FlipDetectorService

class FlipDndWidgetProvider : AppWidgetProvider() {
    companion object {
        private const val ACTION_TOGGLE_SERVICE = "dev.robin.flip_2_dnd.TOGGLE_SERVICE"

        fun updateWidgetUI(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetComponent = ComponentName(context, FlipDndWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

            // Trigger an update for all widgets
            val intent = Intent(context, FlipDndWidgetProvider::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_TOGGLE_SERVICE) {
            val serviceIntent = Intent(context, FlipDetectorService::class.java)
            val isServiceRunning = isServiceRunning(context)

            if (isServiceRunning) {
                context.stopService(serviceIntent)
            } else {
                context.startForegroundService(serviceIntent)
            }

            // Update all widgets after toggling the service
            updateWidgetUI(context)
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_flip_dnd)
        val isServiceRunning = isServiceRunning(context)

        // Update widget appearance based on service state
        views.setImageViewResource(
            R.id.widget_toggle_button,
            if (isServiceRunning) R.drawable.ic_widget_active else R.drawable.ic_widget_inactive
        )

        // Set up the toggle action
        val toggleIntent = Intent(context, FlipDndWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_SERVICE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_toggle_button, pendingIntent)

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun isServiceRunning(context: Context): Boolean {
        val serviceIntent = Intent(context, FlipDetectorService::class.java)
        val componentName = ComponentName(context, FlipDetectorService::class.java)
        return context.getSystemService(Context.ACTIVITY_SERVICE)?.let { activityManager ->
            activityManager as android.app.ActivityManager
            activityManager.getRunningServices(Integer.MAX_VALUE)
                .any { it.service == componentName }
        } ?: false
    }
}