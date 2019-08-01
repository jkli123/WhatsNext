package com.snowdragon.whatsnext.controller;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.snowdragon.whatsnext.model.TaskList;

import java.util.Calendar;
import java.util.UUID;

public class AdditionFragment extends AbstractStaticFragment {

    private static final String TAG = "AdditionFragment";
    private int mStatusIdx = Task.NOT_DONE;

    static AdditionFragment newInstance() {
        return new AdditionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_addition, container, false);

        // Fetching the references to View items in this fragment
        final EditText taskName = view.findViewById(R.id.addition_name_edittext);
        final EditText taskCategory = view.findViewById(R.id.addition_category_edittext);
        final EditText taskDescription = view.findViewById(R.id.addition_description_edittext);
        final Spinner taskStatus = view.findViewById(R.id.addition_status_spinner);
        final TextView taskDeadline = view.findViewById(R.id.addition_deadline_textview);
        Button cancelButton = view.findViewById(R.id.addition_cancel_button);
        Button addTask = view.findViewById(R.id.addition_add_button);

//        // Initializing default value on taskStatus Button
//        mStatusIdx = Task.NOT_DONE;
//        taskStatus.setText(Task.getStatusStringFromIndex(mStatusIdx));
//
//        // Allowing status to be changed on click. Status cycles through the four default statuses
//        taskStatus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mStatusIdx = Task.toggleStatus(mStatusIdx);
//                taskStatus.setText(Task.getStatusStringFromIndex(mStatusIdx));
//            }
//        });

        // Initializing ArrayAdaptor for taskStatus Spinner items
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item,
                Task.sStatusList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskStatus.setAdapter(arrayAdapter);
        taskStatus.setSelection(Task.NOT_DONE);
        taskStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String status = (String) parent.getItemAtPosition(position);
                mStatusIdx = Task.getStatusIndexFromString(status);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Initializing the default deadline on the Deadline TextView as the current date
        final Calendar taskDeadlineValue = Calendar.getInstance();
        taskDeadline.setText(DateFormat.format("dd/MM/yyyy", taskDeadlineValue.getTime()));

        // Assigning taskDeadline TextView to open a DatePickerDialog to select date when clicked on
        final DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        taskDeadlineValue.set(year, month, dayOfMonth);
                        taskDeadline.setText(DateFormat.format("dd/MM/yyyy", taskDeadlineValue.getTime()));
                    }
                },
                taskDeadlineValue.get(Calendar.YEAR),
                taskDeadlineValue.get(Calendar.MONTH),
                taskDeadlineValue.get(Calendar.DAY_OF_MONTH));

        taskDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        // Initializing reference to database
        final Database database = Database.getInstance(getActivity());
        final FirebaseUser firebaseUser = Auth.getInstance().getCurrentUser();

        // "Cancel" button returns user to the MainFragment when clicked on
        // Does not have very practical purpose apart from providing a symmetrical design
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToPreviousFragment();
            }
        });

        // "Add" button performs an addition of Task to sTasks in TaskList
        // and returns to the MainFragment when clicked on
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Task task = new Task();
                task.setName(taskName.getText().toString());
                task.setCategory(taskCategory.getText().toString());
                task.setDescription(taskDescription.getText().toString());
                task.setDeadline(taskDeadlineValue.getTime());
                task.setStatus(mStatusIdx);
                task.setId(UUID.randomUUID().toString());

                TaskList.get().addTask(task.getStatus() == Task.DONE ? TaskList.DONE_LIST: TaskList.NOT_DONE_LIST, task);
                if (task.getStatus() == Task.DONE) {
                    database.addTaskForUser(firebaseUser, task, Database.DONE_COLLECTION);
                } else {
                    database.addTaskForUser(firebaseUser, task, Database.TASK_COLLECTION);
                }

                // Return to previous fragment that started this fragment.
                returnToPreviousFragment();
            }
        });


        View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus && view instanceof EditText ) {
                    Log.d(TAG, "Focus FROM " + view.getId());
                    hideKeyboard(view);
                } else {
                    Log.d(TAG, "Focus TO " + view.getId());

                }
            }
        };

        taskName.setOnFocusChangeListener(focusChangeListener);
        taskCategory.setOnFocusChangeListener(focusChangeListener);
        taskDescription.setOnFocusChangeListener(focusChangeListener);
        taskStatus.setOnFocusChangeListener(focusChangeListener);
        taskDeadline.setOnFocusChangeListener(focusChangeListener);

        return view;
    }

    private void returnToPreviousFragment() {
        getActivity().getSupportFragmentManager()
                .popBackStack();
    }



    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

}
