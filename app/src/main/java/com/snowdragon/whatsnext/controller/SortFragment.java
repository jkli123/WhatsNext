package com.snowdragon.whatsnext.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.snowdragon.whatsnext.database.Auth;
import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskComparatorFactory;
import com.snowdragon.whatsnext.model.TaskList;

public class SortFragment extends Fragment {
    public static String TAG = "SortFragment";
    public static String KEY = "FIELD";
    private static int SIGN_IN_INTENT = 0;

    public static String currentSortField;

    private SortFragment(){}

    public static SortFragment newInstance(String field) {

        Bundle args = new Bundle();
        args.putSerializable(KEY, field);
        SortFragment fragment = new SortFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        currentSortField = getArguments().getString(KEY);

        View view = inflater.inflate(R.layout.fragment_sort, container, false);

        final TextView sortByName = view.findViewById(R.id.sort_name_textview);
        final TextView sortByCategory = view.findViewById(R.id.sort_category_textview);
        final TextView sortByDeadline = view.findViewById(R.id.sort_deadline_textview);
        final TextView sortByStatus = view.findViewById(R.id.sort_status_textview);

        switch (currentSortField) {
            case Task.NAME:
                sortByName.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_check_black_24dp, 0);
                break;

            case Task.CATEGORY:
                sortByCategory.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_check_black_24dp, 0);
                break;

            case Task.DEADLINE:
                sortByDeadline.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_check_black_24dp, 0);
                break;

            case Task.STATUS:
                sortByStatus.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_check_black_24dp, 0);
                break;


        }


        final TaskList taskList = TaskList.get();
        final TaskComparatorFactory taskComparatorFactory = TaskComparatorFactory.get();

        sortByName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainFragment.setSortField(Task.NAME);
                returnToMainFragment();
            }
        });

        sortByCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainFragment.setSortField(Task.CATEGORY);
                returnToMainFragment();
            }
        });

        sortByDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainFragment.setSortField(Task.DEADLINE);
                returnToMainFragment();
            }
        });

        sortByStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainFragment.setSortField(Task.STATUS);
                returnToMainFragment();
            }
        });

        return view;
    }

    private void returnToMainFragment() {
        getActivity().getSupportFragmentManager()
                .popBackStack();
        MainFragment fragment = MainFragment.getInstance().setSignOutListener(new MainFragment.SignOutListener() {
            @Override
            public void onSignOut() {
                startActivityForResult(Auth.getAuthSignInIntent(
                        Auth.EMAIL_PROVIDER,
                        Auth.GOOGLE_PROVIDER),SIGN_IN_INTENT);
            }
        });
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, "MainFragment")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .commit();
    }
}

