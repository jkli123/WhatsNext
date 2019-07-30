package com.snowdragon.whatsnext.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.snowdragon.whatsnext.database.Auth;
import com.snowdragon.whatsnext.debug.DebugFragment;

//TODO detail fragment if you switch from not done task to done, the task remains in the list.
//TODO same for from done to some other not done status
//TODO FAB for addition of tasks
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int SIGN_IN_INTENT = 0;

    /*
     * Set this to false if you wish to test for UI elements in app.
     * Currently having this variable as Database elements do not
     * yet support native local firestore instances which thus makes
     * it impossible to write unit tests for the database as it
     * requires the different runtime resources provided by android SDK.
     */
    private static final boolean isDebugRun = false;

    private Auth auth;
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

        initAppBar();
        auth = Auth.getInstance();

        if(isDebugRun) {
            Log.i(TAG, "Running debug run of app");
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .add(R.id.fragment_container, DebugFragment.newInstance())
                    .commit();
        } else {
            Log.i(TAG, "Running actual app instance");

            if (!auth.isCurrentUserSignedIn()) {
                runSignIn();
            } else {
                runSplashFragment();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "SIGN IN FLOW DONE");
        if (requestCode == SIGN_IN_INTENT) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "SIGN IN OK");
                runSplashFragment();
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "SIGN IN NOT OK");
            }
        }
    }

    private void initAppBar() {
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        final DrawerLayout drawer = findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_closer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.main_nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                boolean isSuccessful = false;
                switch(menuItem.getItemId()) {
                    case R.id.main_home_menu:
                        Fragment fragment = TaskNotDoneFragment.newInstance();
                        getSupportFragmentManager()
                                .beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .replace(R.id.fragment_container, fragment)
                                .commit();
                        isSuccessful = true;
                        break;
                    case R.id.main_sign_out_menu:
                        if(auth.isCurrentUserSignedIn()) {
                            auth.signOutCurrentUser();
                            runSignIn();
                        }
                        isSuccessful = true;
                        break;
                }
                drawer.closeDrawers();
                return isSuccessful;
            }
        });
    }

    private void runSplashFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment splashFragment = SplashFragment.newInstance();
        fm.beginTransaction()
                .replace(R.id.fragment_container, splashFragment)
                .commit();
    }

    private void runSignIn() {
        if(auth.isCurrentUserSignedIn()) {
            return;
        }
        Intent signInIntent = Auth.getAuthSignInIntent(Auth.EMAIL_PROVIDER, Auth.GOOGLE_PROVIDER);
        startActivityForResult(signInIntent, SIGN_IN_INTENT);
    }
}
