package com.snowdragon.whatsnext.controller;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.snowdragon.whatsnext.database.Auth;
import com.snowdragon.whatsnext.database.Database;
import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskList;

import java.util.List;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private static final int SIGN_IN_INTENT = 0;
    private RecyclerView mTaskRecyclerView;
    private TaskAdaptor mTaskAdaptor;
    private MainActivity mMainActivity;
    private static FirebaseUser sFirebaseUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /*
     * This method allows you to make specific changes to menu item properties. For example, you
     * may wish to set the visibility of particular menu items to false
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_task_item:
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, AdditionFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
                break;

            // TODO: Drawer on Appbar to provide sign-out option
            case R.id.menu_sign_out_item:
                Auth.getInstance().signOutCurrentUser();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .remove(this)
                        .commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * Inflation of MainFragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mTaskRecyclerView = view.findViewById(R.id.task_recycler_view);
        mTaskRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        mTaskAdaptor = new TaskAdaptor(TaskList.get().getTasks());

        Database.OnDatabaseStateChangeListener databaseStateChangeListener =
                new Database.OnDatabaseStateChangeListener() {
                    @Override
                    public void onAdd(Task task) { }

                    @Override
                    public void onUpdate(String taskId) { }

                    @Override
                    public void onDelete(Task task) { }

                    @Override
                    public void onGet(List<Task> task) {
                        TaskList.get().setTasks(task);
                        // This line is necessary to update Adaptor data on first authentication
                        mTaskRecyclerView.setAdapter(new TaskAdaptor(TaskList.get().getTasks()));
                    }
                };

        if (sFirebaseUser == null) {
            sFirebaseUser = Auth.getInstance().getCurrentUser();
            Database.getInstance(getActivity())
                    .setOnDatabaseStateChangeListener(databaseStateChangeListener)
                    .getAllTaskForUser(sFirebaseUser);
        } else {
            mTaskRecyclerView.setAdapter(new TaskAdaptor(TaskList.get().getTasks()));
            // mTaskAdaptor = new TaskAdaptor(TaskList.get().getTasks());
            // Not used because it changes the order of the items
        }


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMainActivity.runSignInFlow();
    }

    private class TaskHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private final TextView mTaskName;
        private final TextView mTaskDeadline;
        private Task mTask;

        public TaskHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_task, parent, false));
            itemView.setOnClickListener(this);
            mTaskName = itemView.findViewById(R.id.task_name_textview);
            mTaskDeadline = itemView.findViewById(R.id.task_deadline_textview);
        }

        public void bind(Task task) {
            mTask = task;
            mTaskName.setText(task.getName());
            mTaskDeadline.setText(DateFormat.format("dd/MM/yyyy",task.getDeadline().getTime()));
            if (task.isOverdue()) {
                mTaskDeadline.setTextColor(getResources().getColor(R.color.colorOverdue));
            }
        }

        /*
         * Overriding the the onClick method for the ViewHolder.
         *
         * <p>
         *     Replaces the MainFragment with the DetailFragment
         *     of the selected Task.
         * </p>
         */
        @Override
        public void onClick(View v) {
            DetailFragment detailFragment = DetailFragment.newInstance(mTask);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment, "DetailFragment")
                    .addToBackStack(null)
                    .commit();
        }
    }

    private class TaskAdaptor extends RecyclerView.Adapter<TaskHolder> {

        private final List<Task> mTasks;

        public TaskAdaptor(List<Task> tasks) {
            mTasks = tasks;
        }

        @NonNull
        @Override
        public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new TaskHolder(layoutInflater, parent);
        }

        // To be kept efficient for faster loading as you scroll
        @Override
        public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
            Task task = mTasks.get(position);
            holder.bind(task);
        }

        @Override
        public int getItemCount() {
            return mTasks.size();
        }
    }
}
