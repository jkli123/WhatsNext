package com.snowdragon.whatsnext.controller;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowdragon.whatsnext.database.Auth;
import com.snowdragon.whatsnext.database.Database;
import com.snowdragon.whatsnext.model.Task;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    /**
     * Initialization of Main Activity by launching FragmentManager.
     *
     * <p>
     *     After launching FragmentManager, MainFragment will be launched
     *     by default. It displays a RecyclerView of all existing tasks
     *     yet to be completed.
     * </p>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = new MainFragment();
//        Fragment fragment = new DetailFragment();
        fm.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit();


    }
}
