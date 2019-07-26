package com.snowdragon.whatsnext.controller;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.snowdragon.whatsnext.database.Auth;
import com.snowdragon.whatsnext.database.Database;
import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskChange;
import com.snowdragon.whatsnext.model.TaskList;
import com.snowdragon.whatsnext.patterns.Command;
import com.snowdragon.whatsnext.patterns.Invoker;

import java.util.List;

public class TaskAdaptor extends RecyclerView.Adapter<TaskAdaptor.TaskViewHolder> {

    public static final int DONE_ADAPTOR = 0;
    public static final int NOT_DONE_ADAPTOR = 1;

    private static final String TAG = "TaskAdaptor";
    private static final String DELETE_FROM_DATABASE_COMMAND = "delete";
    private static final String ADD_TO_DATABASE_COMMAND = "add";
    private static final String TRANFER_FROM_DONE_TO_NOT_DONE_DATABASE_COMMAND = "trfdonetonotdone";
    private static final String TRANFER_FROM_NOT_DONE_TO_DONE_DATABASE_COMMAND = "trfnotdonetodone";

    private Database mDatabase;
    private FirebaseUser mUser;
    private Activity mActivity;
    private int mType;
    private List<Task> mTasks;
    private Task mRecentlyDeletedItem;
    private int mRecentlyDeletedItemPosition;
    private int mRecentlyDeletedItemState;
    private Invoker mInvoker;

     TaskAdaptor(Activity activity, List<Task> tasks, int type) {
        mTasks = tasks;
        mActivity = activity;
        mDatabase = Database.getInstance(activity);
        mUser = Auth.getInstance().getCurrentUser();
        mType = type;
        mInvoker = new Invoker();
        registerInvokerCommands();
    }

