package com.vvsoftdev.to_dotask.sync;

import android.content.Context;

import com.vvsoftdev.to_dotask.database.AppDatabase;
import com.vvsoftdev.to_dotask.utilities.NotificationUtils;
import com.vvsoftdev.to_dotask.utilities.ReminderUtils;


public class ReminderTasks {

    public static final String ACTION_DELETE_ALL_TASK = "delete-all-task";
    public static final String ACTION_DISMISS_NOTIFICATION = "dismiss-notification";
    static final String ACTION_REMIND_NOTIFICATION = "show-notification";

    private static AppDatabase mDb;
    public static void executeTask(Context context, String action) {
        if (ACTION_DELETE_ALL_TASK.equals(action)) {
            deleteAllTask(context);
        } else if (ACTION_DISMISS_NOTIFICATION.equals(action)) {
            NotificationUtils.clearAllNotifications(context);
        } else if (ACTION_REMIND_NOTIFICATION.equals(action)){
            NotificationUtils.remindUser(context);
        }
    }

    private static void deleteAllTask(Context context) {
        mDb = AppDatabase.getInstance(context);
        //delete all entries in task table
        mDb.taskDao().nukeTable();
        NotificationUtils.clearAllNotifications(context);
        ReminderUtils.cancelReminder();
    }

}