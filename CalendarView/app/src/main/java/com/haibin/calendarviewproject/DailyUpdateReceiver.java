package com.haibin.calendarviewproject;


import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.arch.core.executor.ArchTaskExecutor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DailyUpdateReceiver extends BroadcastReceiver {
    private static final String PREF_NAME = "CounterWidgetPrefs";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 强制更新所有小组件实例
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = manager.getAppWidgetIds(
                new ComponentName(context, ClickAppWidget.class)
        );
        new ClickAppWidget().onUpdate(context, manager, appWidgetIds);

        // 重新设置次日闹钟
        DailyAlarmScheduler.scheduleDailyUpdate(context);
    }
    private void cleanOldData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7); // 保留7天
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (String key : prefs.getAll().keySet()) {
            if (key.matches("\\d{4}-\\d{2}-\\d{2}_.+")) {
                String dateStr = key.split("_")[0];
                try {
                    Date date = sdf.parse(dateStr);
                    if (date.before(calendar.getTime())) {
                        editor.remove(key);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        editor.apply();
    }
}