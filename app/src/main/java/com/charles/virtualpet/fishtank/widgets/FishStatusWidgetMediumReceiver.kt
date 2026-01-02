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

class FishStatusWidgetMediumReceiver : AppWidgetProvider() {
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
            val views = RemoteViews(context.packageName, R.layout.widget_medium)

            try {
                val reader = WidgetGameStateReader(context)
                val gameState = reader.readGameState()
                val derivedState = WidgetDerivedState.deriveForDisplay(gameState)

                // Set mood
                val moodText = "${getMoodEmoji(derivedState.mood)} ${getMoodText(derivedState.mood)}"
                views.setTextViewText(R.id.widget_mood_text, moodText)
                views.setTextColor(R.id.widget_mood_text, getMoodColor(derivedState.mood))

                // Set level
                views.setTextViewText(R.id.widget_level_text, "Level ${derivedState.level}")

                // Set hunger
                val hungerInt = derivedState.hunger.toInt()
                views.setProgressBar(R.id.widget_hunger_bar, 100, hungerInt, false)
                views.setTextViewText(R.id.widget_hunger_text, "$hungerInt%")
                views.setTextColor(R.id.widget_hunger_text, getStatColor(derivedState.hunger))

                // Set cleanliness
                val cleanInt = derivedState.cleanliness.toInt()
                views.setProgressBar(R.id.widget_clean_bar, 100, cleanInt, false)
                views.setTextViewText(R.id.widget_clean_text, "$cleanInt%")
                views.setTextColor(R.id.widget_clean_text, getStatColor(derivedState.cleanliness))

                // Set happiness
                val happyInt = derivedState.happiness.toInt()
                views.setProgressBar(R.id.widget_happy_bar, 100, happyInt, false)
                views.setTextViewText(R.id.widget_happy_text, "$happyInt%")
                views.setTextColor(R.id.widget_happy_text, getStatColor(derivedState.happiness))

                // Set XP and coins
                views.setTextViewText(R.id.widget_xp_text, "⭐ XP ${derivedState.xp}")
                views.setTextViewText(R.id.widget_coins_text, "💰 ${derivedState.coins}")

            } catch (e: Exception) {
                e.printStackTrace()
                views.setTextViewText(R.id.widget_mood_text, "🐠 Fish Status")
                views.setTextViewText(R.id.widget_level_text, "Tap to open app")
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

    private fun getStatColor(value: Float): Int {
        return when {
            value >= 70f -> android.graphics.Color.parseColor("#90D5B4")
            value >= 40f -> android.graphics.Color.parseColor("#FFD1A8")
            else -> android.graphics.Color.parseColor("#FFB3BA")
        }
    }
}
