package com.charles.virtualpet.fishtank.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.charles.virtualpet.fishtank.MainActivity
import com.charles.virtualpet.fishtank.R
import com.charles.virtualpet.fishtank.domain.model.FishMood
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FishStatusWidgetSmallReceiver : AppWidgetProvider() {
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
            val views = RemoteViews(context.packageName, R.layout.widget_small)

            try {
                val reader = WidgetGameStateReader(context)
                val gameState = reader.readGameState()
                val derivedState = WidgetDerivedState.deriveForDisplay(gameState)

                // Set mood
                val moodText = "${getMoodEmoji(derivedState.mood)} ${getMoodText(derivedState.mood)}"
                views.setTextViewText(R.id.widget_mood_text, moodText)
                views.setTextColor(R.id.widget_mood_text, getMoodColor(derivedState.mood))

                // Set hunger with progress bar
                val hungerInt = derivedState.hunger.toInt()
                views.setProgressBar(R.id.widget_hunger_bar, 100, hungerInt, false)
                views.setTextViewText(R.id.widget_hunger_text, "$hungerInt%")

                // Set level and coins
                views.setTextViewText(
                    R.id.widget_level_coins,
                    "Lv.${derivedState.level}  💰${derivedState.coins}"
                )

            } catch (e: Exception) {
                e.printStackTrace()
                views.setTextViewText(R.id.widget_mood_text, "🐠 Tap to open")
            }

            // Set click intent
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_fish_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun getMoodEmoji(mood: FishMood): String {
        return when (mood) {
            FishMood.HAPPY -> "😊"
            FishMood.NEUTRAL -> "😐"
            FishMood.HUNGRY -> "😋"
            FishMood.DIRTY -> "😤"
            FishMood.SAD -> "😢"
        }
    }

    private fun getMoodText(mood: FishMood): String {
        return when (mood) {
            FishMood.HAPPY -> "Happy"
            FishMood.NEUTRAL -> "Neutral"
            FishMood.HUNGRY -> "Hungry"
            FishMood.DIRTY -> "Dirty"
            FishMood.SAD -> "Sad"
        }
    }

    private fun getMoodColor(mood: FishMood): Int {
        return when (mood) {
            FishMood.HAPPY -> android.graphics.Color.parseColor("#90D5B4")
            FishMood.NEUTRAL -> android.graphics.Color.parseColor("#A8D5E2")
            FishMood.HUNGRY -> android.graphics.Color.parseColor("#FFE5B4")
            FishMood.DIRTY -> android.graphics.Color.parseColor("#D4A5A5")
            FishMood.SAD -> android.graphics.Color.parseColor("#FFB3BA")
        }
    }
}
