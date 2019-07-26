package com.snowdragon.whatsnext.controller;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.snowdragon.whatsnext.database.Auth;
import com.snowdragon.whatsnext.database.Database;
import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskChange;
import com.snowdragon.whatsnext.model.TaskList;

import java.util.Calendar;

public class DetailFragment extends AbstractStaticFragment {

    private static final String KEY = "TASK";
    private static final String TAG = "DetailFragment";

    private Task mTask;
    private FirebaseUser mFirebaseUser = Auth.getInstance().getCurrentUser();
    private final Calendar mTaskDeadlineValue = Calendar.getInstance();
    private TaskChange.Builder mTaskChangeBuilder;
    private int mStatusIdx;
    private Button mUpdateButton;
    private Database mDatabase = Database.getInstance(getActivity());


     static DetailFragment newInstance(Task task) {

        Bundle args = new Bundle();
        args.putSerializable(KEY, task);
        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        mTaskChangeBuilder = new TaskChange.Builder();

        // Fetching the references to View items in this fragment
        final EditText taskName = view.findViewById(R.id.detail_name_edittext);
        final EditText taskCategory = view.findViewById(R.id.detail_category_edittext);
        final EditText taskDescription = view.findViewById(R.id.detail_description_edittext);
        final Button taskStatus = view.findViewById(R.id.detail_status_button);
        final TextView taskDeadline = view.findViewById(R.id.detail_deadline_textview);
        mUpdateButton = view.findViewById(R.id.detail_update_button);
        final Button deleteButton = view.findViewById(R.id.detail_delete_button);

        // Retrieving Task fields content for display
        mTask = (Task) getArguments().getSerializable(KEY);
        mStatusIdx = mTask.getStatus();

        taskName.setText(mTask.getName());
        taskCategory.setText(mTask.getCategory());
        taskDescription.setText(mTask.getDescription());
        taskDeadline.setText(DateFormat.format("dd/MM/yyyy",mTask.getDeadline()));
        taskStatus.setText(Task.getStatusStringFromIndex(mStatusIdx));

        // Assigning taskDeadline TextView to open a DatePickerDialog to select date when clicked on
        mTaskDeadlineValue.setTime(mTask.getDeadline());
        final DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mTaskDeadlineValue.set(year, month, dayOfMonth);
                        taskDeadline.setText(
                                DateFormat.format("dd/MM/yyyy", mTaskDeadlineValue.getTime())
                        );
                    }
                },
                mTaskDeadlineValue.get(Calendar.YEAR),
                mTaskDeadlineValue.get(Calendar.MONTH),
                mTaskDeadlineValue.get(Calendar.DAY_OF_MONTH));

        taskDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        // Allowing status to be changed on click. Status cycles through the four default statuses
        taskStatus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mStatusIdx = Task.toggleStatus(mStatusIdx);
                taskStatus.setText(Task.getStatusStringFromIndex(mStatusIdx));
            }
        });

        // Adding TextChangeListeners to all fields
        taskName.addTextChangedListener(
                new ViewTextWatcher(Task.NAME));
        taskCategory.addTextChangedListener(
                new ViewTextWatcher(Task.CATEGORY));
        taskDescription.addTextChangedListener(
                new ViewTextWatcher(Task.DESCRIPTION));
        taskDeadline.addTextChangedListener(
                new ViewTextWatcher(Task.DEADLINE));
        taskStatus.addTextChangedListener(
                new ViewTextWatcher(Task.STATUS));

        // Adding the ClickListener to the "Update" Button
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskChange taskChange = mTaskChangeBuilder.build();
                TaskList.get().updateTask(mTask.getId(), taskChange);
                if (mTask.getStatus() == Task.DONE) {
                    mDatabase.deleteTaskForUser(mFirebaseUser, mTask, Database.TASK_COLLECTION);
                    mDatabase.addTaskForUser(mFirebaseUser, mTask, Database.DONE_COLLECTION);
                } else {
                    mDatabase.updateTaskForUser(mFirebaseUser,
                            mTask.getId(),
                            taskChange,
                            Database.TASK_COLLECTION);
                }
                returnToMainFragment();
            }
        });
        mUpdateButton.setEnabled(false);

        // Adding the ClickListener to the "Delete" Button
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskList.get().deleteTaskById(mTask.getId());
                mDatabase.deleteTaskForUser(mFirebaseUser, mTask, Database.TASK_COLLECTION);
                mDatabase.deleteTaskForUser(mFirebaseUser, mTask, Database.DONE_COLLECTION);
                returnToMainFragment();
            }
        });

        return view;
    }

    /*
     * Replaces the current AdditionFragment with the MainFragment.
     */
    private void returnToMainFragment() {
        getActivity().getSupportFragmentManager()
                .popBackStack();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, TaskNotDoneFragment.newInstance(), "MainFragment")
                .addToBackStack(null)
                .commit();
    }

    private class ViewTextWatcher implements TextWatcher {
        private String mField;

        public ViewTextWatcher(String field) {
            this.mField = field;

        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            mUpdateButton.setEnabled(true);
            String updatedValue = s.toString();
            switch (mField) {
                case Task.NAME:
                    mTaskChangeBuilder.updateName(updatedValue);
                    break;

                case Task.CATEGORY:
                    mTaskChangeBuilder.updateCategory(updatedValue);
                    break;

                case Task.DESCRIPTION:
                    mTaskChangeBuilder.updateDescription(updatedValue);
                    break;

                case Task.STATUS:
                    mTaskChangeBuilder.updateStatus(Task.getStatusIndexFromString(updatedValue));
                    break;

                case Task.DEADLINE:
                    mTaskChangeBuilder.updateDeadline(mTaskDeadlineValue.getTime());
                    break;
            }

        }

    }

}
