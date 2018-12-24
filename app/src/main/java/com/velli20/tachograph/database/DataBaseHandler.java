/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) [2017] [velli20]
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package com.velli20.tachograph.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.velli20.tachograph.App;
import com.velli20.tachograph.Event;
import com.velli20.tachograph.collections.ListItemLogGroup;
import com.velli20.tachograph.database.GetLoggedRouteTask.OnGetLoggedRouteListener;
import com.velli20.tachograph.location.CustomLocation;

import java.util.ArrayList;

import static com.velli20.tachograph.database.DataBaseHandlerConstants.CREATE_EVENTS_TABLE;
import static com.velli20.tachograph.database.DataBaseHandlerConstants.CREATE_LOCATION_TABLE;

public class DataBaseHandler extends SQLiteOpenHelper implements OnDatabaseActionCompletedListener {
    public static final String TAG = "DataBaseHandler ";
    public static final boolean DEBUG = false;

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "driving_events.db";


    private static DataBaseHandler sInstance;
    public SQLiteDatabase mDb;
    private ArrayList<OnDatabaseEditedListener> mEditCallbacks = new ArrayList<>();


    private DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DataBaseHandler getInstance() {
        if (sInstance == null) sInstance = getSync();
        return sInstance;
    }

    private static synchronized DataBaseHandler getSync() {
        if (sInstance == null) sInstance = new DataBaseHandler(App.get());
        return sInstance;
    }

    public String getDatabaseName() {
        return DATABASE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EVENTS_TABLE);
        db.execSQL(CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int upgradeTo = oldVersion + 1;

        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 3:
                    db.execSQL(CREATE_LOCATION_TABLE);
                    break;
            }

