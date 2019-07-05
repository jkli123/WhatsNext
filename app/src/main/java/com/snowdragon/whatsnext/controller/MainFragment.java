package com.snowdragon.whatsnext.controller;

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

public class MainFragment extends Fragment {


    private static final String TAG = "MainFragment";
    private static final int SIGN_IN_INTENT = 0;
    public static final int TASKS_NOT_DONE = 0;
    public static final int TASKS_DONE = 1;

    private static FirebaseUser sFirebaseUser;
    private static String sSortField;
    public static int sListType;

    private RecyclerView mTaskRecyclerView;
    private TaskAdaptor mTaskAdaptor;
    private MainActivity mMainActivity;
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
        MainFragment fragment;
        switch (item.getItemId()) {
            case R.id.menu_add_task_item:
                runAdditionFragment();
                break;

            case R.id.menu_sort_item:
                runSortFragment();
                break;

            case R.id.menu_show_tasks_done:
                setListType(TASKS_DONE);
                restartMainFragment();
                break;

            case R.id.menu_show_tasks_not_done:
                setListType(TASKS_NOT_DONE);
                restartMainFragment();
                break;


            case R.id.menu_sign_out_item:
                runSignOutFlow();
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

        // Initialize RecyclerView
        mTaskRecyclerView = view.findViewById(R.id.task_recycler_view);
        mTaskRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Initialize Sorting order for RecyclerView items
        if (sSortField == null) {
            setSortField(Task.DEADLINE);
        }

        setTaskComparator(sSortField);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        mTaskRecyclerView.addItemDecoration(dividerItemDecoration);
//        mTaskAdaptor = new TaskAdaptor(TaskList.get().getTasks());

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

    private void runSortFragment() {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, SortFragment.newInstance(sSortField))
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

    private void runSignOutFlow() {
        Auth.getInstance().signOutCurrentUser();
        if (mSignOutListener == null) {
            setSignOutListener(
                    new MainFragment.SignOutListener() {
                        @Override
                        public void onSignOut() {
                            startActivityForResult(Auth.getAuthSignInIntent(
                                    Auth.EMAIL_PROVIDER,
                                    Auth.GOOGLE_PROVIDER), SIGN_IN_INTENT);
                        }
                    });
        }
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .remove(this)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        mSignOutListener.onSignOut();
    }

    public MainFragment setSignOutListener(SignOutListener signOutListener) {
        mSignOutListener = signOutListener;
        return this;
    }

    private void setListType(int listType) {
        sListType = listType;
    }

    public static void setSortField(String field) {
        sSortField = field;
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
        mTaskRecyclerView.setAdapter(
                new TaskAdaptor(TaskList.get().sort(mTaskComparator).getTasks()));
    }

    /*
     * Sort and attach tasksDone to the TaskAdaptor in RecyclerView.
     */
    private void updateRecyclerViewWithTasksDone() {
        mTaskRecyclerView.setAdapter(
                new TaskAdaptor(TaskList.get().sort(mTaskComparator).getTasksDone()));
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


    public interface SignOutListener {
        void onSignOut();
    }
}
