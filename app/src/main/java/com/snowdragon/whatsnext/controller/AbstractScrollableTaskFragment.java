package com.snowdragon.whatsnext.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskComparatorFactory;
import com.snowdragon.whatsnext.model.TaskList;
import com.snowdragon.whatsnext.patterns.Command;
import com.snowdragon.whatsnext.patterns.Invoker;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public abstract class AbstractScrollableTaskFragment extends Fragment {

     final Command ADD_TASK_MENU_COMMAND = new Command() {
        @Override
        public void execute() {
            AbstractScrollableTaskFragment.this.runFragment(true, AdditionFragment.newInstance());
        }
    };

     final Command START_TASK_DONE_FRAGMENT_COMMAND = new Command() {
         @Override
         public void execute() {
             AbstractScrollableTaskFragment.this.runFragment(false, TaskDoneFragment.newInstance());
         }
     };

     final Command START_TASK_NOT_DONE_FRAGMENT_COMMAND = new Command() {
         @Override
         public void execute() {
             AbstractScrollableTaskFragment.this.runFragment(false, TaskNotDoneFragment.newInstance());
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

    final Command DELETE_MULTISELECT_ITEMS_COMMAND = new Command() {
        @Override
        public void execute() {
            mTaskAdaptor.deleteMultiselectItems();
            setMultiselectModeEnabled(false);
        }
    };

    final Command SET_MULTISELECT_ITEMS_NOT_DONE_COMMAND = new Command() {
        @Override
        public void execute() {
            mTaskAdaptor.updateMultiselectItemsStatus(Task.NOT_DONE);
            setMultiselectModeEnabled(false);
        }
    };

    final Command SET_MULTISELECT_ITEMS_IN_PROGRESS_COMMAND = new Command() {
        @Override
        public void execute() {
            mTaskAdaptor.updateMultiselectItemsStatus(Task.IN_PROGRESS);
            setMultiselectModeEnabled(false);
        }
    };

    final Command SET_MULTISELECT_ITEMS_ON_HOLD_COMMAND = new Command() {
        @Override
        public void execute() {
            mTaskAdaptor.updateMultiselectItemsStatus(Task.ON_HOLD);
            setMultiselectModeEnabled(false);
        }
    };


    final Command SET_MULTISELECT_ITEMS_DONE_COMMAND = new Command() {
        @Override
        public void execute() {
            mTaskAdaptor.updateMultiselectItemsStatus(Task.DONE);
            setMultiselectModeEnabled(false);
        }
    };

    private static final String TAG = "AScrollableTaskFragment";

    Invoker mInvoker = new Invoker();
    RecyclerView mTaskRecyclerView;
    TaskAdaptor mTaskAdaptor;
    TaskList mTaskList = TaskList.get();

    private TaskComparatorFactory mComparatorFactory = new TaskComparatorFactory();
    private ItemTouchHelper mHelper;
    private Menu mMenu;
    private ActionBarDrawerToggle mDrawerToggle;
    private MainActivity mActivity;

    abstract void setFragmentSpecificMenuItemsVisible(Menu menu);
    abstract List<Task> getAdaptorTaskList();
    abstract int getAdaptorType();
    abstract void registerInvokerCommands();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        registerInvokerCommands();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        inflater.inflate(R.menu.menu_main, menu);
        displayMenuItemsForNormalMode();
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
        FloatingActionButton floatingActionButton = getActivity().findViewById(R.id.floating_add_button);
        floatingActionButton.show();
        initRecyclerView(view);
        mActivity = (MainActivity) getActivity();
        mDrawerToggle = mActivity.getActionBarDrawerToggle();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mTaskAdaptor != null) {
            mTaskAdaptor.notifyDataSetChanged();
            Log.d(TAG, "Adaptor notified of data set changed");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        detachSwipeForActionCallback();
    }

    /*
     * Adapts app appearances and widget behaviours based on whether multiselection mode is enabled.
     */
    void setMultiselectModeEnabled(boolean isEnabled) {
        AppBarLayout layout = Objects.requireNonNull(getActivity()).findViewById(R.id.appbar_layout);
        FloatingActionButton floatingActionButton = getActivity().findViewById(R.id.floating_add_button);

        if (isEnabled) {
            mTaskAdaptor.setMultiselectModeEnabled(true);
            detachSwipeForActionCallback();
            displayMenuItemsForMultiselectMode();
            configureDrawerForMultiselectMode();
            floatingActionButton.hide();
            mActivity.setIsToolbarHiddenOnScroll(false);
            layout.setExpanded(true, true);

        } else {
            mTaskAdaptor.setMultiselectModeEnabled(false);
            attachSwipeForActionCallback();
            displayMenuItemsForNormalMode();
            configureDrawerForNormalMode();
            floatingActionButton.show();
            mActivity.setIsToolbarHiddenOnScroll(true);
        }
    }

    void registerCommonInvokerCommands() {
        mInvoker.register("" + R.id.menu_sort_item, Invoker.EMPTY_COMMAND);
        mInvoker.register("" + R.id.menu_sort_by_cat_item, SORT_BY_CATEGORY_COMMAND);
        mInvoker.register("" + R.id.menu_sort_by_name_item, SORT_BY_NAME_COMMAND);
        mInvoker.register("" + R.id.menu_sort_by_deadline_item, SORT_BY_DEADLINE_COMMAND);
        mInvoker.register("" + R.id.menu_sort_by_status_item, SORT_BY_STATUS_COMMAND);
        mInvoker.register("" + R.id.menu_mutltiselect_delete, DELETE_MULTISELECT_ITEMS_COMMAND);
        mInvoker.register("" + R.id.menu_set_not_done, SET_MULTISELECT_ITEMS_NOT_DONE_COMMAND);
        mInvoker.register("" + R.id.menu_set_in_progress, SET_MULTISELECT_ITEMS_IN_PROGRESS_COMMAND);
        mInvoker.register("" + R.id.menu_set_on_hold, SET_MULTISELECT_ITEMS_ON_HOLD_COMMAND);
        mInvoker.register("" + R.id.menu_set_done, SET_MULTISELECT_ITEMS_DONE_COMMAND);
    }

    private void attachSwipeForActionCallback() {
        Log.d(TAG, "Task Holder swipe feature attached");
        if(mTaskAdaptor == null) {
            throw new IllegalStateException("Adaptor has not been set. Callback attachment not allowed");
        }
        mHelper = new ItemTouchHelper(new SwipeForActionCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                mTaskAdaptor
        ));
        mHelper.attachToRecyclerView(mTaskRecyclerView);
    }

    private void detachSwipeForActionCallback() {
        Log.d(TAG, "Task Holder swipe feature detached");
        mHelper.attachToRecyclerView(null);
    }

    private void initRecyclerView(View layout) {
        Log.d(TAG, "Initiating Recycler View");
        mTaskRecyclerView = layout.findViewById(R.id.task_recycler_view);
        mTaskRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration
                = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        mTaskRecyclerView.addItemDecoration(dividerItemDecoration);
        mTaskAdaptor = new TaskAdaptor(
                getActivity(),
                getAdaptorTaskList(),
                getAdaptorType());
        mTaskAdaptor.setItemOnLongClickListener(new TaskAdaptor.ItemOnLongClickListener() {
            @Override
            public void onLongClick() {
                setMultiselectModeEnabled(true);
            }
        });
        mTaskRecyclerView.setAdapter(mTaskAdaptor);
        attachSwipeForActionCallback();
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

    private void runFragment(boolean isBackStack, Fragment fragment) {
        int fadeIn = android.R.anim.fade_in;
        int fadeOut = android.R.anim.fade_out;
        FragmentTransaction transaction = getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(fadeIn, fadeOut, fadeIn, fadeOut)
                .replace(R.id.fragment_container, fragment);
        if(isBackStack) {
            transaction.addToBackStack(null).commit();
        } else {
            transaction.commit();
        }
    }

    private void configureDrawerForMultiselectMode() {
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerToggle.setToolbarNavigationClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setMultiselectModeEnabled(false);
                    }
                }
        );
    }

    private void configureDrawerForNormalMode() {
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.setToolbarNavigationClickListener(
                mActivity.getOriginalDrawerClickListener());
    }

    private void displayMenuItemsForMultiselectMode() {
        setAllMenuItemsVisible(false);
        setMultiselectMenuItemsVisible(true);
    }

    private void displayMenuItemsForNormalMode() {
        setAllMenuItemsVisible(true);
        setMultiselectMenuItemsVisible(false);
        setFragmentSpecificMenuItemsVisible(mMenu);
    }

    private void setAllMenuItemsVisible(boolean isVisible) {
        for (int i = 0; i < mMenu.size(); i++) {
            mMenu .getItem(i).setVisible(isVisible);
        }
    }

    private void setMultiselectMenuItemsVisible(boolean isVisible) {
        mMenu.findItem(R.id.menu_mutltiselect_delete).setVisible(isVisible);
        mMenu.findItem(R.id.menu_set_not_done).setVisible(isVisible);
        mMenu.findItem(R.id.menu_set_in_progress).setVisible(isVisible);
        mMenu.findItem(R.id.menu_set_on_hold).setVisible(isVisible);
        mMenu.findItem(R.id.menu_set_done).setVisible(isVisible);
    }
}
