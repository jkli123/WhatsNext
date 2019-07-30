package com.snowdragon.whatsnext.debug;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseUser;
import com.snowdragon.whatsnext.controller.R;
import com.snowdragon.whatsnext.controller.SplashFragment;
import com.snowdragon.whatsnext.database.Auth;
import com.snowdragon.whatsnext.database.Database;
import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskChange;

import java.util.Date;

public class DebugFragment extends Fragment {

    private static final String TAG = "DebugFragment";
    private static final int SIGN_IN = 1;

    //static valid uuid that never changes for testing purposes.
    private static final String VALID_UUID = "9095b520-2185-4cca-a26d-5cdc566ce867";

    private Database mDb;
    private final Auth mAuth = Auth.getInstance();
    private OnSignInCompleteListener mSignInCompleteListener;
    private Database.OnDatabaseStateChangeListener mListener;

    private TextView mDebugText;

    public static DebugFragment newInstance(){
        return new DebugFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_debug, container, false);
        mDebugText = v.findViewById(R.id.debug_textview);
        mDebugText.setAllCaps(true);
        mDb = Database.getInstance(getActivity());
        mListener = new DebugDatabaseListener();
        //Begin debug test mode.
        initDebugMode();
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //For sign in of dev user.
        if(requestCode == SIGN_IN) {
            if(resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Successful sign in");
                mSignInCompleteListener.onSignIn(mAuth.getCurrentUser());
                return;
            }
        }
        Log.d(TAG, "Sign in failed");
    }

    private void initDebugMode() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, SplashFragment.newInstance())
                .commit();
        //Put any code you wish to debug here. Log to Logcat or display on screen.
//        this.setOnSignInCompleteListener(new OnSignInCompleteListener() {
//            @Override
//            public void onSignIn(FirebaseUser user) {
                //Put code you wish to test here with current user.
//                addTaskWithDebugTaskAndUserAddsSuccessfullyToDatabase(user);
//                getAllTaskForUserReturnsAllTasksInDatabase(user);
//                updateDummyTaskWithNewNameSuccessfullyUpdatesDatabase();
//                deleteDummyTaskForUserSuccessfullyDeletesTask();
//            }
//        });
//        mDb.setOnDatabaseStateChangeListener(mListener);
//        signUserIn();
    }

    private void updateDummyTaskWithNewNameSuccessfullyUpdatesDatabase() {
        FirebaseUser user = mAuth.getCurrentUser();
        TaskChange change = new TaskChange.Builder()
                .updateName("NewDummyName")
                .build();
        mDb.updateTaskForUser(user, VALID_UUID, change, Database.DONE_COLLECTION);
    }

    private void updateDummyTaskWithNewCategorySuccessfullyUpdatesDatabase() {
        FirebaseUser user = mAuth.getCurrentUser();
        TaskChange change = new TaskChange.Builder()
                .updateCategory("NewDummyCategory")
                .build();
        mDb.updateTaskForUser(user, VALID_UUID, change, Database.DONE_COLLECTION);
    }

    private void updateDummyTaskWithNewDescriptionSuccessfullyUpdatesDatabase() {
        FirebaseUser user = mAuth.getCurrentUser();
        TaskChange change = new TaskChange.Builder()
                .updateDescription("NewDummyDescription")
                .build();
        mDb.updateTaskForUser(user, VALID_UUID, change, Database.DONE_COLLECTION);
    }

    private void updateDummyTaskWithNewStatusSuccessfullyUpdatesDatabase() {
        FirebaseUser user = mAuth.getCurrentUser();
        TaskChange change = new TaskChange.Builder()
                .updateStatus(0)
                .build();
        mDb.updateTaskForUser(user, VALID_UUID, change, Database.DONE_COLLECTION);
    }

    private void updateDummyTaskWithNewDeadlineSuccessfullyUpdatesDatabase() {
        FirebaseUser user = mAuth.getCurrentUser();
        TaskChange change = new TaskChange.Builder()
                .updateDeadline(new Date())
                .build();
        mDb.updateTaskForUser(user, VALID_UUID, change, Database.DONE_COLLECTION);
    }

    private void deleteDummyTaskForUserSuccessfullyDeletesTask() {
        FirebaseUser user = mAuth.getCurrentUser();
        Task task = getDebugTask();
        mDb.deleteTaskForUser(user, task, Database.DONE_COLLECTION);
    }

    private void signUserOutSuccessfullySignsUserOutOfDatabase() {
        FirebaseUser user = mAuth.getCurrentUser();
        mAuth.signOutCurrentUser();
        Log.i(TAG, "User has been signed out");
    }

    private void addTaskWithDebugTaskAndUserAddsSuccessfullyToDatabase(
            FirebaseUser user) {
        Task debugTask = getDebugTask();
        mDb.addTaskForUser(user, debugTask, Database.DONE_COLLECTION);
    }

    private void getAllTaskForUserReturnsAllTasksInDatabase(final FirebaseUser user) {
        mDb.getAllTaskForUser(user, Database.DONE_COLLECTION);
    }

    private void signUserIn() {
        if(mAuth.isCurrentUserSignedIn()) {
            Log.i(TAG, "Current user is signed in");
            mSignInCompleteListener.onSignIn(mAuth.getCurrentUser());
        } else {
            Log.i(TAG, "User not signed in. Initiating sign in");
            Intent intent =
                    Auth
                            .getAuthSignInIntent(
                                    Auth.GOOGLE_PROVIDER,
                                    Auth.EMAIL_PROVIDER);
            //Initiate sign in flow for firebase.
            startActivityForResult(intent, SIGN_IN);
        }
    }

    private Task getDebugTask() {
        //Dummy task
        Task task = new Task();
        task.setName("This is a debug task");
        task.setDescription("This is a debug description");
        task.setDeadline(new Date());
        task.setStatus(Task.ON_HOLD);
        task.setId(VALID_UUID);
        task.setCategory(Task.STUDY_CATEGORY);
        return task;
    }

    private void setOnSignInCompleteListener(OnSignInCompleteListener listener) {
        //FOr sign in purposes
        mSignInCompleteListener = listener;
    }

    private interface OnSignInCompleteListener {
        void onSignIn(FirebaseUser user);
    }
}
