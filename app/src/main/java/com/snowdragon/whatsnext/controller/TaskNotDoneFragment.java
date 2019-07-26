package com.snowdragon.whatsnext.controller;

import android.util.Log;
import android.view.Menu;


import com.snowdragon.whatsnext.database.Database;
import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskList;

import java.util.List;

class TaskNotDoneFragment extends AbstractScrollableTaskFragment {

    private static final String TAG = "TaskNotDoneFragment";

    static TaskNotDoneFragment newInstance() {
        return new TaskNotDoneFragment();
    }

    @Override
    void setVisibleMenuOptions(Menu menu) {
        menu.findItem(R.id.menu_show_tasks_not_done).setVisible(false);
        menu.findItem(R.id.menu_show_tasks_done).setVisible(true);
    }

    @Override
    Database.OnDatabaseStateChangeListener initDatabaseRetrievalListener() {
        return new Database.SimpleOnDatabaseStateChangeListener() {
            @Override
            public void onGet(List<Task> task) {
                mTaskList.setTaskList(TaskList.NOT_DONE_LIST, task);
                initAdaptor();
            }
        };
    }

    @Override
    void registerInvokerCommands() {
        super.registerCommonInvokerCommands();
        mInvoker.register("" + R.id.menu_show_tasks_done,
                START_TASK_DONE_FRAGMENT_COMMAND);
    }

    @Override
    String getDatabaseCollectionPath() {
        return Database.TASK_COLLECTION;
    }

    private void initAdaptor() {
        mTaskAdaptor = new TaskAdaptor(
                getActivity(),
                mTaskList.getTaskList(TaskList.NOT_DONE_LIST),
                TaskAdaptor.NOT_DONE_ADAPTOR);
        mTaskRecyclerView.setAdapter(mTaskAdaptor);
        mInvoker.execute("" + R.id.menu_sort_by_deadline_item);
        Log.d(TAG, "Adaptor set for recycler view");
        super.attachSwipeForActionCallback();
    }
}
