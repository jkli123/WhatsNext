package com.snowdragon.whatsnext.model;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TaskChange {
    private static final String TAG = "TaskChange";


    private final HashMap<String, Object> mFieldValueHashMap;

    private TaskChange(HashMap<String, Object> mFieldValueHashMap) {
        this.mFieldValueHashMap = mFieldValueHashMap;
    }

    public Map<String, Object> getFieldValueMap() {
        return mFieldValueHashMap;
    }

    public boolean updateTask(@NonNull Task task) {
        String newName = (String) mFieldValueHashMap.get(Task.NAME);
        if (newName != null) {
            task.setName(newName);
        }

        String newCategory = (String) mFieldValueHashMap.get(Task.CATEGORY);
        if (newCategory != null) {
            task.setCategory(newCategory);
        }

        String newDescription = (String) mFieldValueHashMap.get(Task.DESCRIPTION);
        if (newDescription != null) {
            task.setDescription(newDescription);
        }

        Date newDeadline = (Date) mFieldValueHashMap.get(Task.DEADLINE);
        if (newDeadline != null) {
            task.setDeadline(newDeadline);
        }

        Integer newStatus = (Integer) mFieldValueHashMap.get(Task.STATUS);
        if (newStatus != null) {
            task.setStatus(newStatus);
        }
        return true;
    }

    public static class Builder {

        private final HashMap<String, Object> mFieldValueHashMap;

        public Builder() {
            mFieldValueHashMap = new HashMap<>();
        }

        public Builder updateName(String newName) {
            mFieldValueHashMap.put(Task.NAME, newName);
            return this;
        }

        public Builder updateCategory(String newCategory) {
            mFieldValueHashMap.put(Task.CATEGORY, newCategory);
            return this;
        }

        public Builder updateDescription(String newDescription) {
            mFieldValueHashMap.put(Task.DESCRIPTION, newDescription);
            return this;
        }

        public Builder updateStatus(int newStatus) {
            mFieldValueHashMap.put(Task.STATUS, newStatus);
            return this;
        }

        public Builder updateDeadline(Date newDeadline) {
            mFieldValueHashMap.put(Task.DEADLINE, newDeadline);
            return this;
        }

        public TaskChange build() {
            return new TaskChange(mFieldValueHashMap);
        }


    }


}
