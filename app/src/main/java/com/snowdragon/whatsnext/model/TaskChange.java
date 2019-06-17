package com.snowdragon.whatsnext.model;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class TaskChange {
    public static final String NAME = "NAME";
    public static final String CATEGORY = "CATEGORY";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String DEADLINE = "DEADLINE";
    public static final String STATUS = "STATUS";

    private HashMap<String, Object> mFieldValueHashMap;

    private TaskChange(HashMap<String, Object> mFieldValueHashMap) {
        this.mFieldValueHashMap = mFieldValueHashMap;
    }

    public boolean updateTask(Task task) {
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

        Calendar newDeadline = (Calendar) mFieldValueHashMap.get(DEADLINE);
        if (newDeadline != null) {
            task.setDeadline(newDeadline);
        }

        Integer newStatus = (Integer) mFieldValueHashMap.get(STATUS);
        if (newStatus != null) {
            task.setStatus(newStatus);
        }
        return true;
    }

    public class Builder {

        private HashMap<String, Object> mFieldValueHashMap;

        public Builder() {
            mFieldValueHashMap = new HashMap<>();
        }

        public Builder add(String field, Object value) {
            mFieldValueHashMap.put(field, value);
            return this;
        }

        public TaskChange build() {
            return new TaskChange(mFieldValueHashMap);
        }
    }
}
