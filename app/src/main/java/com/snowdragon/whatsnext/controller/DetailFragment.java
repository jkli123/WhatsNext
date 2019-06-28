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
import androidx.fragment.app.Fragment;

import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskChange;

import java.util.Calendar;

public class DetailFragment extends Fragment {

    private static final String KEY = "TASK";
    private static final String TAG = "DetailFragment";


    private Task mTask;
    private final Calendar mTaskDeadlineValue = Calendar.getInstance();;
    private TaskChange.Builder mTaskChangeBuilder;
    private int mStatusIdx;
    private Button mSaveChange;


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
        mTaskChangeBuilder = new TaskChange.Builder();

        // Fetching the references to View items in this fragment
        final EditText taskName = view.findViewById(R.id.detail_name_edittext);
        final EditText taskCategory = view.findViewById(R.id.detail_category_edittext);
        final EditText taskDescription = view.findViewById(R.id.detail_description_edittext);
        final Button taskStatus = view.findViewById(R.id.detail_status_button);
        final TextView taskDeadline = view.findViewById(R.id.detail_deadline_textview);
        mSaveChange = view.findViewById(R.id.detail_save_change_button);

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


        // Allowing status to be changed on click. Status cycles through the four default statuses -
        // COMPLETED, IN_PROGRESS, ON_HOLD, UNDONE
        taskStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatusIndex();
                taskStatus.setText(Task.getStatusStringFromIndex(mStatusIdx));
            }
        });

        // Adding TextChangeListeners to all fields
        taskName.addTextChangedListener(
                new ViewTextWatcher(TaskChange.NAME));
        taskCategory.addTextChangedListener(
                new ViewTextWatcher(TaskChange.CATEGORY));
        taskDescription.addTextChangedListener(
                new ViewTextWatcher(TaskChange.DESCRIPTION));
        taskDeadline.addTextChangedListener(
                new ViewTextWatcher(TaskChange.DEADLINE));
        taskStatus.addTextChangedListener(
                new ViewTextWatcher(TaskChange.STATUS));

        // Adding the ClickListener to the "Save changes" Button
        mSaveChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskChange taskChange = mTaskChangeBuilder.build();
                taskChange.updateTask(mTask);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MainFragment(), "MainFragment")
                        .addToBackStack(null)
                        .commit();
            }
        });
        mSaveChange.setEnabled(false);
        return view;
    }


    /*
     * Used in the taskStatus button click listener to toggle between the different statuses on click
     */
    private void updateStatusIndex() {
        mStatusIdx = (mStatusIdx+1) % 4;
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
            mSaveChange.setEnabled(true);
            String updatedValue = s.toString();
            switch (mField) {
                case TaskChange.NAME:
                    mTaskChangeBuilder.updateName(updatedValue);
                    break;

                case TaskChange.CATEGORY:
                    mTaskChangeBuilder.updateCategory(updatedValue);
                    break;

                case TaskChange.DESCRIPTION:
                    mTaskChangeBuilder.updateDescription(updatedValue);
                    break;

                case TaskChange.STATUS:
                    mTaskChangeBuilder.updateStatus(Task.getStatusIndexFromString(updatedValue));
                    break;

                case TaskChange.DEADLINE:
                    mTaskChangeBuilder.updateDeadline(mTaskDeadlineValue.getTime());
                    break;

            }

        }

    }

    // TODO: Create a "Undo changes" button when content is changed

}
