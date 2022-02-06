package com.example.shohanov_application


import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews


class MyWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val sp = context.getSharedPreferences(
            MyWidgetConfig.WIDGET_PREF, Context.MODE_PRIVATE
        )

        for(id:Int in appWidgetIds) {
            update_widget(context, sp, id, appWidgetManager)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        fun update_widget(
            context: Context,
            sp: SharedPreferences,
            id: Int,
            appWidgetManager: AppWidgetManager
        ) {
            val updateViews = RemoteViews(
                context.packageName,
                R.layout.my_widget
            )

            val widgetColor = sp.getInt(MyWidgetConfig.WIDGET_COLOR + id, 0)

            updateViews.setTextColor(R.id.text_wid_en, widgetColor)
            updateViews.setTextColor(R.id.text_wid_ru, widgetColor)

            if (MyService.list_words_like.contains(MyService.id_current_word)) {
                updateViews.setImageViewResource(R.id.imageView_like_widg, R.drawable.ic_dis_like)
            } else {
                updateViews.setImageViewResource(R.id.imageView_like_widg, R.drawable.ic_like)
            }

            updateViews.setTextViewText(R.id.text_wid_en, MyService.word_en)
            updateViews.setTextViewText(R.id.text_wid_ru, MyService.word_ru)
            setIntents(updateViews, context, id)
            appWidgetManager.updateAppWidget(id, updateViews)
        }

        private fun setIntents(
            rm: RemoteViews,
            context: Context,
            appWidgetId: Int
        ) {
            val next_intent = Intent(context, MyBroadcastReceiver::class.java)
            next_intent.action= MyService.NEXT_WORD_ACTION
            val next_pi = PendingIntent.getBroadcast(context, appWidgetId, next_intent, PendingIntent.FLAG_UPDATE_CURRENT)
            rm.setOnClickPendingIntent(R.id.imageView_word_widg, next_pi)

            val like_intent = Intent(context, MyBroadcastReceiver::class.java)
            like_intent.action= MyService.LIKE_WORD_ACTION
            val like_pi = PendingIntent.getBroadcast(context, appWidgetId, like_intent, PendingIntent.FLAG_UPDATE_CURRENT)
            rm.setOnClickPendingIntent(R.id.imageView_like_widg, like_pi)

            val sound_intent = Intent(context, MyBroadcastReceiver::class.java)
            sound_intent.action= MyService.PLAY_WORD_ACTION
            val sound_pi = PendingIntent.getBroadcast(context, appWidgetId, sound_intent, PendingIntent.FLAG_UPDATE_CURRENT)
            rm.setOnClickPendingIntent(R.id.imageView_sound_widg, sound_pi)


            val resultIntent = Intent(context, MainActivity::class.java)
            val resultPendingIntent =PendingIntent.getActivity(context,appWidgetId,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT)
            rm.setOnClickPendingIntent(R.id.text_wid_en, resultPendingIntent)
            rm.setOnClickPendingIntent(R.id.text_wid_ru, resultPendingIntent)
        }
    }
}

