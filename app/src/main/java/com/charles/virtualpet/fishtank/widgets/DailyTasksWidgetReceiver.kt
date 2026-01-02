package com.charles.virtualpet.fishtank.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.charles.virtualpet.fishtank.MainActivity
import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.domain.model.DailyTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyTasksWidgetReceiver : AppWidgetProvider() {
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
            val views = RemoteViews(context.packageName, R.layout.widget_daily_tasks)

            try {
                val reader = WidgetGameStateReader(context)
                val gameState = reader.readGameState()
                val dailyTasks = gameState.dailyTasks.tasks

                val completedCount = dailyTasks.count { it.isCompleted }
                val totalCount = dailyTasks.size

                // Set completed count
                views.setTextViewText(R.id.widget_tasks_completed, "$completedCount/$totalCount")

                // Update footer message
                val footerMessage = when {
                    completedCount == totalCount && totalCount > 0 -> "All done! Great job! 🎉"
                    completedCount > totalCount / 2 -> "Keep it up! 🎯"
                    completedCount > 0 -> "Good start! 💪"
                    else -> "Let's get started! 🚀"
                }
                views.setTextViewText(R.id.widget_tasks_footer, footerMessage)

                // Update task items (show up to 4 tasks)
                val taskViews = listOf(
                    Triple(R.id.widget_task1_check, R.id.widget_task1_name, R.id.widget_task1_reward),
                    Triple(R.id.widget_task2_check, R.id.widget_task2_name, R.id.widget_task2_reward),
                    Triple(R.id.widget_task3_check, R.id.widget_task3_name, R.id.widget_task3_reward),
                    Triple(R.id.widget_task4_check, R.id.widget_task4_name, R.id.widget_task4_reward)
                )

                for (i in taskViews.indices) {
                    val (checkId, nameId, rewardId) = taskViews[i]

                    if (i < dailyTasks.size) {
                        val task = dailyTasks[i]

                        // Show task
                        views.setViewVisibility(checkId, View.VISIBLE)
                        views.setViewVisibility(nameId, View.VISIBLE)
                        views.setViewVisibility(rewardId, View.VISIBLE)

                        // Update check mark
                        if (task.isCompleted) {
                            views.setTextViewText(checkId, "✓")
                            views.setTextColor(checkId, Color.parseColor("#90D5B4"))
                        } else {
                            views.setTextViewText(checkId, "○")
                            views.setTextColor(checkId, Color.parseColor("#BDC3C7"))
                        }

                        // Update task name
                        views.setTextViewText(nameId, task.name)

                        // Update reward
                        views.setTextViewText(rewardId, "💰${task.rewardCoins}")
                    } else {
                        // Hide unused task slots
                        views.setViewVisibility(checkId, View.GONE)
                        views.setViewVisibility(nameId, View.GONE)
                        views.setViewVisibility(rewardId, View.GONE)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                views.setTextViewText(R.id.widget_tasks_completed, "0/0")
                views.setTextViewText(R.id.widget_tasks_footer, "Tap to open app")
            }

            // Set click intent
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_tasks_completed, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
