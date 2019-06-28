package com.snowdragon.whatsnext.model;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TaskChange {
    private static final String TAG = "TaskChange";

    public static final String NAME = "name";
    public static final String CATEGORY = "category";
    public static final String DESCRIPTION = "description";
    public static final String DEADLINE = "deadline";
    public static final String STATUS = "status";

    private final HashMap<String, Object> mFieldValueHashMap;

    private TaskChange(HashMap<String, Object> mFieldValueHashMap) {
        this.mFieldValueHashMap = mFieldValueHashMap;
    }

    public Map<String, Object> getFieldValueMap() {
        return mFieldValueHashMap;
    }

    public boolean updateTask(@NonNull Task task) {
        String newName = (String) mFieldValueHashMap.get(NAME);
        if (newName != null) {
            task.setName(newName);
        }

        String newCategory = (String) mFieldValueHashMap.get(CATEGORY);
        if (newCategory != null) {
            task.setCategory(newCategory);
        }

        String newDescription = (String) mFieldValueHashMap.get(DESCRIPTION);
        if (newDescription != null) {
            task.setDescription(newDescription);
        }

        Date newDeadline = (Date) mFieldValueHashMap.get(DEADLINE);
        if (newDeadline != null) {
            task.setDeadline(newDeadline);
        }

        Integer newStatus = (Integer) mFieldValueHashMap.get(STATUS);
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
            mFieldValueHashMap.put(NAME, newName);
            return this;
        }

        public Builder updateCategory(String newCategory) {
            mFieldValueHashMap.put(CATEGORY, newCategory);
            return this;
        }

        public Builder updateDescription(String newDescription) {
            mFieldValueHashMap.put(DESCRIPTION, newDescription);
            return this;
        }

        public Builder updateStatus(int newStatus) {
            mFieldValueHashMap.put(STATUS, newStatus);
            return this;
        }

        public Builder updateDeadline(Date newDeadline) {
            mFieldValueHashMap.put(DEADLINE, newDeadline);
            return this;
        }

        public TaskChange build() {
            return new TaskChange(mFieldValueHashMap);
        }


    }


}
