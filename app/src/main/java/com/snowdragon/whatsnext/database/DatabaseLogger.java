package com.snowdragon.whatsnext.database;

import android.content.Context;
import android.util.Log;

import com.snowdragon.whatsnext.controller.R;
import com.snowdragon.whatsnext.model.Task;

import java.util.List;

/**
 * A Logging class solely for use in logging Database messages.
 * <p>
 *    This class helps to log messages to Logcat for
 *    any database related messages. It requires a
 *    context object to access the shared resources
 *    like strings.xml file that defines the message
 *    templates for some of the messages in this class.
 *    It is also able to log a general message.
 * </p>
 */
class DatabaseLogger {
    private static final String TAG = "DatabaseLogger";

    private final Context mContext;

    private DatabaseLogger(Context context) {
        mContext = context;
    }

    static DatabaseLogger getInstance(Context context) {
        return new DatabaseLogger(context);
    }

    void logUpdateMessage(String taskId) {
        Log.d(TAG, mContext.getString(R.string.database_update_message, taskId));
    }

    void logAddMessage(String taskId) {
        Log.d(TAG, mContext.getString(R.string.database_add_message, taskId));
    }

    void logGetMessage(List<Task> tasks) {
        Log.d(TAG, mContext.getString(R.string.database_get_message, tasks.toString()));
    }

    void logDeleteMessage(String taskId) {
        Log.d(TAG, mContext.getString(R.string.database_delete_message, taskId));
    }

    void logMessage(String message) {
        Log.i(TAG, mContext.getString(R.string.database_general_message, message));
    }
}