            upgradeTo++;
        }
    }

    public boolean isDatabaseOpen() {
        return mDb != null && mDb.isOpen();
    }

    public void openDatabase() {
        mDb = getWritableDatabase();

        if (mDb.getVersion() < DATABASE_VERSION) {
            onUpgrade(mDb, mDb.getVersion(), DATABASE_VERSION);
        }
    }

    public void closeDatabase() {
        if (isDatabaseOpen()) {
            mDb.close();
            mDb = null;
        }
    }

    public void registerOnDatabaseEditedListener(OnDatabaseEditedListener l) {
        if (!mEditCallbacks.contains(l)) {
            mEditCallbacks.add(l);
        }
    }

    public void unregisterOnDatabaseEditedListener(OnDatabaseEditedListener l) {
        if (mEditCallbacks.remove(l) && DEBUG) {
            Log.i(TAG, TAG + "unregisterOnDatabaseEditedListener() removed callback " + l.toString());
        }
    }

    public void notifyCallbacks(int action, int rowId) {
        for (OnDatabaseEditedListener c : mEditCallbacks) {
            c.onDatabaseEdited(action, rowId);
        }
    }

    @Override
    public void onDatabaseActionCompleted(int action, int rowId) {
        notifyCallbacks(action, rowId);
    }

    public void addNewEvent(Event event) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }
        new AddEventTask(mDb, event, false).setOnDatabaseActionCompletedListener(this).execute();
    }

    public void updateEvent(Event event) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }
        new AddEventTask(mDb, event, true).setOnDatabaseActionCompletedListener(this).execute();
    }

    public void addLocation(CustomLocation location) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }
        new AddLocationTask(mDb, location).execute();
    }

    public void getEvent(int rowId, OnGetEventTaskCompleted l, boolean includeLocationInfo) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }
        String query = new DatabaseEventQueryBuilder()
                .fromTable(DataBaseHandlerConstants.TABLE_EVENTS)
                .selectAllColumns()
                .whereEventWithRowId(rowId)
                .buildQuery();

        new GetEventTask(mDb, query)
                .setOnGetEventTaskCompleted(l)
                .setIncludeLoggedRouteDistance(includeLocationInfo)
                .execute();

        if (DEBUG) {
            Log.i(TAG, "getEvent() SQL query: " + query);
        }
    }

    public void getRecordingEvent(OnGetEventTaskCompleted l) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }
        String query = new DatabaseEventQueryBuilder()
                .fromTable(DataBaseHandlerConstants.TABLE_EVENTS)
                .selectAllColumns()
                .whereEventIsRecording()
                .buildQuery();

        new GetEventTask(mDb, query)
                .setOnGetEventTaskCompleted(l)
                .execute();

        if (DEBUG) {
            Log.i(TAG, "getRecordingEvent() SQL query: " + query);
        }
    }

    public void getAllEvents(OnTaskCompleted listener, boolean ascending, boolean withLocationInfo) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }
        String query = new DatabaseEventQueryBuilder()
                .fromTable(DataBaseHandlerConstants.TABLE_EVENTS)
                .selectAllColumns()
                .orderByKey(DataBaseHandlerConstants.KEY_EVENT_START_DATE, ascending)
                .buildQuery();

        new GetEventTask(mDb, query)
                .setIncludeLoggedRouteDistance(withLocationInfo)
                .setOnTaskCompleted(listener)
                .execute();

        if (DEBUG) {
            Log.i(TAG, "getAllEvents() SQL query: " + query);
        }
    }

    public void getEventsByRowIds(OnTaskCompleted listener, int rowIds[], boolean withDrivenDistance) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }

        String query = new DatabaseEventQueryBuilder()
                .fromTable(DataBaseHandlerConstants.TABLE_EVENTS)
                .selectAllColumns()
                .whereEventsWithRowIds(rowIds)
                .buildQuery();

        new GetEventTask(mDb, query)
                .setOnTaskCompleted(listener)
                .setIncludeLoggedRouteDistance(withDrivenDistance)
                .execute(query);
    }

    public void getEventsByRowIds(OnTaskCompleted listener, ArrayList<Integer> rowIds) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }

        String query = new DatabaseEventQueryBuilder()
                .fromTable(DataBaseHandlerConstants.TABLE_EVENTS)
                .selectAllColumns()
                .whereEventsWithRowIds(rowIds)
                .buildQuery();

        new GetEventTask(mDb, query)
                .setOnTaskCompleted(listener)
                .execute(query);
    }

    public void getWorkingTimes(GetLogSummaryTask.OnWorkingTimeCalculationsReadyListener l, boolean getFullLog,
                                boolean withDrivenDistance, boolean includeRecordingEvents, boolean includeEventIds) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }
        new GetLogSummaryTask(mDb)
                .includeAllEvents(getFullLog)
                .includeDrivenDistance(withDrivenDistance)
                .includeRecordingEvents(includeRecordingEvents)
                .includeEventIds(includeEventIds)
                .setOnWorkingTimeCalculationsReadyListener(l)
                .execute();
    }

    public void getWorkingTimesWithinEventIds(GetLogSummaryTask.OnWorkingTimeCalculationsReadyListener l, ArrayList<Integer> rowIds,
                                              boolean withDrivenDistance, boolean includeRecordingEvents, boolean includeEventIds) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }
        new GetLogSummaryTask(mDb)
                .includeAllEvents(false)
                .includeDrivenDistance(withDrivenDistance)
                .includeRecordingEvents(includeRecordingEvents)
                .withEventIds(rowIds)
                .includeEventIds(includeEventIds)
                .setOnWorkingTimeCalculationsReadyListener(l)
                .execute();
    }

    public void deleteEvent(int rowId) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }
        new DeleteEventTask(mDb, rowId).setOnDatabaseActionCompletedListener(this).execute();
    }

    public void deleteEvents(ArrayList<Integer> list) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }
        new DeleteEventTask(mDb, list).setOnDatabaseActionCompletedListener(this).execute();
    }

    public void deleteLocations(int eventId) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }
        new DeleteLocationTask(mDb, eventId).setOnDatabaseActionCompletedListener(this).execute();
    }

    public void getSortedLog(String query, String months[], String titleWeek, int sortBy,
                             boolean includeDrivenDistance, OnGetSortedLogListener l) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }

        new GetSortedLogListTask(mDb, query, months, titleWeek)
                .setOnGetSortedLogListener(l)
                .setSortBy(sortBy)
                .setIncludeLoggedRouteDistance(includeDrivenDistance)
                .execute();

        if (DEBUG) {
            Log.i(TAG, "getSortedLog() SQL query: " + query);
        }
    }

    public void getEventsByTime(long startDate, long endDate, OnTaskCompleted listener, boolean withLocationInfo) {

        if (!isDatabaseOpen()) {
            openDatabase();
        }

        String query = new DatabaseEventQueryBuilder()
                .fromTable(DataBaseHandlerConstants.TABLE_EVENTS)
                .selectAllColumns()
                .whereEventsInTimeFrame(startDate, endDate, true)
                .orderByKey(DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS, true).buildQuery();

        new GetEventTask(mDb, query)
                .setIncludeLoggedRouteDistance(withLocationInfo)
                .setOnTaskCompleted(listener)
                .execute(query);

        if (DEBUG) {
            Log.i(TAG, "getEventsByTime() SQL query: " + query);
        }

    }

    public void getAllLocationByTimeFrame(long start, long end, OnGetLocationsListener l) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }
        String query = new DatabaseLocationQueryBuilder()
                .fromTable(DataBaseHandlerConstants.TABLE_LOCATIONS)
                .selectAllColumns()
                .whereLocationsPointsInTimeFrame(start, end)
                .orderByKey(DataBaseHandlerConstants.KEY_LOCATION_TIME, true)
                .buildQuery();
        new GetLocationsTask(mDb, l, query).execute();

    }

    public void getAllLocationByEventId(int id, OnGetLocationsListener l) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }

        String query = new DatabaseLocationQueryBuilder()
                .fromTable(DataBaseHandlerConstants.TABLE_LOCATIONS)
                .selectAllColumns()
                .whereLocationEventIdIs(id)
                .orderByKey(DataBaseHandlerConstants.KEY_LOCATION_TIME, true)
                .buildQuery();

        new GetLocationsTask(mDb, l, query).execute();

    }

    public void getLatestLocation(OnGetLatestLocationListener l, int eventId) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }
        String query = new DatabaseLocationQueryBuilder()
                .fromTable(DataBaseHandlerConstants.TABLE_LOCATIONS)
                .selectAllColumns()
                .whereLocationEventIdIs(eventId)
                .orderByKey(DataBaseHandlerConstants.KEY_LOCATION_TIME, false)
                .setMaxResults(1)
                .buildQuery();

        new GetLocationsTask(mDb, l, query).execute();
    }

    public void getLoggedRoute(int eventId, OnGetLoggedRouteListener l) {
        if (!isDatabaseOpen()) {
            openDatabase();
        }
        GetLoggedRouteTask task = new GetLoggedRouteTask(mDb, eventId);
        task.setOnGetLocationInfoListener(l);
        task.execute();
    }


    public interface OnTaskCompleted {
        void onTaskCompleted(ArrayList<Event> list);
    }


    public interface OnGetEventTaskCompleted {
        void onGetEvent(Event ev);
    }


    public interface OnGetLocationsListener {
        void onGetLocations(ArrayList<CustomLocation> locations);
    }

    public interface OnGetLatestLocationListener {
        void onGetLatestLocation(CustomLocation location);
    }

    public interface OnGetSortedLogListener {
        void onTaskCompleted(ArrayList<ListItemLogGroup> list);
    }

    public interface OnDatabaseEditedListener {
        void onDatabaseEdited(int action, int rowId);
    }


}
