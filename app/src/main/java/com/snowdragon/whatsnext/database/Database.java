package com.snowdragon.whatsnext.database;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.snowdragon.whatsnext.model.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Database {

    private static final String TAG = "Database";
    //This path is only for testing purposes only.
    //Implement a root account for developers using  this path.
    private static final String DEV_PATH = "root/dev";
    private static final String RELEASE_PATH = "root/release";
    private static final String USERS_COLLECTION = "/users";
    private static final String TASK_COLLECTION = "/tasks";

    private FirebaseFirestore sFirestore;
    private OnGetAllTasksCompleteListener mAllTasksListener;

    private Database(FirebaseFirestore firestore) {
        sFirestore = firestore;
    }

    public static Database getInstance() {
        return new Database(FirebaseFirestore.getInstance());
    }

    public Database addTaskForUser(FirebaseUser user, Task task) {
        if(user == null || task == null) {
            throw new IllegalArgumentException("Cannot add null user or task");
        }
        String path = constructUsersDatabasePath(
                DEV_PATH, "/" + user.getUid());
        return addTaskByPath(path, task);
    }

    public Database getAllTaskForUser(FirebaseUser user) {
        if(user == null) {
            throw new IllegalArgumentException("Unable to get null user");
        }
        String path = constructUsersDatabasePath(
                DEV_PATH, "/" + user.getUid());
        return getAllTaskByPath(path);
    }

    public Database setOnGetAllTasksCompleteListener(
            OnGetAllTasksCompleteListener listener) {
        mAllTasksListener = listener;
        return this;
    }

    public Database addTaskByPath(String path, Task task) {
        sFirestore
                .collection(path)
                .add(task);
        return this;
    }

    private Database getAllTaskByPath(String path) {
        final List<Task> result = new ArrayList<>();
        sFirestore.collection(path)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot r : task.getResult()) {
                            result.add(r.toObject(Task.class));
                        }
                        mAllTasksListener.allTasks(result);
                    }
                });
        return this;
    }

    private String constructUsersDatabasePath(String rootPath, String userPath) {
        if(userPath == null) {
            return rootPath + USERS_COLLECTION;
        } else {
            return rootPath + USERS_COLLECTION + userPath + TASK_COLLECTION;
        }
    }

    public interface OnGetAllTasksCompleteListener {
        void allTasks(List<Task> tasks);
    }
}
