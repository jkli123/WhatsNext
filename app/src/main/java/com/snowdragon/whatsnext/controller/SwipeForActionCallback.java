package com.snowdragon.whatsnext.controller;

import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeForActionCallback extends ItemTouchHelper.SimpleCallback {

    private static final String TAG = "SwipeForActionCallback";

    private TaskAdaptor mTaskAdaptor;

    public SwipeForActionCallback(int dragDirs, int swipeDirs, TaskAdaptor adaptor) {
        super(dragDirs, swipeDirs);
        mTaskAdaptor = adaptor;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        if(mTaskAdaptor.getType() == TaskAdaptor.DONE_ADAPTOR) {
            mTaskAdaptor.deleteItemWithUndo(position, "Deleted");
        } else {
            if(direction == ItemTouchHelper.LEFT) {
                mTaskAdaptor.deleteItemWithUndo(position, "Deleted");
            } else if(direction == ItemTouchHelper.RIGHT) {
                mTaskAdaptor.itemDone(position);
            }
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        final View foregroundView = ((TaskAdaptor.TaskViewHolder) viewHolder).getForegroundView();
        selectBackgroundView(viewHolder, dX);
        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((TaskAdaptor.TaskViewHolder) viewHolder).getForegroundView();
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((TaskAdaptor.TaskViewHolder) viewHolder).getForegroundView();
        getDefaultUIUtil().clearView(foregroundView);
    }


    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        if(viewHolder != null) {
            final View foregroundView = ((TaskAdaptor.TaskViewHolder) viewHolder).getForegroundView();
            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    private boolean isSwipeRight(float dX) {
        return dX > 0;
    }

    private void selectBackgroundView(RecyclerView.ViewHolder viewHolder, float dX) {
        TaskAdaptor.TaskViewHolder taskHolder = (TaskAdaptor.TaskViewHolder) viewHolder;
        if(mTaskAdaptor.getType() == TaskAdaptor.DONE_ADAPTOR) {
            showDeleteBackground(taskHolder);
        } else {
            if(isSwipeRight((dX))) {
                showDoneBackground(taskHolder);
            } else {
                showDeleteBackground(taskHolder);
            }
        }
    }

    private void showDeleteBackground(TaskAdaptor.TaskViewHolder taskHolder) {
        taskHolder.getLeftBackgroundView().setVisibility(View.VISIBLE);
        taskHolder.getRightBackgroundView().setVisibility(View.GONE);
    }

    private void showDoneBackground(TaskAdaptor.TaskViewHolder taskHolder) {
        taskHolder.getLeftBackgroundView().setVisibility(View.GONE);
        taskHolder.getRightBackgroundView().setVisibility(View.VISIBLE);
    }
}
