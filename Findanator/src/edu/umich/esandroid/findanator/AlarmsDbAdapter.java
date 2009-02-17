/*
 * Copyright (C) 2009 Gopalkrishna Sharma.
 *
 * Author: Gopalkrishna Sharma Email: gopalkri@umich.edu Date created: 16 /
 * February / 2009
 *
 * This file is part of Team ESAndroid's project for EECS 498 Winter 2009 with
 * Prof. Soloway. Team members: Gopalkrishna Sharma (gopalkri@umich.edu), Nader
 * Jawad (njawad@umich.edu), Vaibhav Mallya (mallyvai@umich.edu).
 */

package edu.umich.esandroid.findanator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AlarmsDbAdapter
{
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_NOTES = "notes";
    public static final String KEY_LOCATIONS = "locations";
    public static final String KEY_START_TIME = "start_time";
    public static final String KEY_END_TIME = "end_time";
    public static final String KEY_START_DATE = "start_date";
    public static final String KEY_END_DATE = "end_date";

    private static final String TAG = "AlarmsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement.
     */
    private static final String DATABASE_CREATE = "create table alarms "
            + "(_id integer primary key autoincrement, "
            + "name text not null, notes text, locations text not null, "
            + "start_time text not null, end_time text not null, "
            + "start_date text not null, end_date text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "alarms";
    private static final int DATABASE_VERSION = 1;

    private Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created.
     *
     * @param ctx
     *            The context within which to work
     */
    public AlarmsDbAdapter(Context ctx)
    {
        mCtx = ctx;
    }

    /**
     * Open the alarms database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException
     *             if the database could be neither opened or created
     */
    public AlarmsDbAdapter open() throws SQLException
    {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    /**
     * Closes database, releasing any resources that were acquired.
     */
    public void close()
    {
        mDbHelper.close();
    }

    /**
     * Create a new alarm using the parameters provided. If the alarm is
     * successfully created return the new rowId for that alarm, otherwise
     * return a -1 to indicate failure.
     *
     * @param name
     *            Name of alarm
     * @param notes
     *            Notes associated with alarm
     * @param locations
     *            Locations that trigger the alarm
     * @param startTime
     *            Start time to activate alarm
     * @param endTime
     *            End time to de-activate alarm
     * @param startDate
     *            Start date to activate alarm
     * @param endDate
     *            End date to de-activate alarm
     * @return rowId of created alarm, or -1
     */
    public long createAlarm(String name, String notes, String locations, String startTime,
            String endTime, String startDate, String endDate)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_NOTES, notes);
        initialValues.put(KEY_LOCATIONS, locations);
        initialValues.put(KEY_START_TIME, startTime);
        initialValues.put(KEY_END_TIME, endTime);
        initialValues.put(KEY_START_DATE, startDate);
        initialValues.put(KEY_END_DATE, endDate);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Deletes alarm with rowId.
     *
     * @param rowId
     *            rowId of alarm to be deleted
     * @return true if alarm was successfully deleted, false otherwise
     */
    public boolean deleteAlarm(long rowId)
    {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all alarms in the database
     *
     * @return Cursor over all alarms
     */
    public Cursor fetchAllAlarms()
    {

        return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_NAME, KEY_NOTES,
                KEY_LOCATIONS, KEY_START_TIME, KEY_END_TIME }, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the alarm that matches the given rowId
     *
     * @param rowId
     *            id of alarm to retrieve
     * @return Cursor positioned to matching alarm, if found
     * @throws SQLException
     *             if alarm could not be found/retrieved
     */
    public Cursor fetchAlarm(long rowId) throws SQLException
    {

        Cursor c = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_NAME, KEY_NOTES,
                KEY_LOCATIONS, KEY_START_TIME, KEY_END_TIME, KEY_START_DATE, KEY_END_DATE },
                KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (c != null)
        {
            c.moveToFirst();
        }
        return c;

    }

    /**
     * Update the alarm using the details provided. The alarm to be updated is
     * specified using the rowId, and it is altered to use the parameters passed
     * in.
     *
     * @param rowId
     *            id of alarm to update
     * @param name
     *            name of updated alarm
     * @param notes
     *            updated notes associated with alarm
     * @param locations
     *            updated locations associated with alarm
     * @param startTime
     *            updated startTime associated with alarm
     * @param endTime
     *            updated endTime associated with alarm
     * @param startDate
     *            updated startDate associated with alarm
     * @param endDate
     *            updated endDate associated with alarm
     * @return true if alarm was successfully updated, false otherwise
     */
    public boolean updateAlarm(long rowId, String name, String notes, String locations,
            String startTime, String endTime, String startDate, String endDate)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_NOTES, notes);
        args.put(KEY_LOCATIONS, locations);
        args.put(KEY_START_TIME, startTime);
        args.put(KEY_END_TIME, endTime);
        args.put(KEY_START_DATE, startDate);
        args.put(KEY_END_DATE, endDate);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
