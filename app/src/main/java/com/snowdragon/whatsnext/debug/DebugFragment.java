package com.snowdragon.whatsnext.debug;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.snowdragon.whatsnext.controller.R;
import com.snowdragon.whatsnext.database.Auth;
import com.snowdragon.whatsnext.database.Database;
import com.snowdragon.whatsnext.model.Task;

import java.util.Date;
import java.util.List;

public class DebugFragment extends Fragment {

    private static final String TAG = "DebugFragment";
    private static final int SIGN_IN = 1;

    private Database mDb = Database.getInstance();
    private Auth mAuth = Auth.getInstance();
    private OnSignInCompleteListener mSignInCompleteListener;

    private TextView mDebugText;

    public static DebugFragment newInstance(){
        return new DebugFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_debug, container, false);
        mDebugText = v.findViewById(R.id.debug_textview);
        //Begin debug test mode.
        initDebugMode();
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //For sign in of dev user.
        if(requestCode == SIGN_IN) {
            if(resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "Successful sign in");
                mSignInCompleteListener.onSignIn(mAuth.getCurrentUser());
                return;
            }
        }
        Log.i(TAG, "Sign in failed");
    }

    private void initDebugMode() {
        //Put any code you wish to debug here. Log to Logcat or display on screen.
        this.setOnSignInCompleteListener(new OnSignInCompleteListener() {
            @Override
            public void onSignIn(FirebaseUser user) {
                //Put code you wish to test here with current user.
//                addTaskWithDebugTaskAndUserAddsSuccessfullyToDatabase(user);
//                getAllTaskForUserReturnsAllTasksInDatabase(user);
                signUserOutSuccessfullySignsUserOutOfDatabase();
            }
        });
        signUserIn();
    }

    private void signUserOutSuccessfullySignsUserOutOfDatabase() {
        FirebaseUser user = mAuth.getCurrentUser();
        mAuth.signOutCurrentUser();
        Log.i(TAG, "User has been signed out");
        //Should throw a PERMISSION DENIED exception
//        getAllTaskForUserReturnsAllTasksInDatabase(user);
        //For some reason writing to database does not throw exception.
        //But it still does not write to the database
//        addTaskWithDebugTaskAndUserAddsSuccessfullyToDatabase(user);
    }

    private void addTaskWithDebugTaskAndUserAddsSuccessfullyToDatabase(
            FirebaseUser user) {
        Task debugTask = getDebugTask();
        mDb.addTaskForUser(user, debugTask);
    }

    private void getAllTaskForUserReturnsAllTasksInDatabase(final FirebaseUser user) {
        mDb.getAllTaskForUser(user).setOnGetAllTasksCompleteListener(new Database.OnGetAllTasksCompleteListener() {
            @Override
            public void allTasks(List<Task> tasks) {
                Log.i(TAG,
                        "User: "
                                + user.getDisplayName()
                                + ", has tasks: "
                                + tasks.toString());
            }
        });
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
        task.setId(null);
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
