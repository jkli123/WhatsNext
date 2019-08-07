package com.snowdragon.whatsnext.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;
import com.snowdragon.whatsnext.database.Auth;
import com.snowdragon.whatsnext.debug.DebugFragment;

//TODO Implement search and filter
//TODO TaskViewHolder animation on long click
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
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private View.OnClickListener mOriginalDrawerClickListener;
    private AppBarLayout.LayoutParams mLayoutParams;

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
        initFab();
        initCoordLayoutParams();
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

    /**
     * For running of sign-in flow
     *
     * <p>
     *     When runSignIn() is called, startActivityForResult will be required
     *     to initiate the sign-in process
     * </p>
     *
     */
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
        // Add the toolbar
        mToolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);

        // Initialize drawer
        final DrawerLayout drawer = findViewById(R.id.main_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawer,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_closer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();

        // Initialize the cross that appears when in Multiselect mode
        mDrawerToggle.setHomeAsUpIndicator(getResources()
                .getDrawable(R.drawable.ic_close_white_24dp));

        // Storing original ClickListener for opening drawer
        if (mOriginalDrawerClickListener == null) {
            mOriginalDrawerClickListener = mDrawerToggle.getToolbarNavigationClickListener();
        }
        mDrawerToggle.setToolbarNavigationClickListener(mOriginalDrawerClickListener);

        // Initialize drawer contents in Nav view
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
                            clearBackStack();
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

    private void initFab() {
        findViewById(R.id.floating_add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runAdditionFragment();
            }
        });
    }

    private void initCoordLayoutParams() {
        mLayoutParams = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
    }

    private void runAdditionFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment additionFragment = AdditionFragment.newInstance();
        int fadeInAnimation = android.R.anim.fade_in;
        int fadeOutAnimation = android.R.anim.fade_out;
        fm.beginTransaction()
                .setCustomAnimations(fadeInAnimation, fadeOutAnimation,
                        fadeInAnimation, fadeOutAnimation)
                .replace(R.id.fragment_container, additionFragment)
                .addToBackStack(null)
                .commit();
    }

    private void runSplashFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment splashFragment = SplashFragment.newInstance();
        fm.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
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

    private void clearBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStack();
        }
    }
    ActionBarDrawerToggle getActionBarDrawerToggle() {
        return mDrawerToggle;
    }

    View.OnClickListener getOriginalDrawerClickListener() {
        return mOriginalDrawerClickListener;
    }

    void setIsToolbarHiddenOnScroll(boolean enabled) {
        if (enabled) {
            mLayoutParams.setScrollFlags(
                    AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
                            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL);
        } else {
            mLayoutParams.setScrollFlags(0);
        }
    }
}
