/*
 * Copyright (C) 2009 Gopalkrishna Sharma, Nader Jawad, Vaibhav Mallya.
 *
 * Author: Gopalkrishna Sharma
 * Email: gopalkri@umich.edu
 * Date started: 16 / February / 2009
 *
 * This file is part of Team ESAndroid's project for EECS 498 Winter 2009 with Prof. Soloway.
 * Team members: Gopalkrishna Sharma (gopalkri@umich.edu),
 * Nader Jawad (njawad@umich.edu), Vaibhav Mallya (mallyvai@umich.edu).
 *
 */

package edu.umich.esandroid.findanator;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;

public class EditAlarm extends TabActivity
{
    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;

    private TabHost mTabHost;
    private AutoCompleteTextView mName;
    private EditText mNotes;
    private EditText mLocations;
    private TextView mStartTime;
    private TextView mEndTime;
    private TextView mStartDate;
    private TextView mEndDate;
    private Button mBtnStartDate;
    private Button mBtnEndDate;
    private Button mBtnStartTime;
    private Button mBtnEndTime;
    private Button mDone;

    private TextView mCurrentDate;
    private TextView mCurrentTime;

    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMin;

    private Long mRowId;
    private AlarmsDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Initialize database
        mDbHelper = new AlarmsDbAdapter(this);
        mDbHelper.open();

        // Set up UI
        setContentView(R.layout.edit_alarm);
        setupUI();

