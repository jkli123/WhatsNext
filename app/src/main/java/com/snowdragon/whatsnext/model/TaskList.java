package com.snowdragon.whatsnext.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TaskList {

    private static final String TAG = "TaskList";

    private static List<Task> sTasks;
    private static List<Task> sTasksDone;
    private static boolean sIsFirst = true;

    private TaskList() {
        if(sIsFirst) {
            sIsFirst = !sIsFirst;
//            populate(3);
        }
    }

    public static TaskList get() {
        if (sTasks == null) {
            sTasks = new ArrayList<>();
            sTasksDone = new ArrayList<>();
        }
        return new TaskList();
    }

    public List<Task> getTasks() {
        return sTasks;
    }

    public void setTasks(List<Task> tasks) {
        sTasks = tasks;
    }

    public List<Task> getTasksDone() {
        return sTasksDone;
    }

    public void setTasksDone(List<Task> tasksDone) { sTasksDone = tasksDone; }

    public TaskList sort(Comparator<Task> taskComparator) {
        List<Task> newTasks = new ArrayList<>();
        newTasks.addAll(sTasks);
        Collections.sort(newTasks, taskComparator);
        sTasks = newTasks;

        List<Task> newTasksDone = new ArrayList<>();
        newTasksDone.addAll(sTasksDone);
        Collections.sort(newTasksDone, taskComparator);
        sTasksDone = newTasksDone;
        return this;
    }

    public void add(Task task) {
        if (task.getStatus() == Task.DONE) {
            sTasksDone.add(task);
        } else {
            sTasks.add(task);
        }
    }

    public Task read(String id) throws IllegalArgumentException {
        if(id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        // Find Task by UUID
        Task target = null;
        List<Task> newList = new ArrayList<>();
        newList.addAll(sTasks);
        newList.addAll(sTasksDone);
        for (Task task : newList) {
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
        boolean res = taskChange.updateTask(target);
        if (target.getStatus() == Task.DONE) {
            sTasks.remove(target);
            sTasksDone.add(target);
        }
        return res;
    }

    public Task delete(String id) {
         //Find Task by UUID
        Task target = read(id);
        sTasks.remove(target);
        sTasksDone.remove(target);
        return target;
    }

    public void populate(int taskCount) {
        Random random = new Random();
        for (int i = 1; i < taskCount + 1; i++) {
            Task task = new Task();
            task.setName("Task " + i);
            task.setCategory(i % 2 == 0 ? Task.STUDY_CATEGORY : Task.WORK_CATEGORY);
            task.setDescription("Description " + i);
            task.setStatus(random.nextInt(4));
            task.setDeadline(new Date());
            task.setId(UUID.randomUUID().toString());
            add(task);
        }
    }

}
