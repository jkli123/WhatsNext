package com.snowdragon.whatsnext.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Task implements Serializable {

    /*
    Please remember to change this serial version UID if any substantial changes are
    being made to this class, incrementing by 1 each time there are changes. This
    is to prevent serialization of older Task objects.
     */
    private static final long serialVersionUID = 1L;
    private static final String TAG = "Task";
    public static final String NAME = "name";
    public static final String CATEGORY = "category";
    public static final String DESCRIPTION = "description";
    public static final String DEADLINE = "deadline";
    public static final String STATUS = "status";

    public static final String WORK_CATEGORY = "WORK";
    public static final String STUDY_CATEGORY = "STUDY";
    public static final int NOT_DONE = 0;
    public static final int IN_PROGRESS = 1;
    public static final int ON_HOLD = 2;
    public static final int DONE = 3;
    public static List<String> sStatusList = Arrays.asList("Not Done", "In Progress", "On Hold", "Done");

    private String mName;
    private String mCategory;
    private String mDescription;
    private Date mDeadline;
    private int mStatus;
    private String mId;

    /*
     * Converts the String representation of Status (as in sStatusList) to its respective integer
     * representation
     */
    public static int getStatusIndexFromString(String status) {
        return sStatusList.indexOf(status);
    }

    /*
     * Converts the integer representation of a status (as specified above in the static fields)
     * to its respective String representation in sStatusList that will be used for display in the
     * UI's TextView and EditText
     */
    public static String getStatusStringFromIndex(int idx) {
        return sStatusList.get(idx);
    }

    public static int toggleStatus(int status) {
        return status <= 0 ? 3 : status - 1;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public Date getDeadline() {
        return mDeadline;
    }

    public void setDeadline(Date deadline) {
        mDeadline = deadline;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public String getId() {
        return mId;
    }

    public void setId(@Nullable String id) {
        if (id == null) {
            mId = UUID.randomUUID().toString();
        } else {
            mId = id;
        }
    }

    public boolean isOverdue() {
        //Only compared year, month, day. Time is of no issue here.
        Calendar d = Calendar.getInstance();
        d.setTime(mDeadline);
        d.set(d.get(Calendar.YEAR),
                d.get(Calendar.MONTH),
                d.get(Calendar.DAY_OF_MONTH),
                0, 0, 0);
        d.set(Calendar.MILLISECOND, 0);
        Calendar today = Calendar.getInstance();
        today.set(today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH),
                0, 0, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.after(d);
    }

    public String toString() {
        return "[Name: " + mName +
                ", Category: " + mCategory +
                ", Description: " + mDescription +
                ", Status: " + mStatus +
                ", Deadline: " + mDeadline +
                ", UUID: " + mId + "]";
    }
}
