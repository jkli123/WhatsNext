package com.snowdragon.whatsnext.model;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class TaskListTest {

    @Test
    public void getReturnsNonNullTaskListReference () {
        assertNotNull(TaskList.get());
    }

    @Test
    public void getWithMultipleCallsReturnsDifferentTaskListReference() {
        assertNotSame(TaskList.get(), TaskList.get());
    }

    @Test
    public void getTasksWithMultipleCallsReturnsSameListOfTask() {
        assertSame(TaskList.get().getTasks(), TaskList.get().getTasks());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void sortWithIdComparatorReturnsListOfTaskAccordingToId() {
        Comparator<Task> dummyComparator =
                (Comparator<Task>) Mockito.mock(Comparator.class);
        assertSame(TaskList.get().sort(dummyComparator), TaskList.get().getTasks());
    }

    @Test
    public void populateWhenCallingGetMoreThanOnceShouldOnlyBeCalledOnce() {
        List<Task> initial = new ArrayList<>();
        //Initial call to task list
        for(Task task : TaskList.get().getTasks()) {
            initial.add(task);
        }
        //Second call to task list should not populate the list again
        assertArrayEquals(initial.toArray(), TaskList.get().getTasks().toArray());
    }

    @Test
    public void addDummyTaskToTaskListShouldChangeTaskList() {
        Task dummyTask = newTask();
        List<Task> initial = new ArrayList<>();
        for(Task task : TaskList.get().getTasks()) {
            initial.add(task);
        }
        TaskList.get().add(dummyTask);
        assertNotEquals(initial.toArray(), TaskList.get().getTasks().toArray());
    }

    @Test
    public void addDummyTaskToTaskListMakesOtherReferencesSeeChange() {
        Task dummyTask = newTask();
        TaskList listTest = TaskList.get();
        listTest.add(dummyTask);
        assertSame(listTest.getTasks(), TaskList.get().getTasks());
    }

    @Test
    public void readWithDummyTaskIdReturnsDummyTaskAdded() {
        Task task = newTask();
        String id = task.getId();
        TaskList.get().add(task);
        assertEquals(task, TaskList.get().read(id));
    }

    @Test(expected = IllegalArgumentException.class)
    public void readWithNullTaskIdThrowsIllegalArgumentException() {
        TaskList.get().read(null);
    }

    @Test
    public void removeDummyTaskShouldHaveItRemovedFromList() {
        List<Task> initial = copyOf(TaskList.get().getTasks());
        Task task = newTask();
        String idDummy = task.getId();
        TaskList.get().add(task);
        Task removed = TaskList.get().remove(idDummy);
        assertEquals(task, removed);
        assertEquals(initial, TaskList.get().getTasks());
    }

    @Test
    public void updateWithNewNameShouldUpdateTaskInTasklist() {
        TaskChange change =
                new TaskChange.Builder()
                        .add(TaskChange.NAME, "Othername")
                        .build();
        Task task = newTask();
        String idDummy = task.getId();
        task.setName("Initial");
        TaskList.get().add(task);
        assertTrue(TaskList.get().update(idDummy, change));
        assertEquals(task, TaskList.get().read(idDummy));
        assertEquals(task.getName(), "Othername");
    }

    private List<Task> copyOf(List<Task> list) {
        List<Task> tasks = new ArrayList<>();
        for(Task task : list) {
            tasks.add(task);
        }
        return tasks;
    }

    private Task newTask() {
        Task task = new Task();
        task.setName("Test task");
        task.setId(UUID.randomUUID().toString());
        task.setCategory("CAT");
        task.setDeadline(new Date());
        task.setDescription("Desciprtion");
        return task;
    }
}
