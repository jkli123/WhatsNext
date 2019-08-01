package com.snowdragon.whatsnext.controller;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
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
        final TextView taskDeadline = view.findViewById(R.id.detail_deadline_textview);
        final Spinner taskStatus = view.findViewById(R.id.detail_status_spinner);
        mUpdateButton = view.findViewById(R.id.detail_update_button);
        final Button deleteButton = view.findViewById(R.id.detail_delete_button);


        // Retrieving Task fields content for display
        mTask = (Task) getArguments().getSerializable(KEY);
        mStatusIdx = mTask.getStatus();

        taskName.setText(mTask.getName());
        taskCategory.setText(mTask.getCategory());
        taskDescription.setText(mTask.getDescription());
        taskDeadline.setText(DateFormat.format("dd/MM/yyyy",mTask.getDeadline()));

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

        // taskStatus toggle Button has now been replaced with taskStatus Spinner
        // Initializing ArrayAdaptor for taskStatus Spinner items
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item,
                Task.sStatusList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskStatus.setAdapter(arrayAdapter);

        // Set current view on the Spinner to be mTask's current status
        taskStatus.setSelection(mTask.getStatus());

        // Implementing OnItemSelectedListener for the Spinner
        // Note: OnItemClickedListener is not meant for Spinners and an error will be thrown if used
        taskStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String status = (String) parent.getItemAtPosition(position);
                mStatusIdx = Task.getStatusIndexFromString(status);

                // Enable updateButton on first change made to Status
                if (mStatusIdx != mTask.getStatus()) {
                    mUpdateButton.setEnabled(true);
                }

                // Update Status regardless
                mTaskChangeBuilder.updateStatus(mStatusIdx);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

        // Adding the ClickListener to the "Update" Button
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskChange taskChange = mTaskChangeBuilder.build();
                int oldStatus = mTask.getStatus();
                TaskList.get().updateTask(mTask.getId(), taskChange);
                int newStatus = mTask.getStatus();
                if (oldStatus != Task.DONE && newStatus == Task.DONE) {
                    //Shift from not done list to done list
                    mDatabase.deleteTaskForUser(mFirebaseUser, mTask, Database.TASK_COLLECTION);
                    mDatabase.addTaskForUser(mFirebaseUser, mTask, Database.DONE_COLLECTION);
                } else if(oldStatus == Task.DONE && newStatus != Task.DONE) {
                    //Shift from done to not done list
                    mDatabase.deleteTaskForUser(mFirebaseUser, mTask, Database.DONE_COLLECTION);
                    mDatabase.addTaskForUser(mFirebaseUser, mTask, Database.TASK_COLLECTION);
                } else {
                    mDatabase.updateTaskForUser(mFirebaseUser,
                            mTask.getId(),
                            taskChange,
                            newStatus == Task.DONE
                                    ? Database.DONE_COLLECTION
                                    : Database.TASK_COLLECTION);
                }
                returnToPreviousFragment();
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
                returnToPreviousFragment();
            }
        });

        return view;
    }

    //Return to whichever fragment called this fragment
    private void returnToPreviousFragment() {
        getActivity().getSupportFragmentManager()
                .popBackStack();
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

                case Task.DEADLINE:
                    mTaskChangeBuilder.updateDeadline(mTaskDeadlineValue.getTime());
                    break;
            }

        }

    }

}
