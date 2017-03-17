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

package com.velli.tachograph.database;

import java.util.ArrayList;
import java.util.Date;

import com.velli.tachograph.App;
import com.velli.tachograph.DateCalculations;
import com.velli.tachograph.Event;
import com.velli.tachograph.R;
import com.velli.tachograph.Utils;
import com.velli.tachograph.collections.LogListItemGroup;
import com.velli.tachograph.database.GetLocationInfo.OnGetLocationInfoListener;
import com.velli.tachograph.location.CustomLocation;
import com.velli.tachograph.location.LogListItemLocation;

import static com.velli.tachograph.database.DataBaseHandlerConstants.CREATE_EVENTS_TABLE;
import static com.velli.tachograph.database.DataBaseHandlerConstants.CREATE_LOCATION_TABLE;
import static com.velli.tachograph.database.DataBaseHandlerConstants.LOCATION_COLUMNS;
import static com.velli.tachograph.database.DataBaseHandlerConstants.TABLE_EVENTS;
import static com.velli.tachograph.database.DataBaseHandlerConstants.TABLE_LOCATIONS;
import static com.velli.tachograph.database.DataBaseHandlerConstants.columnsSelection;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

public class DataBaseHandler extends SQLiteOpenHelper {
	public static final String TAG = "DataBaseHandler ";
	public static int ACTION_ADD_EVENT = 0;
	public static int ACTION_EDIT_EVENT = 1;
	public static int ACTION_DELETE_EVENT = 2;
	public static int ACTION_UPDATE_EVENT = 3;
	public static int ACTION_DELETE_LOCATION = 4;
	public static int REQUEST_CODE_CHECK_IF_EVENT_OVERLAPS_OTHER_EVENTS = 0;
	
    public static final int DATABASE_VERSION = 2;
    // Database Name
    public static final String DATABASE_NAME = "driving_events.db";

  
    private static DataBaseHandler sInstance;
	public SQLiteDatabase mDb;
	private ArrayList<OnDatabaseEditedListener> mEditCallbacks = new ArrayList<>();
	
    public interface OnTaskCompleted {
        void onTaskCompleted(ArrayList<Event> list);
    }
    
    public interface OnGetEventTaskCompleted {
        void onGetEvent(Event ev);
    }
    
    public interface OnGetLocationsListener {
        void onGetLocations(ArrayList<CustomLocation> locations);
    }
    
    public interface OnGetLocationsLogListener {
        void onGetLocations(ArrayList<LogListItemLocation> locations);
    }
    
    public interface OnGetLatestLocationListener {
        void onGetLatestLocation(CustomLocation location);
    }
    
    public interface OnGetSortedLogListener {
        void onTaskCompleted(ArrayList<LogListItemGroup> list);
    }
    
    public interface OnDatabaseEditedListener {
    	void onDatabaseEdited(int action, int rowId);
    }

    public interface OnDatabaseResultListener {
    	void onDatabaseResult(boolean result, int requestCode);
    }
    
    public static DataBaseHandler getInstance(){
		if(sInstance == null) sInstance = getSync();
    	return sInstance;
    }

	private static synchronized DataBaseHandler getSync() {
		if(sInstance == null) sInstance = new DataBaseHandler(App.get());
		return sInstance;
	}

    private DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public String getDatabaseName(){
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
                case 1:
                    break;
                case 2:
                    break;
            }
            
