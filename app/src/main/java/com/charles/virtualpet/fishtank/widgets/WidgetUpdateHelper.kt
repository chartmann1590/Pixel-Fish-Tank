package com.charles.virtualpet.fishtank.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Helper to trigger widget updates when game state changes
 */
object WidgetUpdateHelper {
    fun updateAllWidgets(context: Context) {
        // Update small widgets
        val smallIntent = Intent(context, FishStatusWidgetSmallReceiver::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, FishStatusWidgetSmallReceiver::class.java))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(smallIntent)

        // Update medium widgets
        val mediumIntent = Intent(context, FishStatusWidgetMediumReceiver::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, FishStatusWidgetMediumReceiver::class.java))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(mediumIntent)

        // Update streak widgets
        val streakIntent = Intent(context, StreakWidgetReceiver::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, StreakWidgetReceiver::class.java))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(streakIntent)

        // Update daily tasks widgets
        val tasksIntent = Intent(context, DailyTasksWidgetReceiver::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, DailyTasksWidgetReceiver::class.java))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(tasksIntent)
    }
}
