package com.snowdragon.whatsnext.debug;

import android.util.Log;

import com.snowdragon.whatsnext.database.Database;
import com.snowdragon.whatsnext.model.Task;

import java.util.List;

//Just to test if the listener for the database events works. This class useless AF.
class DebugDatabaseListener implements Database.OnDatabaseStateChangeListener {
    private static final String TAG = "DebugDatabaseListener";

    @Override
    public void onAdd(Task task) {
        Log.d(TAG, "Informed of add event on task id: " + task.getId());
    }

    @Override
    public void onUpdate(String taskId) {
        Log.d(TAG, "Informed of update event on task id: " + taskId);
    }

    @Override
    public void onDelete(Task task) {
        Log.d(TAG, "Informed of delete event on task id: " + task.getId());
    }

    @Override
    public void onGet(int type, List<Task> task) {
        Log.d(TAG, "Informed of get event. retrieved: " + task.toString());
    }
}
