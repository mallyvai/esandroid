package edu.umich.esandroid.findanator;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

public class EditAlarm extends Activity
{
    private AutoCompleteTextView mName;
    private EditText mNotes;
    private EditText mLocations;
    private EditText mStartTime;
    private EditText mEndTime;
    private EditText mStartDate;
    private EditText mEndDate;
    private Button mDone;

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
        mName = (AutoCompleteTextView) findViewById(R.id.alarm_name);
        String[] alarmNames = getAllAlarmNames();
        if (alarmNames != null)
        {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_dropdown_item_1line, alarmNames);
            mName.setAdapter(adapter);
        }
        mNotes = (EditText) findViewById(R.id.alarm_notes);
        mLocations = (EditText) findViewById(R.id.alarm_locations);
        mStartTime = (EditText) findViewById(R.id.alarm_start_time);
        mEndTime = (EditText) findViewById(R.id.alarm_end_time);
        mStartDate = (EditText) findViewById(R.id.alarm_start_date);
        mEndDate = (EditText) findViewById(R.id.alarm_end_date);
        mDone = (Button) findViewById(R.id.done);
        mDone.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                setResult(RESULT_OK);
                finish();
            }
        });

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
}
