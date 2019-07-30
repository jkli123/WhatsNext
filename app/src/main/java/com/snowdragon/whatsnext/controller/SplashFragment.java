package com.snowdragon.whatsnext.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.snowdragon.whatsnext.database.Auth;
import com.snowdragon.whatsnext.database.Database;
import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskComparatorFactory;
import com.snowdragon.whatsnext.model.TaskList;

import java.util.Comparator;
import java.util.List;

public class SplashFragment extends AbstractStaticFragment {

    private static final String TAG = "SplashFragment";

    private ImageView mLogo;
    private AppBarLayout mAppBarLayout;
    private Database mDatabase;
    private Auth mAuth;
    private OnAllBackgroundActivityCompleteListener mListener;

    public static SplashFragment newInstance() {
        return new SplashFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = Database.getInstance(getActivity());
        mAuth = Auth.getInstance();
        mListener = new OnAllBackgroundActivityCompleteListener() {
            @Override
            public void onAllBackgroundActivityComplete() {
                sortTaskLists();
                finishSplashScreen();
            }
        };
        initUserData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_splash, container, false);
        mLogo = view.findViewById(R.id.splash_image);
        mAppBarLayout = getActivity().findViewById(R.id.appbar_layout);

        hideAppbar();
        hideFab();

        return view;
    }

    private void runScreenFinishAnimation() {
        mLogo.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_right));
        mLogo.getAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "Animation ended");
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.fragment_container, TaskNotDoneFragment.newInstance())
                        .commit();
                mAppBarLayout.setExpanded(true, true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void hideAppbar() {
        mAppBarLayout.setExpanded(false);
    }

    private void hideFab() {
        FloatingActionButton button = getActivity().findViewById(R.id.floating_add_button);
        button.hide();
    }

    private void initUserData() {
        mDatabase.setOnDatabaseStateChangeListener(new Database.SimpleOnDatabaseStateChangeListener() {

            private int mCompleteTransaction = 0;

            @Override
            public void onGet(int type, List<Task> task) {
                if(type == TaskList.DONE_LIST) {
                    Log.d(TAG, "Retrieved done list");
                    TaskList.get().setTaskList(TaskList.DONE_LIST, task);
                    mCompleteTransaction++;
                } else {
                    Log.d(TAG, "Retrieved not done list");
                    TaskList.get().setTaskList(TaskList.NOT_DONE_LIST, task);
                    mCompleteTransaction++;
                }
                if(isAllDataRetrieved(mCompleteTransaction)) {
                    informAllBackgroundActivityCompleteListener();
                }
            }
        })
                .getAllTaskForUser(mAuth.getCurrentUser(), Database.DONE_COLLECTION)
                .getAllTaskForUser(mAuth.getCurrentUser(), Database.TASK_COLLECTION);
    }

    private void finishSplashScreen() {
        Log.d(TAG, "All background tasks finished. Animating");
        runScreenFinishAnimation();
    }

    private void sortTaskLists() {
        TaskComparatorFactory factory = new TaskComparatorFactory();
        Comparator<Task> deadlineComp = factory.getDeadlineComparator();
        TaskList.get().sortTaskList(TaskList.NOT_DONE_LIST, deadlineComp);
        TaskList.get().sortTaskList(TaskList.DONE_LIST, deadlineComp);
    }

    private void informAllBackgroundActivityCompleteListener() {
        if(mListener != null) {
            mListener.onAllBackgroundActivityComplete();
        }
    }

    private boolean isAllDataRetrieved(int transaction) {
        return transaction == 2;
    }

    private interface OnAllBackgroundActivityCompleteListener {
        void onAllBackgroundActivityComplete();
    }
}

