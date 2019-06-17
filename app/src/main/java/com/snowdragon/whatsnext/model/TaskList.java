package com.snowdragon.whatsnext.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class TaskList {
    public TaskList mTaskList;
    private List<Task> mTasks;

    /*
     *
     *
     */
    public TaskList get() {
        if (mTaskList == null) {
            mTaskList = new TaskList();
        }
        return mTaskList;
    }


    public List<Task> sort(Comparator<Task> taskComparator) {
        List<Task> newList = new ArrayList<>();
        newList.addAll(mTasks);
        Collections.sort(newList, taskComparator);
        return newList;
    }

    public void add(Task task) {
        mTasks.add(task);
    }

    public Task read(UUID id) {
        // Find Task by UUID
        Task target = null;
        for (Task task : mTasks) {
            if (task.getId().equals(id)) {
                target = task;
                break;
            }
        }
        return target;
    }

    public boolean update(UUID id, TaskChange taskChange) {
        // Find Task by UUID
        Task target = read(id);

        // Update all changed fields using data in taskChange
        return taskChange.updateTask(target);
    }

    public Task remove(UUID id) {
        // Find Task by UUID
        Task target = read(id);
        mTasks.remove(target);
        return target;
    }
}
