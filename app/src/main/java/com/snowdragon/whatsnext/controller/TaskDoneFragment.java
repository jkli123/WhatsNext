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
    List<Task> getAdaptorTaskList() {
        return mTaskList.getTaskList(TaskList.DONE_LIST);
    }

    @Override
    int getAdaptorType() {
        return TaskAdaptor.DONE_ADAPTOR;
    }


    @Override
    void registerInvokerCommands() {
        super.registerCommonInvokerCommands();
        mInvoker
                .register("" + R.id.menu_show_tasks_not_done,
                        START_TASK_NOT_DONE_FRAGMENT_COMMAND);
    }


}
