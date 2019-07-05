package com.snowdragon.whatsnext.controller;

import android.app.DatePickerDialog;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseUser;
import com.snowdragon.whatsnext.database.Auth;
import com.snowdragon.whatsnext.database.Database;
import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskList;

import java.util.Calendar;
import java.util.UUID;

public class AdditionFragment extends Fragment {
    private static final String TAG = "AdditionFragment";
    private int mStatusIdx;

    public static AdditionFragment newInstance() {
        Bundle args = new Bundle();
        AdditionFragment fragment = new AdditionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addition, container, false);

        // Fetching the references to View items in this fragment
        final EditText taskName = view.findViewById(R.id.addition_name_edittext);
        final EditText taskCategory = view.findViewById(R.id.addition_category_edittext);
        final EditText taskDescription = view.findViewById(R.id.addition_description_edittext);
        final Button taskStatus = view.findViewById(R.id.addition_status_button);
        final TextView taskDeadline = view.findViewById(R.id.addition_deadline_textview);
        Button addTask = view.findViewById(R.id.addition_add_button);

        // Initializing default value on taskStatus Button
        mStatusIdx = Task.NOT_DONE;
        taskStatus.setText(Task.getStatusStringFromIndex(mStatusIdx));

        // Allowing status to be changed on click. Status cycles through the four default statuses
        taskStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusIdx = Task.toggleStatus(mStatusIdx);
                taskStatus.setText(Task.getStatusStringFromIndex(mStatusIdx));
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

        // "Add Task" button performs an addition of Task to sTasks in TaskList
        // and returns to the MainFragment when clicked on
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Task task = new Task();
                task.setName(taskName.getText().toString());
                task.setCategory(taskCategory.getText().toString());
                task.setDescription(taskDescription.getText().toString());
                task.setDeadline(taskDeadlineValue.getTime());
                task.setStatus(Task.getStatusIndexFromString(taskStatus.getText().toString()));
                task.setId(UUID.randomUUID().toString());

                TaskList.get().add(task);
                if (task.getStatus() == Task.DONE) {
                    database.addTaskForUser(firebaseUser, task, Database.DONE_COLLECTION);
                } else {
                    database.addTaskForUser(firebaseUser, task, Database.TASK_COLLECTION);
                }


                // Return to MainFragment
                getActivity().getSupportFragmentManager()
                        .popBackStack();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MainFragment(), "MainFragment")
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }
}
