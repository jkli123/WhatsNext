package com.snowdragon.whatsnext.database;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.snowdragon.whatsnext.model.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Database {

    private static final String TAG = "Database";
    private static final String DEV_PATH = "dev/";
    private static final String USERS_PATH = "users/";

    private static FirebaseFirestore sFirestore;


    private Database() {

    }

    public static Database getInstance() {
        if(sFirestore == null) {
            sFirestore = FirebaseFirestore.getInstance();
        }
        return new Database();
    }

    public void addTask(Task task) {
        sFirestore.collection(DEV_PATH).add(task);
    }

    public List<Task> getAllTask() {
        List<Task> result = new ArrayList<>();
        sFirestore.collection(DEV_PATH).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                for(QueryDocumentSnapshot r : task.getResult()) {
                    Log.i(TAG, r.toObject(Task.class).toString());
                }
            }
        });
        return result;
    }
}
