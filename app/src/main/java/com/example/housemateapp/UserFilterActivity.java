package com.example.housemateapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
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

    String[] statusTypes;
    String[] sortByTypes;

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

        Resources resources = getResources();
        statusTypes = resources.getStringArray(R.array.status_types);
        sortByTypes = resources.getStringArray(R.array.sort_users_by);

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

    @Override
    protected void onStart() {
        super.onStart();

        Bundle previousSelectionsBundle = getIntent().getExtras();
        updateFieldsFromBundle(previousSelectionsBundle);
    }

    private void updateFieldsFromBundle(Bundle bundle) {
        Resources resources = getResources();

        // update "Range in Kilometers" seek-bar
        selectedRange = bundle.getDouble(User.RANGE_IN_KILOMETERS, 0f);
        text_rangeValue.setText(resources.getString(R.string.display_range_in_kilometers, selectedRange));
        seekBar_range.setProgress((int) (selectedRange * 10));

        // update "Will Stay for Days" seek-bar
        selectedDays = bundle.getInt(User.WILL_STAY_FOR_DAYS, 0);
        text_daysValue.setText(resources.getString(R.string.display_will_stay_for_days, selectedDays));
        seekBar_days.setProgress(selectedDays);

        // update "Status Type" spinner
        String statusType = bundle.getString(User.STATUS_TYPE, "");
        if (!TextUtils.isEmpty(statusType)) {
            int statusTypeIndex = 0;
            while (statusTypeIndex < statusTypes.length &&
                !statusTypes[statusTypeIndex].equalsIgnoreCase(statusType)) {
                statusTypeIndex++;
            }

            if (statusTypeIndex < statusTypes.length) {
                spinner_statusType.setSelection(statusTypeIndex);
            }
        }

        // update "Sort By" spinner
        String sortByType = bundle.getString("sortType", "");
        if (!TextUtils.isEmpty(sortByType)) {
            int sortTypeIndex = 0;
            while (sortTypeIndex < sortByTypes.length &&
                !sortByTypes[sortTypeIndex].equalsIgnoreCase(sortByType)) {
                sortTypeIndex++;
            }

            if (sortTypeIndex < sortByTypes.length) {
                spinner_sortBy.setSelection(sortTypeIndex);
            }
        }
    }
}