package com.snowdragon.whatsnext.controller;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.snowdragon.whatsnext.database.Auth;
import com.snowdragon.whatsnext.database.Database;
import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskComparatorFactory;
import com.snowdragon.whatsnext.model.TaskList;

import java.util.Comparator;
import java.util.List;

//TODO refactor out done list
//TODO refactor out sign out listener?? i have no idea what it is for, its not being called anywhere in this class.
//TODO sort the tasks when they come out of the database. no order when they are set for now.
//TODO consider refactoring out TaskList's sort method, currently it sorts multiple lists and the name of the method does not convey the intention of code
//TODO consider refactoring our sSortField and mTaskComparator variable out of this class. They make code hard to follow.
//TODO as per the first TODO, refactoring the done list should get rid of the methods dealing with swapping of the lists.
public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private static final int SIGN_IN_INTENT = 0;
    public static final int TASKS_NOT_DONE = 0;
    public static final int TASKS_DONE = 1;

    private static FirebaseUser sFirebaseUser;
    private static String sSortField;
    public static int sListType;

    private RecyclerView mTaskRecyclerView;
    private TaskAdaptor mAdapter;
    private Comparator<Task> mTaskComparator;
    private SignOutListener mSignOutListener;

    public static MainFragment getInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /*
     * Callback after menu bar has been inflated
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        if (sListType == TASKS_NOT_DONE) {
            menu.findItem(R.id.menu_show_tasks_not_done).setVisible(false);
        } else if (sListType == TASKS_DONE) {
            menu.findItem(R.id.menu_show_tasks_done).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_task_item:
                runAdditionFragment();
                break;
            case R.id.menu_show_tasks_done:
                setListType(TASKS_DONE);
                restartMainFragment();
                break;
            case R.id.menu_show_tasks_not_done:
                setListType(TASKS_NOT_DONE);
                restartMainFragment();
                break;
            case R.id.menu_sort_item:
                mTaskComparator = null;
                break;
            case R.id.menu_sort_by_name_item:
                sSortField = Task.NAME;
                setTaskComparator(sSortField);
                break;
            case R.id.menu_sort_by_cat_item:
                sSortField = Task.CATEGORY;
                setTaskComparator(sSortField);
                break;
            case R.id.menu_sort_by_status_item:
                sSortField = Task.STATUS;
                setTaskComparator(sSortField);
                break;
            case R.id.menu_sort_by_deadline_item:
                sSortField = Task.DEADLINE;
                setTaskComparator(sSortField);
                break;
        }
        if(mTaskComparator != null) {
            TaskList.get().sort(mTaskComparator);
            mAdapter.updateList(TaskList.get().getTasks());
            mAdapter.notifyDataSetChanged();
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

        // Initialize RecyclerView
        mTaskRecyclerView = view.findViewById(R.id.task_recycler_view);
        mTaskRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setTaskComparator(sSortField = Task.DEADLINE);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        mTaskRecyclerView.addItemDecoration(dividerItemDecoration);

        if (sFirebaseUser == null) {
            sFirebaseUser = Auth.getInstance().getCurrentUser();
            updateAllTasksFromDatabase();
        } else {
            if (sListType == TASKS_NOT_DONE) {
                updateRecyclerViewWithTasksNotDone();
            } else if (sListType == TASKS_DONE) {
                updateRecyclerViewWithTasksDone();
            }
        }
        return view;
    }

    private void runAdditionFragment() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, AdditionFragment.newInstance())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .commit();
    }

    private void restartMainFragment() {
        getActivity().getSupportFragmentManager()
                .popBackStack();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new MainFragment(), "MainFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .commit();
    }

    public MainFragment setSignOutListener(SignOutListener signOutListener) {
        mSignOutListener = signOutListener;
        return this;
    }

    private void setListType(int listType) {
        sListType = listType;
    }

    private void setTaskComparator(String field) {
        switch (sSortField) {
            case Task.NAME:
                mTaskComparator = TaskComparatorFactory.get().getNameComparator();
                break;
            case Task.CATEGORY:
                mTaskComparator = TaskComparatorFactory.get().getCategoryComparator();
                break;
            case Task.DEADLINE:
                mTaskComparator = TaskComparatorFactory.get().getDeadlineComparator();
                break;
            case Task.STATUS:
                mTaskComparator = TaskComparatorFactory.get().getStatusComparator();
                break;
        }
    }

    private void updateAllTasksFromDatabase() {
        updateTaskList();
        updateTaskDoneList();
    }

    /*
     * Load the latest unfinished Tasks from the database.
     */
    private void updateTaskList() {
        Database.getInstance(getActivity())
                .setOnDatabaseStateChangeListener(new Database.OnDatabaseStateChangeListener() {
                    @Override
                    public void onAdd(Task task) { }

                    @Override
                    public void onUpdate(String taskId) { }

                    @Override
                    public void onDelete(Task task) { }

                    @Override
                    public void onGet(List<Task> task) {
                        TaskList.get().setTasks(task);
                        updateRecyclerViewWithTasksNotDone();
                    }
                })
                .getAllTaskForUser(sFirebaseUser, Database.TASK_COLLECTION);
    }

    /*
     * Load the latest Done Tasks from the database.
     */
    private void updateTaskDoneList() {
        Database.getInstance(getActivity())
                .setOnDatabaseStateChangeListener(new Database.OnDatabaseStateChangeListener() {
                    @Override
                    public void onAdd(Task task) { }

                    @Override
                    public void onUpdate(String taskId) { }

                    @Override
                    public void onDelete(Task task) { }

                    @Override
                    public void onGet(List<Task> task) {
                        TaskList.get().setTasksDone(task);
                    }
                })
                .getAllTaskForUser(sFirebaseUser, Database.DONE_COLLECTION);
    }


    /*
     * Sort and attach tasks to the TaskAdaptor in RecyclerView.
     */
    private void updateRecyclerViewWithTasksNotDone() {
        mAdapter = new TaskAdaptor(TaskList.get().getTasks());
        mTaskRecyclerView.setAdapter(mAdapter);
    }

    /*
     * Sort and attach tasksDone to the TaskAdaptor in RecyclerView.
     */
    private void updateRecyclerViewWithTasksDone() {
        mAdapter = new TaskAdaptor(TaskList.get().getTasksDone());
        mTaskRecyclerView.setAdapter(mAdapter);
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
                itemView.setBackgroundColor(getResources().getColor(R.color.colorOverdue));
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

        private List<Task> mTasks;

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

        public void updateList(List<Task> taskList) {
            mTasks = taskList;
        }
    }


    public interface SignOutListener {
        void onSignOut();
    }
}
