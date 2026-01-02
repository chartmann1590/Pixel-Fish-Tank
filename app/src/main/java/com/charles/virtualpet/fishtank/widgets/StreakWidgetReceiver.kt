package com.charles.virtualpet.fishtank.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.charles.virtualpet.fishtank.MainActivity
import com.charles.virtualpet.fishtank.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StreakWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val views = RemoteViews(context.packageName, R.layout.widget_streak)

            try {
                val reader = WidgetGameStateReader(context)
                val gameState = reader.readGameState()
                val dailyTasks = gameState.dailyTasks

                // Set current streak
                views.setTextViewText(R.id.widget_streak_count, "${dailyTasks.currentStreak}")

                // Set longest streak
                views.setTextViewText(
                    R.id.widget_longest_streak,
                    "🏆 Best: ${dailyTasks.longestStreak} days"
                )

            } catch (e: Exception) {
                e.printStackTrace()
                views.setTextViewText(R.id.widget_streak_count, "0")
                views.setTextViewText(R.id.widget_longest_streak, "🏆 Best: 0 days")
            }

            // Set click intent
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_streak_count, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
