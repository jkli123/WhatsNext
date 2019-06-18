package com.snowdragon.whatsnext.model;

import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.UUID;

public class Task {
    private static final String TAG = "Task";

    public static final String WORK_CATEGORY = "WORK";
    public static final String STUDY_CATEGORY = "STUDY";
    public static final int COMPLETED = 1;
    public static final int IN_PROGRESS = 2;
    public static final int ON_HOLD = 3;
    public static final int UNDONE = 4;

    private String mName;
    private String mCategory;
    private String mDescription;
    private Calendar mDeadline;
    private int mStatus;
    private UUID mId;

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

    public Calendar getDeadline() {
        return mDeadline;
    }

    public void setDeadline(Calendar deadline) {
        mDeadline = deadline;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public UUID getId() {
        return mId;
    }

    public void setId(@Nullable UUID id) {
        if (id == null) {
            mId = UUID.randomUUID();
        } else {
            mId = id;
        }
    }
}
