package com.snowdragon.whatsnext.controller;

import android.app.DatePickerDialog;
import android.os.Bundle;
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




        return view;
    }

    private void returnToPreviousFragment() {
        getActivity().getSupportFragmentManager()
                .popBackStack();
    }


}
