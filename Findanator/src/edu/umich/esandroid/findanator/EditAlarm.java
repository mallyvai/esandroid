package edu.umich.esandroid.findanator;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditAlarm extends Activity
{
    private EditText mName;
    private EditText mNotes;
    private EditText mLocations;
    private EditText mStartTime;
    private EditText mEndTime;
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
        mName = (EditText) findViewById(R.id.alarm_name);
        mNotes = (EditText) findViewById(R.id.alarm_notes);
        mLocations = (EditText) findViewById(R.id.alarm_locations);
        mStartTime = (EditText) findViewById(R.id.alarm_start_time);
        mEndTime = (EditText) findViewById(R.id.alarm_end_time);
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
            mName.setText(alarm.getString(
                    alarm.getColumnIndexOrThrow(AlarmsDbAdapter.KEY_NAME)));
            mNotes.setText(alarm.getString(
                    alarm.getColumnIndexOrThrow(AlarmsDbAdapter.KEY_NOTES)));
            mLocations.setText(alarm.getString(
                    alarm.getColumnIndexOrThrow(AlarmsDbAdapter.KEY_LOCATIONS)));
            mStartTime.setText(alarm.getString(
                    alarm.getColumnIndexOrThrow(AlarmsDbAdapter.KEY_START_TIME)));
            mEndTime.setText(alarm.getString(
                    alarm.getColumnIndexOrThrow(AlarmsDbAdapter.KEY_END_TIME)));
        }
    }

    private void saveState()
    {
        String name = mName.getText().toString();
        String notes = mNotes.getText().toString();
        String locations = mLocations.getText().toString();
        String startTime = mStartTime.getText().toString();
        String endTime = mStartTime.getText().toString();

        if (mRowId == null)
        {
            long id = mDbHelper.createAlarm(name, notes, locations, startTime, endTime);
            if (id > 0)
            {
                mRowId = id;
            }
        }
        else
        {
            mDbHelper.updateAlarm(mRowId, name, notes, locations, startTime, endTime);
        }
    }
}