            upgradeTo++;
        }
	}
	
	public boolean isDatabaseOpen(){
        return mDb != null && mDb.isOpen();
	}
	
	public void openDatabase(){
		mDb = getWritableDatabase();
		
		if(mDb.getVersion() < DATABASE_VERSION){
			onUpgrade(mDb, mDb.getVersion(), DATABASE_VERSION);
		}
	}
	
	public void closeDatabase(){
		if(isDatabaseOpen()){
            mDb.close();
            mDb = null;
		}
	}

	
	public void registerOnDatabaseEditedListener(OnDatabaseEditedListener l){
		if(!mEditCallbacks.contains(l)){
			mEditCallbacks.add(l);
		}
	}
	
	public void unregisterOnDatabaseEditedListener(OnDatabaseEditedListener l){
		if(mEditCallbacks.remove(l)){
			Log.i(TAG, TAG + "unregisterOnDatabaseEditedListener() removed callback " + l.toString());
		}
	}
	
	public void notifyCallbacks(int action, int result){
		for(OnDatabaseEditedListener c : mEditCallbacks){
			c.onDatabaseEdited(action, result);
		}
	}
	
	// Adding new event
	public void addNewEvent(Event event){
		final AddEvent task = new AddEvent(event, false, true);
    	task.execute();
	}
	
	public void addNewEventWithLocation(Event event, Location loc, boolean isStartLocation, Context c){
		final AddEvent task = new AddEvent(event, false, isStartLocation, loc, c);
    	task.execute();
	}
	
	public void addLocation(CustomLocation location){
		final AddLocationTask task = new AddLocationTask(location);
		task.execute();
	}
	
    public void updateEvent(Event event, boolean notify){
    	final AddEvent task = new AddEvent(event, true, true);
    	task.execute();
	}
    
    public void updateEventWithLocation(Event event, Location loc, boolean isStartLocation, Context c){
		final AddEvent task = new AddEvent(event, true, isStartLocation, loc, c);
    	task.execute();
	}
    

    
    public void getLatestLocation(OnGetLatestLocationListener l, int eventId){ 
    	if(!isDatabaseOpen()) {
    		openDatabase();
    	}
		new GetLocationsTask(mDb, l, eventId).execute();
    }
    
    public void getLocationInfo(long start, long end, int rowId, int strokeWidth, int lineColor, OnGetLocationInfoListener l) {
    	if(!isDatabaseOpen()) {
    		openDatabase();
    	}
    	GetLocationInfo task = new GetLocationInfo(mDb);
    	task.setMapParams(strokeWidth, lineColor);
    	task.setParams(start, end, rowId);
    	task.setOnGetLocationInfoListener(l);
    	task.execute();
    }
    
    public void getEvent(int rowId, OnGetEventTaskCompleted l, boolean includeLocationInfo){
    	if(!isDatabaseOpen()) {
    		openDatabase();
    	}
		GetEventTask task = new GetEventTask(mDb, rowId);
		task.setOnGetEventTaskCompleted(l);
		task.setIncludeLocationInfo(includeLocationInfo);
		task.execute();
	}
	
	public void getRecordingEvent(OnGetEventTaskCompleted l){
		if(!isDatabaseOpen()) {
    		openDatabase();
    	}
		GetEventTask task = new GetEventTask(mDb);
		task.setOnGetEventTaskCompleted(l);
		task.execute();
	}
	
	public void deleteEvent(int rowId){
		DeleteEventTask task = new DeleteEventTask(rowId);
		task.execute();
	}
	
	public void deleteEvents(ArrayList<Integer> list){
		DeleteEventTask task = new DeleteEventTask(list);
		task.execute();
	}
	
	public void deleteLocations(long startTime, long endTime){
		new DeleteLocationTask(startTime, endTime).execute();
	}
	
	public void deleteLocations(int eventId){
		new DeleteLocationTask(eventId).execute();
	}
	
	public void getAllEvents(OnTaskCompleted listener, boolean ascending, boolean withLocationInfo, boolean calculateRestingEvents){
		if(!isDatabaseOpen()) {
    		openDatabase();
    	}
		String selectQuery = "SELECT " + columnsSelection + " FROM " + TABLE_EVENTS + " ORDER BY " + DataBaseHandlerConstants.KEY_EVENT_START_DATE + (ascending? " ASC" :" DESC");
		GetEventsTask task = new GetEventsTask(mDb);
		task.setCalculateRestingEventsAutomatically(calculateRestingEvents);
		task.setIncludeLocationInfo(withLocationInfo);
		task.setOnTaskCompleted(listener);
		task.execute(selectQuery);
	}
	
	
	public void getEventsByRowIds(OnTaskCompleted listener, int rowids[], boolean calculateRestingEvents){
		if(rowids == null || rowids.length == 0){
			return;
		}
		if(!isDatabaseOpen()) {
    		openDatabase();
    	}
		StringBuilder ids = new StringBuilder();
		ids.append("SELECT " + columnsSelection + " FROM " + TABLE_EVENTS + " WHERE( ");
		
		int lenght = rowids.length;
		
		for(int i = 0; i < lenght; i++){
			if(i != 0){
				ids.append(" OR ");
			}
			ids.append(DataBaseHandlerConstants.KEY_ID + " = " + String.valueOf(rowids[i]));
		}
		ids.append(")");
		new GetEventsTask(mDb)
		.setCalculateRestingEventsAutomatically(calculateRestingEvents)
		.setOnTaskCompleted(listener)
		.execute(ids.toString());
	}
	
	public void getSortedLog(boolean ascending, int sortBy, Resources res, OnGetSortedLogListener l, String category, boolean calculateRestingEvents){
		if(!isDatabaseOpen()) {
    		openDatabase();
    	}
		
		GetSortedLogListTask task = new GetSortedLogListTask(mDb, res.getStringArray(R.array.months), res.getString(R.string.title_week));
		task.setOnGetSortedLogListener(l);
		task.setCalculateRestingEventsAutomatically(calculateRestingEvents);
		task.setParams(category, ascending, sortBy);
		
		task.execute();
	}
	
	
	public void getEventsByTime(long startDate, long endDate, OnTaskCompleted listener, 
			boolean withLocationInfo, boolean includeAutomaticallyCalculatedRestingEvents){
		
	   final String orIsRecording = DataBaseHandlerConstants.KEY_EVENT_RECORDING + " = " + String.valueOf(1);
	  
	   final StringBuilder b = new StringBuilder();
	   if(!isDatabaseOpen()) {
		   openDatabase();
   	   }
	   
	   b.append("SELECT " + columnsSelection + " FROM " + TABLE_EVENTS);
	   b.append(" WHERE(");
	   b.append("(" + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " >= " + String.valueOf(startDate) + " AND " + DataBaseHandlerConstants.KEY_EVENT_END_DATE_IN_MILLIS + " <= "+ String.valueOf(endDate) + ") ");
	   b.append("OR ( " + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " <= " + String.valueOf(startDate) + " AND " + DataBaseHandlerConstants.KEY_EVENT_END_DATE_IN_MILLIS + " >= "+ String.valueOf(startDate) + ") ");
	   b.append("OR ( " + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " >= " + String.valueOf(startDate) + " AND " + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " <= "+ String.valueOf(endDate) + " AND " + DataBaseHandlerConstants.KEY_EVENT_END_DATE_IN_MILLIS + " >= "+ String.valueOf(endDate) + ")");
	  
	   if(endDate > System.currentTimeMillis()) {
		   b.append("OR ( " + orIsRecording + " AND " + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " >= " + String.valueOf(startDate) + ")");
	   }
	   b.append(") ORDER BY " + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " ASC");
	  
	   GetEventsTask task = new GetEventsTask(mDb);
	   task.setCalculateRestingEventsAutomatically(includeAutomaticallyCalculatedRestingEvents);
	   task.setIncludeLocationInfo(withLocationInfo);
	   task.setOnTaskCompleted(listener);
	   task.execute(b.toString());

		
	}
	

	
	public void getAllLocationByTimeFrame(long start, long end, OnGetLocationsListener l){
		if(!isDatabaseOpen()) {
    		openDatabase();
    	}
		final String query = "SELECT " + LOCATION_COLUMNS + " FROM " + TABLE_LOCATIONS 
				+ " WHERE(" + DataBaseHandlerConstants.KEY_LOCATION_TIME + " >= " + String.valueOf(start) + " AND " + DataBaseHandlerConstants.KEY_LOCATION_TIME + " <= " + String.valueOf(end)
				+") ORDER BY " + DataBaseHandlerConstants.KEY_LOCATION_TIME + " ASC";
		new GetLocationsTask(mDb, l, query).execute();

	}
	
	public void getAllLocationByEventId(int id, OnGetLocationsListener l){
		if(!isDatabaseOpen()) {
    		openDatabase();
    	}
		final String query = "SELECT " + LOCATION_COLUMNS + " FROM " + TABLE_LOCATIONS 
				+ " WHERE(" + DataBaseHandlerConstants.KEY_LOCATION_EVENT_ID + " = " + String.valueOf(id) + ") ORDER BY " + DataBaseHandlerConstants.KEY_LOCATION_TIME + " ASC";
		new GetLocationsTask(mDb, l, query).execute();

	}
	
	public void getLocationLog(boolean ascending, OnGetLocationsLogListener l){
		new GetLocationLogTask(ascending, l).execute();
	}
	
    public void checkIfDataBaseContainsThisDate(OnDatabaseResultListener l, Event ev){
    	new CheckIfEventOverlapsOtherEvents(l, ev).execute();
    }
	
	
	
	private class CheckIfEventOverlapsOtherEvents extends AsyncTask<Void, Void, ArrayList<Integer>> {
		private OnDatabaseResultListener mListener;
		private Event mEventToCheck;
		
		public CheckIfEventOverlapsOtherEvents(OnDatabaseResultListener l, Event ev){
			mListener = l;
			mEventToCheck = ev;
		}
		
		@Override
		protected ArrayList<Integer> doInBackground(Void... params) {
			if(!isDatabaseOpen()){
				openDatabase();
			}
			
			ArrayList<Integer> ids = new ArrayList<>();
			final String orIsRecording = DataBaseHandlerConstants.KEY_EVENT_RECORDING + " = " + String.valueOf(1);
			final StringBuilder b = new StringBuilder();
			b.append("SELECT " + columnsSelection + " FROM " + TABLE_EVENTS)
		    .append(" WHERE(")
			.append("(" + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " >= " + String.valueOf(mEventToCheck.getStartDateInMillis()) + " AND " + DataBaseHandlerConstants.KEY_EVENT_END_DATE_IN_MILLIS + " <= "+ String.valueOf(mEventToCheck.getEndDateInMillis()) + ") ")
			.append("OR ( " + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " <= " + String.valueOf(mEventToCheck.getStartDateInMillis()) + " AND " + DataBaseHandlerConstants.KEY_EVENT_END_DATE_IN_MILLIS + " >= "+ String.valueOf(mEventToCheck.getStartDateInMillis()) + ") ")
			.append("OR ( " + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " >= " + String.valueOf(mEventToCheck.getStartDateInMillis()) + " AND " + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " <= "+ String.valueOf(mEventToCheck.getEndDateInMillis()) + " AND " + DataBaseHandlerConstants.KEY_EVENT_END_DATE_IN_MILLIS + " >= "+ String.valueOf(mEventToCheck.getEndDateInMillis()) + ")");
			  
			if(mEventToCheck.getEndDateInMillis() > System.currentTimeMillis()) {
				b.append("OR ( " + orIsRecording + " AND " + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " >= " + String.valueOf(mEventToCheck.getStartDateInMillis()) + ")");
			}
			b.append(") ORDER BY " + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " ASC");
			  
			   
		
			Cursor cursor = mDb.rawQuery(b.toString(), null);
			if (cursor != null && cursor.moveToFirst()) {
				do {
					ids.add(cursor.getInt(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_ID)));
				} while (cursor.moveToNext());
			}

            if(cursor != null) {
                cursor.close();
            }
			return ids;
		}
		
		@Override
		protected void onPostExecute(ArrayList<Integer> result){
			if(mListener == null){
				return;
			}
			if(result != null && !result.isEmpty()){
				if(result.size() == 1 && result.get(0) == mEventToCheck.getRowId()){
					mListener.onDatabaseResult(false, REQUEST_CODE_CHECK_IF_EVENT_OVERLAPS_OTHER_EVENTS);
				} else {
					mListener.onDatabaseResult(true, REQUEST_CODE_CHECK_IF_EVENT_OVERLAPS_OTHER_EVENTS);
				}
			} else {
				mListener.onDatabaseResult(false, REQUEST_CODE_CHECK_IF_EVENT_OVERLAPS_OTHER_EVENTS);
			}
		}
		
	}

	private class AddLocationTask extends AsyncTask<Void, Void, Long> {
		private final CustomLocation mLocation;
		
		public AddLocationTask(CustomLocation location){
			mLocation = location;
		}

		@Override
		protected Long doInBackground(Void... params) {
			if(!isDatabaseOpen()){
				openDatabase();
			}
			final ContentValues values = new ContentValues();
			values.put(DataBaseHandlerConstants.KEY_LOCATION_LATITUDE, mLocation.getLatitude());
			values.put(DataBaseHandlerConstants.KEY_LOCATION_LONGITUDE, mLocation.getLongitude());
			values.put(DataBaseHandlerConstants.KEY_LOCATION_TIME, mLocation.getTime());
			values.put(DataBaseHandlerConstants.KEY_LOCATION_SPEED, mLocation.getSpeed());
			values.put(DataBaseHandlerConstants.KEY_LOCATION_STATUS, mLocation.getStatus());
			values.put(DataBaseHandlerConstants.KEY_LOCATION_EVENT_ID, mLocation.getEventId());
			
			return mDb.insert(TABLE_LOCATIONS, null, values);
		}
				
	}
	
	private class DeleteLocationTask extends AsyncTask<Void, Void, Integer> {
		private long mStart;
		private long mEnd;
		private int mEventId;
		
		public DeleteLocationTask(long start, long end){
			mStart = start;
			mEnd = end;
			mEventId = -1;
		}
		
		public DeleteLocationTask(int eventId){
			mStart = -1;
			mEnd = -1;
			mEventId = eventId;
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			String query = null;
			if(mStart != -1 && mEnd != -1){
				query = "DELETE FROM " + DataBaseHandlerConstants.TABLE_LOCATIONS + " WHERE("
		                    + DataBaseHandlerConstants.KEY_LOCATION_TIME + " >= " + String.valueOf(mStart) + " AND "
		                    + DataBaseHandlerConstants.KEY_LOCATION_TIME + " <= " + String.valueOf(mEnd) + ")";
			} else if(mEventId != -1){
				query = "DELETE FROM " + DataBaseHandlerConstants.TABLE_LOCATIONS + " WHERE("
	                    + DataBaseHandlerConstants.KEY_LOCATION_EVENT_ID + " = " + String.valueOf(mEventId) + ")";
			}
			
			if(!isDatabaseOpen()){
				openDatabase();
			}
			
			if(query != null) {
				mDb.execSQL(query);
			}
			
			return 1;
		}
		
		@Override
		protected void onPostExecute(Integer result){
			notifyCallbacks(ACTION_DELETE_LOCATION, result);
		}
	}
	
	private class DeleteEventTask extends AsyncTask<Void, Void, Integer> {
		private ArrayList<Integer> mRowIds;
		private int mRowId;
		
		public DeleteEventTask(ArrayList<Integer> ids){
			mRowIds = ids;
		}
		
		public DeleteEventTask(int rowid){
			mRowId = rowid;
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			if(!isDatabaseOpen()){
				openDatabase();
			}
			if(mRowIds == null){
				return mDb.delete(TABLE_EVENTS, DataBaseHandlerConstants.KEY_ID + "=?", new String[] {String.valueOf(mRowId)});
			} else {
				for(Integer rowId : mRowIds){
					if(rowId != null){
						mDb.delete(TABLE_EVENTS, DataBaseHandlerConstants.KEY_ID + "=?", new String[] {String.valueOf(rowId)});
					}
				}
				return mRowIds.size();
			}
		
		}
		
		@Override
		protected void onPostExecute(Integer result){
			notifyCallbacks(ACTION_DELETE_EVENT, result);
		}
		
	}
	
	private class AddEvent extends AsyncTask<Void, Integer, Integer> {
		private Event mEvent;
		private boolean mUpdate;
		private boolean mNotify;
		private boolean mStartLocation;
		
		private int mEventId;
		private Location mLocation;
		private Context mContext;
		
		public AddEvent(Event event, boolean update, boolean notify){
			mEvent = event;
			mUpdate = update;
			mNotify = notify;
		}
		
		public AddEvent(Event event, boolean update, boolean start, Location loc, Context context){
			mEvent = event;
			mUpdate = update;
			mNotify = true;
			mStartLocation = start;
			mLocation = loc;
			mContext = context;
			
		}

		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			int rowId = progress[0];
			if(mNotify){
				mNotify = false;
				for (OnDatabaseEditedListener c : mEditCallbacks) {
					c.onDatabaseEdited(mUpdate ? ACTION_UPDATE_EVENT : ACTION_EDIT_EVENT, rowId);
				}
			}
		}

		@Override
		protected Integer doInBackground(Void... params) {
			if(!isDatabaseOpen()){
				openDatabase();
			}
			final ContentValues values = new ContentValues();
			
			
			mEventId = mEvent.getRowId();
			values.put(DataBaseHandlerConstants.KEY_EVENT_START_HOUR, mEvent.getStartHour());
			values.put(DataBaseHandlerConstants.KEY_EVENT_START_MINUTE, mEvent.getStartMinutes());
			values.put(DataBaseHandlerConstants.KEY_EVENT_END_HOUR, mEvent.getEndHour());
			values.put(DataBaseHandlerConstants.KEY_EVENT_END_MINUTE, mEvent.getEndMinutes());
			values.put(DataBaseHandlerConstants.KEY_EVENT_TYPE, mEvent.getEventType());
			values.put(DataBaseHandlerConstants.KEY_EVENT_START_DATE, DateCalculations.formatDate(new Date(mEvent.getStartDateInMillis()), mEvent.getStartHour(), mEvent.getStartMinutes()));
			values.put(DataBaseHandlerConstants.KEY_EVENT_END_DATE, DateCalculations.formatDate(new Date(mEvent.getEndDateInMillis()), mEvent.getEndHour(), mEvent.getEndMinutes()));
			values.put(DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS, DateCalculations.formatDateToMillis(mEvent.getStartDateInMillis(), mEvent.getStartHour(), mEvent.getStartMinutes()));
			values.put(DataBaseHandlerConstants.KEY_EVENT_END_DATE_IN_MILLIS, DateCalculations.formatDateToMillis(mEvent.getEndDateInMillis(), mEvent.getEndHour(), mEvent.getEndMinutes()));
			values.put(DataBaseHandlerConstants.KEY_EVENT_MILEAGE_START, mEvent.getMileageStart());
			values.put(DataBaseHandlerConstants.KEY_EVENT_MILEAGE_END, mEvent.getMileageEnd());
			values.put(DataBaseHandlerConstants.KEY_EVENT_RECORDING, mEvent.isRecordingEvent());
			values.put(DataBaseHandlerConstants.KEY_EVENT_NOTE, mEvent.getNote());
			if(mLocation == null){
				values.put(DataBaseHandlerConstants.KEY_EVENT_START_LOCATION, mEvent.getStartLocation());
			    values.put(DataBaseHandlerConstants.KEY_EVENT_END_LOCATION, mEvent.getEndLocation());
			}
			values.put(DataBaseHandlerConstants.KEY_EVENT_IS_SPLIT_BREAK, mEvent.isSplitBreak() ? 1 : 0);
			
			
			int result = -1;
			try{
				if(mUpdate){
					result = mEventId;
				    mDb.update(TABLE_EVENTS, values, DataBaseHandlerConstants.KEY_ID + "=?", new String[] {String.valueOf(mEventId)});
				} else {
					result = (int) mDb.insert(TABLE_EVENTS, null, values);
				}
			} catch(Exception e) {}
			
			final int rowId = result;

			if(mLocation != null && rowId != -1){
				publishProgress(rowId);

				String address = DatabaseLocationUtils.getAdressForLocation(mContext, mLocation.getLatitude(), mLocation.getLongitude());

				if(address != null && !address.isEmpty()){
					address = address.replaceAll("'", "''");
					DatabaseUtils.sqlEscapeString(address);

					try{
						if(mStartLocation){
							mDb.execSQL("UPDATE " + TABLE_EVENTS + " SET " + DataBaseHandlerConstants.KEY_EVENT_START_LOCATION + " = '" + address + "' WHERE " + DataBaseHandlerConstants.KEY_ID + " = " + String.valueOf(rowId));
						} else {
							mDb.execSQL("UPDATE " + TABLE_EVENTS + " SET " + DataBaseHandlerConstants.KEY_EVENT_END_LOCATION + " = '" + address + "' WHERE " + DataBaseHandlerConstants.KEY_ID + " = " + String.valueOf(rowId));
						}
					} catch (Exception e) {}

				}

			}


			return rowId;
		}
		
		@Override
		protected void onPostExecute(Integer result){
			if (mNotify && result != -2) {
				for (OnDatabaseEditedListener c : mEditCallbacks) {
					c.onDatabaseEdited(mUpdate ? ACTION_UPDATE_EVENT : ACTION_EDIT_EVENT, result);
				}
			}
			
			mContext = null;
		}
		
	}
	
	
	
	public class GetLocationLogTask extends AsyncTask<Void, Void, ArrayList<LogListItemLocation>> {
		public final String QUERY = "SELECT " + LOCATION_COLUMNS + " FROM " + TABLE_LOCATIONS + " ORDER BY " + DataBaseHandlerConstants.KEY_LOCATION_TIME + " ASC";
		
		private OnGetLocationsLogListener mListener;
		final boolean mAscending;
		
		public GetLocationLogTask(boolean ascending, OnGetLocationsLogListener l){
			mListener = l;
			mAscending = ascending;
			
		}
		
		@Override
		protected ArrayList<LogListItemLocation> doInBackground(Void... params) {
			ArrayList<LogListItemLocation> list = new ArrayList<>();
			
			
			
			if(mDb == null || !mDb.isOpen()){
				openDatabase();
			}
			final Cursor cursor = mDb.rawQuery(QUERY, null);
			   
			double distance = 0;
			
			double latitude = -1;
    		double longitude = -1;
    		long start = -1;
    		
    		if (cursor != null && cursor.moveToFirst()) {
				do {
					int type = cursor.getInt(5);
					
					if(latitude != -1 && longitude != -1){
						distance += Utils.calculateDistance(latitude, longitude, cursor.getDouble(0), cursor.getDouble(1));
					}
					
					latitude = cursor.getDouble(0);
					longitude = cursor.getDouble(1);
					
					if((type == CustomLocation.LOCATION_STATUS_STOP || cursor.isLast()) && start != -1){
						int duration = DateCalculations.convertDatesToMinutes(start, cursor.getLong(4));
						list.add(new LogListItemLocation(distance, duration, start, cursor.getLong(4)));
						distance = 0;
						latitude = -1;
			    		longitude = -1;
			    		start = -1;
					} else if(type == CustomLocation.LOCATION_STATUS_START) {
						start = cursor.getLong(4);
						distance = 0;
						latitude = cursor.getDouble(0);
			    		longitude = cursor.getDouble(1);
			    		
					} 
					
				} while(cursor.moveToNext());
				
				cursor.close();

    		}
				
			
			return list;
		}
		

		
		@Override
		protected void onPostExecute(ArrayList<LogListItemLocation> list){
			if(mListener != null){
				mListener.onGetLocations(list);
			}
			mListener = null;
		}
		
	}
	
	
	
	

}
