package com.larson.productivitybylarson.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.larson.productivitybylarson.receiver.DeadlineAlarmReceiver;

public class NotificationHelper {

    public static final String CHANNEL_DEADLINE = "deadline_channel";
    public static final String CHANNEL_TIMER = "timer_channel";

    public static void createNotificationChannels(Context context) {
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel deadlineChannel = new NotificationChannel(
                CHANNEL_DEADLINE,
                "Deadline Reminders",
                NotificationManager.IMPORTANCE_HIGH
        );
        deadlineChannel.setDescription("Notifications for approaching task deadlines");

        NotificationChannel timerChannel = new NotificationChannel(
                CHANNEL_TIMER,
                "Task Timer",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        timerChannel.setDescription("Notifications when your task timer expires");

        manager.createNotificationChannel(deadlineChannel);
        manager.createNotificationChannel(timerChannel);
    }

    public static void scheduleDeadlineAlarm(Context context, long taskId,
                                              String taskTitle, long deadlineMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Schedule alarms at key intervals before deadline
        long[] offsets = {
                24 * 60 * 60 * 1000L,  // 24 hours before
                60 * 60 * 1000L,        // 1 hour before
                15 * 60 * 1000L         // 15 minutes before
        };
        String[] labels = {"24 hours", "1 hour", "15 minutes"};

        for (int i = 0; i < offsets.length; i++) {
            long triggerTime = deadlineMillis - offsets[i];
            if (triggerTime > System.currentTimeMillis()) {
                Intent intent = new Intent(context, DeadlineAlarmReceiver.class);
                intent.putExtra("task_id", taskId);
                intent.putExtra("task_title", taskTitle);
                intent.putExtra("time_label", labels[i]);

                int requestCode = (int) (taskId * 10 + i);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context, requestCode, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                );
            }
        }
    }

    public static void cancelDeadlineAlarms(Context context, long taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        for (int i = 0; i < 3; i++) {
            Intent intent = new Intent(context, DeadlineAlarmReceiver.class);
            int requestCode = (int) (taskId * 10 + i);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(pendingIntent);
        }
    }

    public static void scheduleTimerAlarm(Context context, long taskId,
                                           String taskTitle, long durationMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, DeadlineAlarmReceiver.class);
        intent.putExtra("task_id", taskId);
        intent.putExtra("task_title", taskTitle);
        intent.putExtra("is_timer", true);

        int requestCode = (int) (taskId * 10 + 9);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = System.currentTimeMillis() + durationMillis;
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
        );
    }
}
