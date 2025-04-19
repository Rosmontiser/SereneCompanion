package com.haibin.calendarviewproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Implementation of App Widget functionality.
 */
public class ClickAppWidget extends AppWidgetProvider {

    private static final String PREF_NAME = "CounterWidgetPrefs";
    private static final String KEY_ANXIETY_COUNTER = "anxiety_counter";
    private static final String KEY_HAPPY_COUNTER = "happy_counter";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // 更新所有小组件实例
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        DailyAlarmScheduler.scheduleDailyUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        DailyAlarmScheduler.cancelDailyUpdate(context);
    }
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // 获取当前计数
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String anxietyDailyKey = getDailyKey(KEY_ANXIETY_COUNTER, appWidgetId);
        String happyDailyKey = getDailyKey(KEY_HAPPY_COUNTER, appWidgetId);
        int anxietyCounter = prefs.getInt(anxietyDailyKey, 0);
        int happyCounter = prefs.getInt(happyDailyKey, 0);
        // 创建 RemoteViews 对象
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.click_app_widget);

        // 设置按钮文本为当前计数值
        views.setTextViewText(R.id.button_anxiety_increment, "\uD83D\uDE14 " + anxietyCounter);
        views.setTextViewText(R.id.button_happy_increment, "\uD83D\uDE0A 💕" + happyCounter);

        // 设置按钮点击事件
        views.setOnClickPendingIntent(R.id.button_anxiety_increment,
                getPendingSelfIntent(context, appWidgetId, "ACTION_ANXIETY_INCREMENT"));
        views.setOnClickPendingIntent(R.id.button_happy_increment,
                getPendingSelfIntent(context, appWidgetId, "ACTION_HAPPY_INCREMENT"));

        // 更新小组件
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, android.content.Intent intent) {
        super.onReceive(context, intent);

        // 处理按钮点击事件
        if ("ACTION_ANXIETY_INCREMENT".equals(intent.getAction())) {
            handleAction(context, intent, KEY_ANXIETY_COUNTER, 1);
        } else if ("ACTION_HAPPY_INCREMENT".equals(intent.getAction())) {
            handleAction(context, intent, KEY_HAPPY_COUNTER, 1);
        }
    }

    private void handleAction(Context context, android.content.Intent  intent, String counterKey, int delta) {
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String dailyKey = getDailyKey(counterKey, appWidgetId);
            int counter = prefs.getInt(dailyKey, 0);
            counter += delta;
            prefs.edit().putInt(dailyKey, counter).apply();
            updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);

            // 修改部分：使用Toast代替Notification
            if (counter % 10 == 0) {
                String[] phrases = context.getResources().getStringArray(
                        counterKey.equals(KEY_ANXIETY_COUNTER) ?
                                R.array.anxiety_comfort_phrases :
                                R.array.happy_encouragement_phrases
                );
                showRandomToast(context, phrases);
            }
        }
    }

    private void showRandomToast(Context context, String[] phrases) {
        if (phrases.length == 0) return;
        String message = phrases[new Random().nextInt(phrases.length)];

        // 通过Handler在主线程显示Toast
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        });
    }

    // 新增方法：生成带日期的存储键
    private static String getDailyKey(String counterKey, int appWidgetId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        return today + "_" + counterKey + "_" + appWidgetId;
    }

    private void sendNotification(Context context, String title, String content) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "mood_widget_channel";

        // 创建通知渠道 (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "心情提醒",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("来自心情记录小组件的温馨提醒");
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.icd)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // 创建 PendingIntent 的辅助方法
    protected static android.app.PendingIntent getPendingSelfIntent(Context context, int appWidgetId, String action) {
        android.content.Intent intent = new android.content.Intent(context, ClickAppWidget.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return android.app.PendingIntent.getBroadcast(context, appWidgetId, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
    }
}