        // Initialize with alarm data
        if (savedInstanceState == null)
        {
            mRowId = null;
        }
        else
        {
            mRowId = savedInstanceState.getLong(AlarmsDbAdapter.KEY_ROWID);
        }
        if (mRowId == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras != null)
            {
                mRowId = extras.getLong(AlarmsDbAdapter.KEY_ROWID);
            }
        }
        populateFields();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        populateFields();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putLong(AlarmsDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id)
        {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);

        case TIME_DIALOG_ID:
            return new TimePickerDialog(this, mTimeSetListener, mHour, mMin, false);
        }
        return null;
    }

    private void populateFields()
    {
        if (mRowId != null)
        {
            Cursor alarm = mDbHelper.fetchAlarm(mRowId);
            startManagingCursor(alarm);
            mName.setText(alarm.getString(alarm.getColumnIndexOrThrow(AlarmsDbAdapter.KEY_NAME)));
            mNotes.setText(alarm.getString(alarm.getColumnIndexOrThrow(AlarmsDbAdapter.KEY_NOTES)));
            mLocations.setText(alarm.getString(alarm
                    .getColumnIndexOrThrow(AlarmsDbAdapter.KEY_LOCATIONS)));
            mStartTime.setText(alarm.getString(alarm
                    .getColumnIndexOrThrow(AlarmsDbAdapter.KEY_START_TIME)));
            mEndTime.setText(alarm.getString(alarm
                    .getColumnIndexOrThrow(AlarmsDbAdapter.KEY_END_TIME)));
            mStartDate.setText(alarm.getString(alarm
                    .getColumnIndexOrThrow(AlarmsDbAdapter.KEY_START_DATE)));
            mEndDate.setText(alarm.getString(alarm
                    .getColumnIndexOrThrow(AlarmsDbAdapter.KEY_END_DATE)));
        }
        mTabHost.setCurrentTab(0);
    }

    private void saveState()
    {
        String name = mName.getText().toString();
        String notes = mNotes.getText().toString();
        String locations = mLocations.getText().toString();
        String startTime = mStartTime.getText().toString();
        String endTime = mStartTime.getText().toString();
        String startDate = mStartDate.getText().toString();
        String endDate = mEndDate.getText().toString();

        if (mRowId == null)
        {
            long id = mDbHelper.createAlarm(name, notes, locations, startTime, endTime, startDate,
                    endDate);
            if (id > 0)
            {
                mRowId = id;
            }
        }
        else
        {
            mDbHelper.updateAlarm(mRowId, name, notes, locations, startTime, endTime, startDate,
                    endDate);
        }
    }

    private String[] getAllAlarmNames()
    {
        Cursor c = mDbHelper.fetchAllAlarms();
        int numRows = c.getCount();
        if (numRows <= 0)
        {
            // There are no alarms in the database
            return null;
        }
        String[] names = new String[numRows];
        int namePos = c.getColumnIndexOrThrow(AlarmsDbAdapter.KEY_NAME);
        if (!c.moveToFirst())
        {
            return null;
        }
        int i = 0;
        names[i++] = c.getString(namePos);
        while (c.moveToNext())
        {
            names[i++] = c.getString(namePos);
        }
        return (String[]) names;
    }

    private void setupUI()
    {
        mTabHost = getTabHost();
        mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator("Name").setContent(
                R.id.edit_alarm_name_tab));
        mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator("Notes").setContent(
                R.id.edit_alarm_notes_tab));
        mTabHost.addTab(mTabHost.newTabSpec("tab_test3").setIndicator("Places").setContent(
                R.id.edit_alarm_locations_tab));
        mTabHost.addTab(mTabHost.newTabSpec("tab_test4").setIndicator("Times").setContent(
                R.id.edit_alarm_times_tab));
        mTabHost.setCurrentTab(0);

        mName = (AutoCompleteTextView) findViewById(R.id.edit_alarm_name);
        String[] alarmNames = getAllAlarmNames();
        if (alarmNames != null)
        {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_dropdown_item_1line, alarmNames);
            mName.setAdapter(adapter);
        }
        mNotes = (EditText) findViewById(R.id.edit_alarm_notes);
        mLocations = (EditText) findViewById(R.id.edit_alarm_locations);
        mStartTime = (TextView) findViewById(R.id.edit_alarm_start_time);
        mEndTime = (TextView) findViewById(R.id.edit_alarm_end_time);
        mStartDate = (TextView) findViewById(R.id.edit_alarm_start_date);
        mEndDate = (TextView) findViewById(R.id.edit_alarm_end_date);
        mDone = (Button) findViewById(R.id.edit_alarm_done);
        mDone.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                setResult(RESULT_OK);
                finish();
            }
        });

        mBtnStartDate = (Button) findViewById(R.id.edit_alarm_pick_start_date);
        mBtnEndDate = (Button) findViewById(R.id.edit_alarm_pick_end_date);
        mBtnStartTime = (Button) findViewById(R.id.edit_alarm_pick_start_time);
        mBtnEndTime = (Button) findViewById(R.id.edit_alarm_pick_end_time);
        // Set click listeners for all date/time changing buttons
        mBtnStartDate.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mCurrentDate = mStartDate;
                showDialog(DATE_DIALOG_ID);
            }
        });

        mBtnEndDate.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mCurrentDate = mEndDate;
                showDialog(DATE_DIALOG_ID);
            }
        });

        mBtnStartTime.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mCurrentTime = mStartTime;
                showDialog(TIME_DIALOG_ID);
            }
        });

        mBtnEndTime.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mCurrentTime = mEndTime;
                showDialog(TIME_DIALOG_ID);
            }
        });

        // Get current date and time
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMin = c.get(Calendar.MINUTE);

        // Set Start Date and Start Time to current time
        mCurrentDate = mStartDate;
        mCurrentTime = mStartTime;
        updateDisplay();

        // Set End Date and End Time to current time + 1 day
        ++mDay;
        mCurrentDate = mEndDate;
        mCurrentTime = mEndTime;
        updateDisplay();
        --mDay;
    }

    private void updateDisplay()
    {
        mCurrentDate.setText(new StringBuilder().append(mMonth + 1).append("-").append(mDay)
                .append("-").append(mYear).append(" "));
        mCurrentTime.setText(new StringBuilder().append(pad(mHour)).append(":").append(pad(mMin)));
    }

    // The callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener()
    {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDisplay();
        }
    };

    // The callback received when the user "sets" the time in the dialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
        new TimePickerDialog.OnTimeSetListener()
    {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute)
        {
            mHour = hourOfDay;
            mMin = minute;
            updateDisplay();
        }
    };

    private static String pad(int c)
    {
        if (c >= 10)
        {
            return String.valueOf(c);
        }
        else
        {
            return "0" + String.valueOf(c);
        }
    }
}
