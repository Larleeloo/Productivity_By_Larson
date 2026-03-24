package com.larson.productivitybylarson.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.larson.productivitybylarson.MainActivity;
import com.larson.productivitybylarson.R;
import com.larson.productivitybylarson.util.NotificationHelper;

import android.app.PendingIntent;

public class DeadlineAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId = intent.getLongExtra("task_id", -1);
        String taskTitle = intent.getStringExtra("task_title");
        boolean isTimer = intent.getBooleanExtra("is_timer", false);
        String timeLabel = intent.getStringExtra("time_label");

        String title;
        String message;
        String channelId;

        if (isTimer) {
            title = "Timer Complete!";
            message = "Your timer for \"" + taskTitle + "\" has finished. Time to wrap up!";
            channelId = NotificationHelper.CHANNEL_TIMER;
        } else {
            title = "Deadline Approaching!";
            message = "\"" + taskTitle + "\" is due in " + timeLabel + "!";
            channelId = NotificationHelper.CHANNEL_DEADLINE;
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) taskId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) taskId, builder.build());
    }
}
