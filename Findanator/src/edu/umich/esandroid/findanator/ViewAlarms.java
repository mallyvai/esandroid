/*
 * Copyright (C) 2009 Gopalkrishna Sharma.
 *
 * Author: Gopalkrishna Sharma
 * Email: gopalkri@umich.edu
 * Date created: 16 / February / 2009
 *
 * This file is part of Team ESAndroid's project for EECS 498 Winter 2009 with Prof. Soloway.
 * Team members: Gopalkrishna Sharma (gopalkri@umich.edu),
 * Nader Jawad (njawad@umich.edu), Vaibhav Mallya (mallyvai@umich.edu).
 *
 */

package edu.umich.esandroid.findanator;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ViewAlarms extends ListActivity
{
    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private AlarmsDbAdapter mDbHelper;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mDbHelper = new AlarmsDbAdapter(this);
        mDbHelper.open();

        fillData();

        registerForContextMenu(getListView());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);

        Intent i = new Intent(this, EditAlarm.class);
        i.putExtra(AlarmsDbAdapter.KEY_ROWID, id);

        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        //Bundle extras = data.getExtras();

        switch(requestCode)
        {
        case ACTIVITY_CREATE:
            // Get relevant alarm details from extras, and update database and list view.
            break;
        case ACTIVITY_EDIT:
            // Get row id of edited note
            // If row id is valid, get alarm details from extras and update database.
            break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
        case DELETE_ID:
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            // info.id contains the position of the item in the list view that needs to be deleted.
            mDbHelper.deleteAlarm(info.id);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete_alarm);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_add_alarm);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        switch (item.getItemId())
        {
        case INSERT_ID:
            createAlarm();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void fillData()
    {
        Cursor alarmsCursor = mDbHelper.fetchAllAlarms();
        startManagingCursor(alarmsCursor);

        // Create an array to specify the fields we want to display in the list
        // (only NAME)
        String[] from = new String[] { AlarmsDbAdapter.KEY_NAME };

        // and an array of the fields we want to bind those fields to (in this
        // case just alarm_row)
        int[] to = new int[] { R.id.alarm_row };

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter alarms = new SimpleCursorAdapter(this, R.layout.alarm_row, alarmsCursor,
                from, to);
        setListAdapter(alarms);
    }

    /**
     * Creates a new alarm, and goes to AddAlarm page.
     */
    private void createAlarm()
    {
        Intent i = new Intent(this, EditAlarm.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }
}
