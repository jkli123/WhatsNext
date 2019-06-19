package com.snowdragon.whatsnext.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TaskList {
    private static final String TAG = "TaskList";

    private static List<Task> sTasks;

    private TaskList() {
        populate(15);
    }

    public static TaskList get() {
        if (sTasks == null) {
            sTasks = new ArrayList<>();
        }
        return new TaskList();
    }

    public List<Task> getTasks() {
        return sTasks;
    }


    public List<Task> sort(Comparator<Task> taskComparator) {
        List<Task> newList = new ArrayList<>();
        newList.addAll(sTasks);
        Collections.sort(newList, taskComparator);
        return newList;
    }

    public void add(Task task) {
        sTasks.add(task);
    }

    public Task read(String id) {
        // Find Task by UUID
        Task target = null;
        for (Task task : sTasks) {
            if (task.getId().equals(id)) {
                target = task;
                break;
            }
        }
        return target;
    }

    public boolean update(String id, TaskChange taskChange) {
        // Find Task by UUID
        Task target = read(id);

        // Update all changed fields using data in taskChange
        return taskChange.updateTask(target);
    }

    public Task remove(String id) {
         //Find Task by UUID
        Task target = read(id);
        sTasks.remove(target);
        return target;
    }

    public void populate(int taskCount) {
        Random random = new Random();
        for (int i = 1; i < taskCount + 1; i++) {
            Task task = new Task();
            task.setName("Task " + i);
            task.setCategory(i % 2 == 0 ? Task.STUDY_CATEGORY : Task.WORK_CATEGORY);
            task.setDescription("Description " + i);
            task.setStatus(random.nextInt(4) +1);
            task.setDeadline(Calendar.getInstance());
            add(task);
        }
    }
}
