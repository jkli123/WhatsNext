package com.snowdragon.whatsnext.model;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class TaskChangeTest {

    private final String dummyName = "dummyName";
    private final String dummyCat = "dummyCat";
    private final String dummyDesc = "dummyDesc";
    private final Date dummyDeadline = new Date();
    private final int dummyStatus = Task.COMPLETED;

    @Test
    public void updateTaskWithAllParemetersReturnsCorrectlyUpdatedInfo() {
        TaskChange change = new TaskChange.Builder()
                .add(TaskChange.NAME, dummyName)
                .add(TaskChange.CATEGORY, dummyCat)
                .add(TaskChange.DESCRIPTION, dummyDesc)
                .add(TaskChange.DEADLINE, dummyDeadline)
                .add(TaskChange.STATUS, dummyStatus)
                .build();
        Task task = new Task();
        assertTrue(change.updateTask(task));
        assertEquals(dummyName, task.getName());
        assertEquals(dummyCat, task.getCategory());
        assertEquals(dummyDesc, task.getDescription());
        assertEquals(dummyDeadline, task.getDeadline());
        assertEquals(dummyStatus, task.getStatus());
    }

    @Test
    public void updateTaskWithAllNullParametersReturnsInitialTask() {
        TaskChange change = new TaskChange.Builder().build();
        Task task = new Task();
        assertTrue(change.updateTask(task));
        assertEquals(null, task.getName());
        assertEquals(null, task.getCategory());
        assertEquals(null, task.getDescription());
        assertEquals(null, task.getDeadline());
        assertEquals(0, task.getStatus());
    }
}