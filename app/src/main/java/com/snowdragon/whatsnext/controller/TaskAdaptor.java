package com.snowdragon.whatsnext.controller;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TaskAdaptor extends RecyclerView.Adapter<TaskAdaptor.TaskViewHolder> {

    static final int DONE_ADAPTOR = 0;
    static final int NOT_DONE_ADAPTOR = 1;

    private static final String TAG = "TaskAdaptor";
    private static final String DELETE_FROM_DATABASE_COMMAND = "delete";
    private static final String ADD_TO_DATABASE_COMMAND = "add";
    private static final String TRANSFER_FROM_DONE_TO_NOT_DONE_DATABASE_COMMAND = "trfdonetonotdone";
    private static final String TRANSFER_FROM_NOT_DONE_TO_DONE_DATABASE_COMMAND = "trfnotdonetodone";
    private static final String UPDATE_STATUS_FOR_DATABASE = "updatestatus";

    private Database mDatabase;
    private FirebaseUser mUser;
    private Activity mActivity;
    private int mType;
    private List<Task> mTasks;
    private Task mRecentlyDeletedItem;
    private int mRecentlyDeletedItemPosition;
    private int mRecentlyDeletedItemState;
    private Task mRecentlyUpdatedItem;
    private int mRecentlyUpdatedItemOldStatus;
    private int mRecentlyUpdatedItemNewStatus;
    private TaskChange mCurrentTaskChange;
    private Invoker mInvoker;
    private ItemOnLongClickListener mItemOnLongClickListener;
    private Set<Task> mMultiselectItems;
    private boolean mIsMultiselectEnabled;
    private List<TaskViewHolder> mTaskViewHolders;

    TaskAdaptor(Activity activity, List<Task> tasks, int type) {
        mTasks = tasks;
        mActivity = activity;
        mDatabase = Database.getInstance(activity);
        mUser = Auth.getInstance().getCurrentUser();
        mType = type;
        mMultiselectItems = new HashSet<>();
        mIsMultiselectEnabled = false;
        mTaskViewHolders = new ArrayList<>();
        mInvoker = new Invoker();
        registerInvokerCommands();
    }

    @NonNull
    @Override
    public TaskAdaptor.TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        View view = inflater.inflate(R.layout.list_item_task, parent, false);
        TaskViewHolder newTaskViewHolder =  new TaskViewHolder(view);
        mTaskViewHolders.add(newTaskViewHolder);
        return newTaskViewHolder;
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

    /**
     * Delete all items in multi-selection from client side TaskList and from Database.
     */
    void deleteMultiselectItems() {
        Iterator<Task> taskIterator = mMultiselectItems.iterator();
        while (taskIterator.hasNext()) {
            mRecentlyDeletedItem = taskIterator.next();
            mTasks.remove(mRecentlyDeletedItem);
            mInvoker.execute(DELETE_FROM_DATABASE_COMMAND);
        }
        notifyDataSetChanged();
    }

    /**
     * Update status of all items in multi-selection on client side TaskList and on Database.
     *
     * @param status The new status to update the tasks to.
     *
     */
    void updateMultiselectItemsStatus(int status) {
        mCurrentTaskChange = new TaskChange.Builder().updateStatus(status).build();
        mRecentlyUpdatedItemNewStatus = status;
        Iterator<Task> taskIterator = mMultiselectItems.iterator();
        while (taskIterator.hasNext()) {
            mRecentlyUpdatedItemOldStatus = mRecentlyUpdatedItem.getStatus();
            TaskList.get().updateTask(mRecentlyUpdatedItem.getId(), mCurrentTaskChange);
            mInvoker.execute(UPDATE_STATUS_FOR_DATABASE);
        }
        notifyDataSetChanged();
    }

    void itemDone(final int pos) {
         mRecentlyDeletedItemState = mTasks.get(pos).getStatus();
         mRecentlyDeletedItem = mTasks.get(pos);
         mRecentlyDeletedItemPosition = pos;

         updateTaskStatusToDone();

         notifyItemRemoved(pos);
         mInvoker.execute(TRANSFER_FROM_NOT_DONE_TO_DONE_DATABASE_COMMAND);

         showUndoSnackbar("Task Done", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTaskStatusToPreviousStatus();
                transferFromDoneListToNotDoneList();
                notifyItemInserted(mRecentlyDeletedItemPosition);
                mInvoker.execute(TRANSFER_FROM_DONE_TO_NOT_DONE_DATABASE_COMMAND);
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
        TaskChange change = new TaskChange
                .Builder()
                .updateStatus(mRecentlyDeletedItemState)
                .build();
        TaskList.get().updateTask(mRecentlyDeletedItem.getId(), change);
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
        mInvoker.register(TRANSFER_FROM_DONE_TO_NOT_DONE_DATABASE_COMMAND, new Command() {
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
        mInvoker.register(TRANSFER_FROM_NOT_DONE_TO_DONE_DATABASE_COMMAND, new Command() {
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
        mInvoker.register(UPDATE_STATUS_FOR_DATABASE, new Command() {
            @Override
            public void execute() {
                if (mRecentlyUpdatedItemOldStatus != Task.DONE && mRecentlyUpdatedItemNewStatus == Task.DONE) {
                    //Shift from not done list to done list
                    mDatabase.deleteTaskForUser(mUser, mRecentlyUpdatedItem, Database.TASK_COLLECTION);
                    mDatabase.addTaskForUser(mUser, mRecentlyUpdatedItem, Database.DONE_COLLECTION);

                } else if(mRecentlyUpdatedItemOldStatus == Task.DONE && mRecentlyUpdatedItemNewStatus != Task.DONE) {
                    //Shift from done to not done list
                    mDatabase.deleteTaskForUser(mUser, mRecentlyUpdatedItem, Database.DONE_COLLECTION);
                    mDatabase.addTaskForUser(mUser, mRecentlyUpdatedItem, Database.TASK_COLLECTION);

                } else {
                    mDatabase.updateTaskForUser(mUser,
                            mRecentlyUpdatedItem.getId(),
                            mCurrentTaskChange,
                            mType == DONE_ADAPTOR
                                    ? Database.DONE_COLLECTION
                                    : Database.TASK_COLLECTION);
                }
            }
        });
    }

    private void uncheckCheckBoxForAll() {
        int itemCount = mTaskViewHolders.size();
        for (int i = 0; i < itemCount; i++) {
            TaskViewHolder holder = mTaskViewHolders.get(i);
            holder.setCheckBox(false);
        }
    }

    private void displayCheckBoxForAll(boolean isDisplayed) {
        int itemCount = mTaskViewHolders.size();
        for (int i = 0; i < itemCount; i++) {
            TaskViewHolder holder = mTaskViewHolders.get(i);
            holder.displayCheckBox(isDisplayed);
        }
    }

    private void setActionBarTitleAsMultiselectCount() {
        ((AppCompatActivity)mActivity)
                .getSupportActionBar()
                .setTitle("" + mMultiselectItems.size());
    }

    private void setActionBarTitleAsAppName() {
        ((AppCompatActivity)mActivity)
                .getSupportActionBar()
                .setTitle("WhatsNext");
    }

    void setMultiselectModeEnabled(boolean isEnabled) {
        if (isEnabled) {
            mIsMultiselectEnabled = true;
            displayCheckBoxForAll(true);
            setActionBarTitleAsMultiselectCount();
        } else {
            setActionBarTitleAsAppName();
            mIsMultiselectEnabled = false;
            displayCheckBoxForAll(false);
            uncheckCheckBoxForAll();
            mMultiselectItems.clear();
        }
    }

    void setItemOnLongClickListener(ItemOnLongClickListener itemOnLongClickListener) {
        mItemOnLongClickListener = itemOnLongClickListener;
    }

    interface ItemOnLongClickListener {
        void onLongClick();
    }


    class TaskViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        static final float CHECKBOX_MAX_RATIO = (float) 0.15;

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
        private Guideline mGuideline;
        private CheckBox mCheckBox;

        private final TextView mTaskName;
        private final TextView mTaskDeadline;
        private final TextView mStatusText;
        private final ImageView mStatusIcon;
        private final View mForegroundView;
        private final View mLeftBackgroundView;
        private final View mRightBackgroundView;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            mGuideline = itemView.findViewById(R.id.checkbox_guideline);
            mTaskName = itemView.findViewById(R.id.task_name_textview);
            mTaskDeadline = itemView.findViewById(R.id.task_deadline_textview);
            mStatusText = itemView.findViewById(R.id.task_status_text);
            mStatusIcon = itemView.findViewById(R.id.task_status_icon);
            mForegroundView = itemView.findViewById(R.id.foreground_task_block);
            mLeftBackgroundView = itemView.findViewById(R.id.background_swipe_left_task_block);
            mRightBackgroundView = itemView.findViewById(R.id.background_swipe_right_task_block);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            initCheckBox();
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "Clicked on view holder");
            if (mIsMultiselectEnabled) {
                performClickWhenMultiselectEnabled();

            } else {
                Log.d(TAG, "Opened \"" + mTask.getName() +"\" detail fragment");
                if(mActivity instanceof AppCompatActivity) {
                    int fadeIn = android.R.anim.fade_in;
                    int fadeOut = android.R.anim.fade_out;
                    Log.d(TAG, "Clicked on, changing fragment");
                    AppCompatActivity activity = (AppCompatActivity) mActivity;
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(fadeIn, fadeOut, fadeIn, fadeOut)
                            .replace(
                                    R.id.fragment_container,
                                    DetailFragment.newInstance(mTask),
                                    "DetailFragment")
                            .addToBackStack(null)
                            .commit();
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG, "Long Click on \"" + mTask.getName() + "\"");
            if (!mIsMultiselectEnabled) {
                mItemOnLongClickListener.onLongClick();
                addToMultiselectList();
                setActionBarTitleAsMultiselectCount();
            }
            return true;
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
            setCheckBox(isMultiSelected());
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

        private boolean isMultiSelected() {
            return mMultiselectItems.contains(mTask);
        }

        /**
         * CheckBox now behaves like the TaskViewHolder on click.
         */
        private void initCheckBox() {
            mCheckBox = itemView.findViewById(R.id.task_checkbox);
            displayCheckBox(mIsMultiselectEnabled);
            mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performClickWhenMultiselectEnabled();
                }
            });
        }

        private boolean performClickWhenMultiselectEnabled() {
            if (isMultiSelected()) {
                removeFromMultiselectList();
            } else {
                addToMultiselectList();
            }
            setActionBarTitleAsMultiselectCount();

            // Disable multiselect mode when selection count falls to zero
            if (mMultiselectItems.isEmpty()) {
                setMultiselectModeEnabled(false);
                Fragment fragment = ((AppCompatActivity)mActivity)
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                ((AbstractScrollableTaskFragment) fragment)
                        .setMultiselectModeEnabled(false);
            }

            return true;
        }

        private void setCheckBox(boolean isChecked) {
            mCheckBox.setChecked(isChecked);
        }

        private void displayCheckBox(boolean isMultiselectEnabled) {
            float ratio = isMultiselectEnabled ? CHECKBOX_MAX_RATIO : 0;
            mGuideline.setGuidelinePercent(ratio);
            mCheckBox.setVisibility(isMultiselectEnabled ? View.VISIBLE : View.INVISIBLE);
        }

        private void addToMultiselectList() {
            Log.d(TAG, "Added \"" + mTask.getName() +"\" to multiselect list");
            mMultiselectItems.add(mTask);
            mCheckBox.setChecked(true);
        }

        private void removeFromMultiselectList() {
            Log.d(TAG, "Removed \"" + mTask.getName() +"\" from multiselect list");
            mMultiselectItems.remove(mTask);
            mCheckBox.setChecked(false);
        }
    }
}
