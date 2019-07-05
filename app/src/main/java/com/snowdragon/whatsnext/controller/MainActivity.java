package com.snowdragon.whatsnext.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.snowdragon.whatsnext.database.Auth;
import com.snowdragon.whatsnext.debug.DebugFragment;

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

        if(isDebugRun) {
            Log.i(TAG, "Running debug run of app");
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .add(R.id.fragment_container, DebugFragment.newInstance())
                    .commit();
        } else {
            Log.i(TAG, "Running actual app instance");

            if (!Auth.getInstance().isCurrentUserSignedIn()) {
                startActivityForResult(Auth.getAuthSignInIntent(
                        Auth.EMAIL_PROVIDER,
                        Auth.GOOGLE_PROVIDER),SIGN_IN_INTENT);
            } else {
                runMainFragment();
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
                runMainFragment();
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "SIGN IN NOT OK");
            }
        }
    }

    private void runMainFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment mainFragment = MainFragment.getInstance().setSignOutListener(
                new MainFragment.SignOutListener() {
                    @Override
                    public void onSignOut() {
                        startActivityForResult(Auth.getAuthSignInIntent(
                                Auth.EMAIL_PROVIDER,
                                Auth.GOOGLE_PROVIDER),SIGN_IN_INTENT);
                    }
                });
        fm.beginTransaction()
                .replace(R.id.fragment_container, mainFragment)
                .commit();
    }
}