    @NonNull
    @Override
    public TaskAdaptor.TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View view = inflater.inflate(R.layout.list_item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskAdaptor.TaskViewHolder holder, int position) {
        Task task = mTasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    public int getType() { return mType; }

    void deleteItemWithUndo(int pos, String undoText) {
        mRecentlyDeletedItem = mTasks.remove(pos);
        mRecentlyDeletedItemPosition = pos;
        mInvoker.execute(DELETE_FROM_DATABASE_COMMAND);
        notifyItemRemoved(pos);
        showUndoSnackbar(undoText, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoDelete();
            }
        });
    }

    void itemDone(final int pos) {
         mRecentlyDeletedItemState = mTasks.get(pos).getStatus();
         mRecentlyDeletedItem = mTasks.get(pos);
         mRecentlyDeletedItemPosition = pos;

         updateTaskStatusToDone();

         mTasks.remove(pos);
         notifyItemRemoved(pos);
         mInvoker.execute(TRANFER_FROM_NOT_DONE_TO_DONE_DATABASE_COMMAND);

         showUndoSnackbar("Task Done", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTaskStatusToPreviousStatus();
                transferFromDoneListToNotDoneList();
                notifyItemInserted(mRecentlyDeletedItemPosition);
                mInvoker.execute(TRANFER_FROM_DONE_TO_NOT_DONE_DATABASE_COMMAND);
            }
        });
    }

    private void showUndoSnackbar(String infoText, View.OnClickListener undoListener) {
        View coordinatorLayout = mActivity.findViewById(R.id.fragment_layout_coordinator);
        Snackbar snackbar = Snackbar.make(coordinatorLayout, infoText, Snackbar.LENGTH_LONG);
        snackbar.setAction("undo", undoListener);
        snackbar.show();
    }

    private void undoDelete() {
        mTasks.add(mRecentlyDeletedItemPosition, mRecentlyDeletedItem);
        mInvoker.execute(ADD_TO_DATABASE_COMMAND);
        notifyItemInserted(mRecentlyDeletedItemPosition);
    }

    private void updateTaskStatusToDone() {
        TaskChange change = new TaskChange
                .Builder()
                .updateStatus(Task.DONE)
                .build();
        TaskList.get().updateTask(mRecentlyDeletedItem.getId(), change);
    }

    private void updateTaskStatusToPreviousStatus() {
        new TaskChange
                .Builder()
                .updateStatus(mRecentlyDeletedItemState)
                .build()
                .updateTask(mRecentlyDeletedItem);
    }

    private void transferFromDoneListToNotDoneList() {
        TaskList.get().deleteTaskById(mRecentlyDeletedItem.getId());
        mTasks.add(mRecentlyDeletedItemPosition, mRecentlyDeletedItem);
    }

    private void registerInvokerCommands() {
        mInvoker.register(DELETE_FROM_DATABASE_COMMAND, new Command() {
            @Override
            public void execute() {
                mDatabase.deleteTaskForUser(
                        mUser,
                        mRecentlyDeletedItem,
                        mType == DONE_ADAPTOR
                                ? Database.DONE_COLLECTION
                                : Database.TASK_COLLECTION);
            }
        });
        mInvoker.register(ADD_TO_DATABASE_COMMAND, new Command() {
            @Override
            public void execute() {
                mDatabase.addTaskForUser(
                        mUser,
                        mRecentlyDeletedItem,
                        mType == DONE_ADAPTOR
                                ? Database.DONE_COLLECTION
                                : Database.TASK_COLLECTION);
            }
        });
        mInvoker.register(TRANFER_FROM_DONE_TO_NOT_DONE_DATABASE_COMMAND, new Command() {
            @Override
            public void execute() {
                mDatabase.deleteTaskForUser(mUser,
                        mRecentlyDeletedItem,
                        Database.DONE_COLLECTION);
                mDatabase.addTaskForUser(mUser,
                        mRecentlyDeletedItem,
                        Database.TASK_COLLECTION);
            }
        });
        mInvoker.register(TRANFER_FROM_NOT_DONE_TO_DONE_DATABASE_COMMAND, new Command() {
            @Override
            public void execute() {
                mDatabase.deleteTaskForUser(mUser,
                        mRecentlyDeletedItem,
                        Database.TASK_COLLECTION);
                mDatabase.addTaskForUser(mUser,
                        mRecentlyDeletedItem,
                        Database.DONE_COLLECTION);
            }
        });
    }

    class TaskViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private final Drawable mNotDoneIcon = mActivity.getResources()
                .getDrawable(R.drawable.ic_not_done_160px, null);
        private final Drawable mInProgIcon = mActivity.getResources()
                .getDrawable(R.drawable.ic_in_progress_160px, null);
        private final Drawable mOnHoldIcon = mActivity.getResources()
                .getDrawable(R.drawable.ic_pause_160px, null);
        private final Drawable mDoneIcon = mActivity.getResources()
                .getDrawable(R.drawable.ic_done_160px, null);
        private final String mNotDoneText = mActivity.getResources()
                .getString(R.string.not_done);
        private final String mInProgText = mActivity.getResources()
                .getString(R.string.in_progress_short_form);
        private final String mOnHoldText = mActivity.getResources()
                .getString(R.string.on_hold);
        private final String mDoneText = mActivity.getResources()
                .getString(R.string.done);
        private final int mRedForegroundColor = mActivity.getResources()
                .getColor(R.color.colorOverdue);
        private final int mGrayForegroundColor = mActivity.getResources()
                .getColor(R.color.colorItemListNotOverdueText);

        private Task mTask;
        private final TextView mTaskName;
        private final TextView mTaskDeadline;
        private final TextView mStatusText;
        private final ImageView mStatusIcon;
        private final View mForegroundView;
        private final View mLeftBackgroundView;
        private final View mRightBackgroundView;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            mTaskName = itemView.findViewById(R.id.task_name_textview);
            mTaskDeadline = itemView.findViewById(R.id.task_deadline_textview);
            mStatusText = itemView.findViewById(R.id.task_status_text);
            mStatusIcon = itemView.findViewById(R.id.task_status_icon);
            mForegroundView = itemView.findViewById(R.id.foreground_task_block);
            mLeftBackgroundView = itemView.findViewById(R.id.background_swipe_left_task_block);
            mRightBackgroundView = itemView.findViewById(R.id.background_swipe_right_task_block);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "Clicked on view holder");
            if(mActivity instanceof AppCompatActivity) {
                Log.d(TAG, "Clicked on, changing fragment");
                AppCompatActivity activity = (AppCompatActivity) mActivity;
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.fragment_container,
                                DetailFragment.newInstance(mTask),
                                "DetailFragment")
                        .addToBackStack(null)
                        .commit();
            }
        }

        View getForegroundView() {
            return mForegroundView;
        }

        View getLeftBackgroundView() {
            return mLeftBackgroundView;
        }

        View getRightBackgroundView() {
            return mRightBackgroundView;
        }

        void bind(Task task) {
            mTask = task;
            mTaskName.setText(task.getName());
            mTaskDeadline.setText(
                    DateFormat
                            .format("dd/MM/yyyy",task.getDeadline().getTime()));
            setDateColour();
            setStatusIcon(task.getStatus());
        }

        private void setStatusIcon(int status) {
            switch(status) {
                case Task.DONE :
                    mStatusIcon.setImageDrawable(mDoneIcon);
                    mStatusText.setText(mDoneText);
                    break;
                case Task.IN_PROGRESS :
                    mStatusIcon.setImageDrawable(mInProgIcon);
                    mStatusText.setText(mInProgText);
                    break;
                case Task.ON_HOLD :
                    mStatusIcon.setImageDrawable(mOnHoldIcon);
                    mStatusText.setText(mOnHoldText);
                    break;
                case Task.NOT_DONE :
                    mStatusIcon.setImageDrawable(mNotDoneIcon);
                    mStatusText.setText(mNotDoneText);
                    break;
                default :
                    throw new IllegalArgumentException("Task status code not recognized");
            }
        }

        private void setDateColour() {
            if(mTask.isOverdue()) {
                mTaskDeadline.setTypeface(null, Typeface.BOLD);
                mTaskDeadline.setTextColor(mRedForegroundColor);
            } else {
                mTaskDeadline.setTypeface(null);
                mTaskDeadline.setTextColor(mGrayForegroundColor);
            }
        }
    }
}
