package com.vvsoftdev.to_dotask.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.content.ContextCompat;

import com.vvsoftdev.to_dotask.R;
import com.vvsoftdev.to_dotask.UI.MainActivity;
import com.vvsoftdev.to_dotask.sync.ReminderTasks;
import com.vvsoftdev.to_dotask.sync.ToDoReminderIntentService;


/**
 * Utility class for creating hydration notifications
 */
public class NotificationUtils {

    /*
     * This notification ID can be used to access our notification after we've displayed it. This
     * can be handy when we need to cancel the notification, or perhaps update it. This number is
     * arbitrary and can be set to whatever you like. 1138 is in no way significant.
     */
    private static final int TODO_REMINDER_NOTIFICATION_ID = 1138;
    /**
     * This pending intent id is used to uniquely reference the pending intent
     */
    private static final int TODO_REMINDER_PENDING_INTENT_ID = 3417;
    /**
     * This notification channel id is used to link notifications to this channel
     */
    private static final String REMINDER_NOTIFICATION_CHANNEL_ID = "reminder_notification_channel";
    private static final int ACTION_DELETE_ALL_INTENT_ID = 1;
    private static final int ACTION_IGNORE_PENDING_INTENT_ID = 14;

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void remindUser(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    REMINDER_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, REMINDER_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_todo_notification)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(context.getString(R.string.app_name) + " : " + context.getString(R.string.reminder_notification_title))
                .setContentText(context.getString(R.string.reminder_notification_body))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        context.getString(R.string.reminder_notification_body)))
                // if you want the phone vibrate when notification appeared(need to add associated permission)
                // .setDefaults(Notification.DEFAULT_VIBRATE)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(contentIntent(context))
                .addAction(deleteAllTasks(context))
                .addAction(ignoreReminderAction(context))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(TODO_REMINDER_NOTIFICATION_ID, notificationBuilder.build());
    }
    private static Action ignoreReminderAction(Context context) {
        Intent ignoreReminderIntent = new Intent(context, ToDoReminderIntentService.class);
        ignoreReminderIntent.setAction(ReminderTasks.ACTION_DISMISS_NOTIFICATION);
        PendingIntent ignoreReminderPendingIntent = PendingIntent.getService(
                context,
                ACTION_IGNORE_PENDING_INTENT_ID,
                ignoreReminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Action ignoreReminderAction = new Action(R.drawable.ic_cancel_black_24px,
                "Hide",
                ignoreReminderPendingIntent);
        return ignoreReminderAction;
    }

    private static Action deleteAllTasks(Context context) {
        Intent deleteAllTaskIntent = new Intent(context, ToDoReminderIntentService.class);
        deleteAllTaskIntent.setAction(ReminderTasks.ACTION_DELETE_ALL_TASK);
        PendingIntent deletePendingIntent = PendingIntent.getService(
                context,
                ACTION_DELETE_ALL_INTENT_ID,
                deleteAllTaskIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Action deleteAction = new Action(R.drawable.ic_check_box_black_24dp,
                "Delete To-Do tasks",
                deletePendingIntent);
        return deleteAction;
    }

    private static PendingIntent contentIntent(Context context) {
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(
                context,
                TODO_REMINDER_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_check_box_black_24dp);
        return largeIcon;
    }
}