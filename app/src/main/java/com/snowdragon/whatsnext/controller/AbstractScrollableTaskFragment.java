package com.snowdragon.whatsnext.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.snowdragon.whatsnext.database.Auth;
import com.snowdragon.whatsnext.database.Database;
import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskComparatorFactory;
import com.snowdragon.whatsnext.model.TaskList;
import com.snowdragon.whatsnext.patterns.Command;
import com.snowdragon.whatsnext.patterns.Invoker;

import java.util.Comparator;

public abstract class AbstractScrollableTaskFragment extends Fragment {

     final Command ADD_TASK_MENU_COMMAND = new Command() {
        @Override
        public void execute() {
            AbstractScrollableTaskFragment.this.runFragment(null, AdditionFragment.newInstance());
        }
    };

     final Command START_TASK_DONE_FRAGMENT_COMMAND = new Command() {
         @Override
         public void execute() {
             AbstractScrollableTaskFragment.this.runFragment(null, TaskDoneFragment.newInstance());
         }
     };

     final Command START_TASK_NOT_DONE_FRAGMENT_COMMAND = new Command() {
         @Override
         public void execute() {
             AbstractScrollableTaskFragment.this.runFragment(null, TaskNotDoneFragment.newInstance());
         }
     };

     final Command SORT_BY_NAME_COMMAND = new Command() {
         @Override
         public void execute() {
             sortList(verifyAdaptorType(), mComparatorFactory.getNameComparator());
         }
     };

    final Command SORT_BY_STATUS_COMMAND = new Command() {
        @Override
        public void execute() {
            sortList(verifyAdaptorType(), mComparatorFactory.getStatusComparator());
        }
    };
    final Command SORT_BY_DEADLINE_COMMAND = new Command() {
        @Override
        public void execute() {
            sortList(verifyAdaptorType(), mComparatorFactory.getDeadlineComparator());
        }
    };

    final Command SORT_BY_CATEGORY_COMMAND = new Command() {
        @Override
        public void execute() {
            sortList(verifyAdaptorType(), mComparatorFactory.getCategoryComparator());
        }
    };

    private static final String TAG = "AbstractScrollableTaskFragment";

    Invoker mInvoker = new Invoker();
    RecyclerView mTaskRecyclerView;
    TaskAdaptor mTaskAdaptor;
    TaskList mTaskList = TaskList.get();

    private TaskComparatorFactory mComparatorFactory = TaskComparatorFactory.get();

    abstract void setVisibleMenuOptions(Menu menu);
    abstract Database.OnDatabaseStateChangeListener initDatabaseRetrievalListener();
    abstract void registerInvokerCommands();
    abstract String getDatabaseCollectionPath();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        registerInvokerCommands();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        setVisibleMenuOptions(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mInvoker.execute("" + item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        initRecyclerView(view);
        initUserData();

        return view;
    }

    void registerCommonInvokerCommands() {
        mInvoker.register("" + R.id.menu_add_task_item, ADD_TASK_MENU_COMMAND);
        mInvoker.register("" + R.id.menu_sort_item, Invoker.EMPTY_COMMAND);
        mInvoker.register("" + R.id.menu_sort_by_cat_item, SORT_BY_CATEGORY_COMMAND);
        mInvoker.register("" + R.id.menu_sort_by_name_item, SORT_BY_NAME_COMMAND);
        mInvoker.register("" + R.id.menu_sort_by_deadline_item, SORT_BY_DEADLINE_COMMAND);
        mInvoker.register("" + R.id.menu_sort_by_status_item, SORT_BY_STATUS_COMMAND);
    }

    void attachSwipeForActionCallback() {
        if(mTaskAdaptor == null) {
            throw new IllegalStateException("Adaptor has not been set. Callback attachment not allowed");
        }
        ItemTouchHelper helper = new ItemTouchHelper(new SwipeForActionCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                mTaskAdaptor
        ));
        helper.attachToRecyclerView(mTaskRecyclerView);
    }

    private void initRecyclerView(View layout) {
        mTaskRecyclerView = layout.findViewById(R.id.task_recycler_view);
        mTaskRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration
                = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        mTaskRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void initUserData() {
        Database database = Database.getInstance(getActivity());
        FirebaseUser user = Auth.getInstance().getCurrentUser();
        database
                .setOnDatabaseStateChangeListener(initDatabaseRetrievalListener())
                .getAllTaskForUser(user, getDatabaseCollectionPath());
    }

    private int verifyAdaptorType() {
        if(mTaskAdaptor.getType() == TaskAdaptor.DONE_ADAPTOR
            || mTaskAdaptor.getType() == TaskAdaptor.NOT_DONE_ADAPTOR) {
            return mTaskAdaptor.getType();
        }
        throw new IllegalStateException("Adaptor not recognized as done or not done.");
    }

    private void sortList(int listType, Comparator<Task> comparator) {
        mTaskList.sortTaskList(listType, comparator);
        mTaskAdaptor.notifyDataSetChanged();
    }

    private void runFragment(String backStack, Fragment fragment) {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(backStack)
                .commit();
    }
}
