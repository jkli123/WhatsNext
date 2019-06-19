package com.snowdragon.whatsnext.model;

import android.support.annotation.Nullable;

import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
    private Date mDeadline;
    private int mStatus;
    private String mId;

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

    public String toString() {
        return "[Name: " + mName +
                ", Category: " + mCategory +
                ", Description: " + mDescription +
                ", Status: " + mStatus +
                ", Deadline: " + mDeadline +
                ", UUID: " + mId + "]";
    }
}
