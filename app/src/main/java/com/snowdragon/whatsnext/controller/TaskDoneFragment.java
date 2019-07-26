package com.snowdragon.whatsnext.controller;

import android.util.Log;
import android.view.Menu;

import com.snowdragon.whatsnext.database.Database;
import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskList;

import java.util.List;

public class TaskDoneFragment extends AbstractScrollableTaskFragment {

    private static final String TAG = "TaskDoneFragment";

    public static TaskDoneFragment newInstance() {
        return new TaskDoneFragment();
    }

    @Override
    void setVisibleMenuOptions(Menu menu) {
        menu.findItem(R.id.menu_show_tasks_not_done).setVisible(true);
        menu.findItem(R.id.menu_show_tasks_done).setVisible(false);
    }

    @Override
    Database.OnDatabaseStateChangeListener initDatabaseRetrievalListener() {
        return new Database.SimpleOnDatabaseStateChangeListener() {
            @Override
            public void onGet(List<Task> task) {
                mTaskList.setTaskList(TaskList.DONE_LIST, task);
                initAdaptor();
            }
        };
    }

    @Override
    void registerInvokerCommands() {
        super.registerCommonInvokerCommands();
        mInvoker
                .register("" + R.id.menu_show_tasks_not_done,
                        START_TASK_NOT_DONE_FRAGMENT_COMMAND);
    }

    @Override
    String getDatabaseCollectionPath() {
        return Database.DONE_COLLECTION;
    }

    private void initAdaptor() {
        mTaskAdaptor = new TaskAdaptor(
                getActivity(),
                mTaskList.getTaskList(TaskList.DONE_LIST),
                TaskAdaptor.DONE_ADAPTOR);
        mTaskRecyclerView.setAdapter(mTaskAdaptor);
        mInvoker.execute("" + R.id.menu_sort_by_deadline_item);
        Log.d(TAG, "Adaptor set for recycler view");
        super.attachSwipeForActionCallback();
    }
}
