package com.snowdragon.whatsnext.controller;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.snowdragon.whatsnext.model.Task;

import org.w3c.dom.Text;

import java.util.List;

public class DetailFragment extends Fragment {

    private static final String KEY = "TASK";
    private static final String TAG = "DetailFragment";
    private static String[] statusArray = {"COMPLETED", "IN_PROGRESS", "ON_HOLD", "UNDONE"};


    private Task mTask;

    public static DetailFragment newInstance(Task task) {


        Bundle args = new Bundle();
        args.putSerializable(KEY, task);
        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        EditText taskName = view.findViewById(R.id.detail_name_edittext);
        EditText taskCategory = view.findViewById(R.id.detail_category_edittext);
        EditText taskDescription = view.findViewById(R.id.detail_description_edittext);
        Button taskStatus = view.findViewById(R.id.detail_status_button);
        TextView taskDeadline = view.findViewById(R.id.detail_deadline_textview);

        mTask = (Task) getArguments().getSerializable(KEY);
        taskName.setText(mTask.getName());
        taskCategory.setText(mTask.getCategory());
        taskDescription.setText(mTask.getDescription());
        taskStatus.setText(statusArray[ mTask.getStatus() ]);
        taskDeadline.setText(DateFormat.format("dd/MM/yyyy",mTask.getDeadline().getTime()));
        return view;
    }


}
