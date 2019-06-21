package com.snowdragon.whatsnext.model;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class TaskTest {

    private final Task mTaskTest = new Task();
    private final UUID mIdDummy = UUID.randomUUID();

    @Test
    public void setIdRandomUuidStringReturnsSameUuidString() {
        mTaskTest.setId(mIdDummy.toString());
        assertEquals(mIdDummy.toString(), mTaskTest.getId());
    }
}