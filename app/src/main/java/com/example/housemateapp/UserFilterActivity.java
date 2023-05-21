package com.example.housemateapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.housemateapp.entities.User;

public class UserFilterActivity extends AppCompatActivity {

    Button button_applyFilter;

    SeekBar seekBar_range;
    TextView text_rangeValue;
    SeekBar seekBar_days;
    TextView text_daysValue;
    Spinner spinner_statusType;
    Spinner spinner_sortBy;

    private double selectedRange = 0f;
    private int selectedDays = 0;
    private int selectedStatusTypeIndex = 0;
    private int selectedSortTypeIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_filter);

        seekBar_range = findViewById(R.id.seekBarUserFilterRange);
        text_rangeValue = findViewById(R.id.textUserFilterRangeValue);
        seekBar_days = findViewById(R.id.seekBarUserFilterStay);
        text_daysValue = findViewById(R.id.textUserFilterStayValue);
        spinner_statusType = findViewById(R.id.spinnerFilterStatusType);
        spinner_sortBy = findViewById(R.id.spinnerSortingType);
        String[] statusTypes = getResources().getStringArray(R.array.status_types);
        String[] sortByTypes = getResources().getStringArray(R.array.sort_users_by);

        Resources resources = getResources();

        text_rangeValue.setText(resources.getString(R.string.display_range_in_kilometers, 0f));
        text_daysValue.setText(resources.getString(R.string.display_will_stay_for_days, 0));

        seekBar_range.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                selectedRange = (double) i / 10;
                String valueString = resources.getString(R.string.display_range_in_kilometers, selectedRange);
                text_rangeValue.setText(valueString);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBar_days.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                selectedDays = i;
                String valueString = resources.getString(R.string.display_will_stay_for_days, i);
                text_daysValue.setText(valueString);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        button_applyFilter = findViewById(R.id.buttonApplyFilter);

        button_applyFilter.setOnClickListener(view -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(User.RANGE_IN_KILOMETERS, selectedRange);
            resultIntent.putExtra(User.WILL_STAY_FOR_DAYS, selectedDays);
            resultIntent.putExtra(User.STATUS_TYPE, statusTypes[selectedStatusTypeIndex]);
            resultIntent.putExtra("sortType", sortByTypes[selectedSortTypeIndex]);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        ArrayAdapter<CharSequence> statusTypeAdapter = ArrayAdapter
            .createFromResource(this, R.array.status_types, android.R.layout.simple_spinner_item);
        spinner_statusType.setAdapter(statusTypeAdapter);
        spinner_statusType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedStatusTypeIndex = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedStatusTypeIndex = 0;
            }
        });

        ArrayAdapter<CharSequence> sortingTypeAdapter = ArrayAdapter
            .createFromResource(this, R.array.sort_users_by, android.R.layout.simple_spinner_item);
        spinner_sortBy.setAdapter(sortingTypeAdapter);
        spinner_sortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSortTypeIndex = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedSortTypeIndex = 0;
            }
        });
    }
}