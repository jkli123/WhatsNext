package com.snowdragon.whatsnext.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TaskList {

    public static final int DONE_LIST = 0;
    public static final int NOT_DONE_LIST = 1;

    private static final String TAG = "TaskList";

    private static List<Task> sTasks;
    private static List<Task> sTasksDone;
    private static boolean sIsFirst = true;

    private TaskList() {
        if(sIsFirst) {
            sIsFirst = !sIsFirst;
        }
    }

    public static TaskList get() {
        if (sTasks == null) {
            sTasks = new ArrayList<>();
            sTasksDone = new ArrayList<>();
        }
        return new TaskList();
    }

    public List<Task> getTaskList(int type) {
        if(type == DONE_LIST) {
            return sTasksDone;
        } else if(type == NOT_DONE_LIST) {
            return sTasks;
        } else {
            dispatchTypeException();
            return null;
        }
    }

    public TaskList setTaskList(int type, List<Task> tasks) {
        if(type == DONE_LIST) {
            sTasksDone = tasks;
        } else if(type == NOT_DONE_LIST) {
            sTasks = tasks;
        } else {
            dispatchTypeException();
        }
        return this;
    }

    public TaskList sortTaskList(int type, Comparator<? super Task> taskComparator) {
        if(type == DONE_LIST) {
            Collections.sort(sTasksDone, taskComparator);
        } else if(type == NOT_DONE_LIST) {
            Collections.sort(sTasks, taskComparator);
        } else {
            dispatchTypeException();
        }
        return this;
    }

    public TaskList addTask(int type, Task task) {
        TaskComparatorFactory factory = new TaskComparatorFactory();
        if(type == DONE_LIST) {
            if(task.getStatus() != Task.DONE) {
                dispatchStatusException();
            }
            sTasksDone.add(task);
            Collections.sort(sTasksDone, factory.getDeadlineComparator());
        } else if(type == NOT_DONE_LIST) {
            if(task.getStatus() == Task.DONE) {
                dispatchStatusException();
            }
            sTasks.add(task);
            Collections.sort(sTasks, factory.getDeadlineComparator());
        } else {
            dispatchTypeException();
        }
        return this;
    }

    public Task deleteTaskById(String taskId) {
        Task task = read(taskId);
        if(task.getStatus() == Task.DONE) {
            sTasksDone.remove(task);
        } else {
            sTasks.remove(task);
        }
        return task;
    }

    public TaskList updateTask(String taskId, TaskChange taskChange) {
        Task task = deleteTaskById(taskId);
        taskChange.updateTask(task);
        if(task.getStatus() == Task.DONE) {
            addTask(DONE_LIST, task);
        } else {
            addTask(NOT_DONE_LIST, task);
        }
        return this;
    }

    private void dispatchTypeException() {
        throw new IllegalArgumentException("Passed in type not held by TaskList");
    }

    private void dispatchStatusException() {
        throw new IllegalStateException("Task status does not match with type of list operated on");
    }

    private void dispatchNullTypeException() {
        throw new IllegalArgumentException("null value passed in");
    }

    private Task read(String id) throws IllegalArgumentException {
        if (id == null) {
            dispatchNullTypeException();
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
}
