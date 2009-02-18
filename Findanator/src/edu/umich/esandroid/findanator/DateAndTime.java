package edu.umich.esandroid.findanator;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

public class DateAndTime extends Activity
{
    private TextView mTvStartDate;
    private Button mBtnStartDate;
    private TextView mTvEndDate;
    private Button mBtnEndDate;
    private TextView mTvStartTime;
    private Button mBtnStartTime;
    private TextView mTvEndTime;
    private Button mBtnEndTime;

    private TextView mCurrentDate;
    private TextView mCurrentTime;

    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMin;

    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.date_and_time);

        // Capture View elements
        mTvStartDate = (TextView) findViewById(R.id.date_time_start_date);
        mBtnStartDate = (Button) findViewById(R.id.date_time_pick_start_date);
        mTvEndDate = (TextView) findViewById(R.id.date_time_end_date);
        mBtnEndDate = (Button) findViewById(R.id.date_time_pick_end_date);
        mTvStartTime = (TextView) findViewById(R.id.date_time_start_time);
        mBtnStartTime = (Button) findViewById(R.id.date_time_pick_start_time);
        mTvEndTime = (TextView) findViewById(R.id.date_time_end_time);
        mBtnEndTime = (Button) findViewById(R.id.date_time_pick_end_time);

        // Add click listeners to all buttons
        mBtnStartDate.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mCurrentDate = mTvStartDate;
                showDialog(DATE_DIALOG_ID);
            }
        });

        mBtnEndDate.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mCurrentDate = mTvEndDate;
                showDialog(DATE_DIALOG_ID);
            }
        });

        mBtnStartTime.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mCurrentTime = mTvStartTime;
                showDialog(TIME_DIALOG_ID);
            }
        });

        mBtnEndTime.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mCurrentTime = mTvEndTime;
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
        mCurrentDate = mTvStartDate;
        mCurrentTime = mTvStartTime;
        updateDisplay();

        // Set End Date and End Time to current time + 1 day
        ++mDay;
        mCurrentDate = mTvEndDate;
        mCurrentTime = mTvEndTime;
        updateDisplay();
        --mDay;
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

    // The callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener()
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
    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener()
    {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute)
        {
            mHour = hourOfDay;
            mMin = minute;
            updateDisplay();
        }
    };

    @Override
    protected void onPause()
    {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    private void updateDisplay()
    {
        // month is 0 based, so we need to add 1 to get it right
        mCurrentDate.setText(new StringBuilder()
                .append(mMonth + 1).append("-").append(mDay).append("-").append(mYear).append(" "));
        mCurrentTime.setText(new StringBuilder().append(pad(mHour)).append(":").append(pad(mMin)));
    }

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
