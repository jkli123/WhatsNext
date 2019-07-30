package com.snowdragon.whatsnext.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;


import com.snowdragon.whatsnext.database.Database;
import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskList;

import java.util.List;

class TaskNotDoneFragment extends AbstractScrollableTaskFragment {

    private static final String TAG = "TaskNotDoneFragment";
    private static final String TASK_KEY = "task";

    static TaskNotDoneFragment newInstance() {
        return new TaskNotDoneFragment();
    }

    @Override
    void setVisibleMenuOptions(Menu menu) {
        menu.findItem(R.id.menu_show_tasks_not_done).setVisible(false);
        menu.findItem(R.id.menu_show_tasks_done).setVisible(true);
    }

    @Override
    List<Task> getAdaptorTaskList() {
        return mTaskList.getTaskList(TaskList.NOT_DONE_LIST);
    }

    @Override
    int getAdaptorType() {
        return TaskAdaptor.NOT_DONE_ADAPTOR;
    }

    @Override
    void registerInvokerCommands() {
        super.registerCommonInvokerCommands();
        mInvoker.register("" + R.id.menu_show_tasks_done,
                START_TASK_DONE_FRAGMENT_COMMAND);
    }


}
