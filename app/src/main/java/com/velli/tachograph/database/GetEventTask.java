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

import static com.velli.tachograph.database.DataBaseHandlerConstants.TABLE_EVENTS;
import static com.velli.tachograph.database.DataBaseHandlerConstants.TABLE_LOCATIONS;
import static com.velli.tachograph.database.DataBaseHandlerConstants.columns;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import com.velli.tachograph.Event;
import com.velli.tachograph.database.DataBaseHandler.OnGetEventTaskCompleted;

public class GetEventTask extends AsyncTask<String, String, Event>{
	private OnGetEventTaskCompleted mListener;
	private SQLiteDatabase mDb;
	private int mRowId;
	private boolean mRec;
	private boolean mIncludeLocationInfo = false;
	
	/**Returns currently recording event*/
	public GetEventTask(SQLiteDatabase db){
		mDb = db;
		mRec = true;
	}
	
	/**Returns event with given rowId*/
	public GetEventTask(SQLiteDatabase db, int rowId){
		mDb = db;
		mRowId = rowId;
		mRec = false;
	}
	
	public GetEventTask setOnGetEventTaskCompleted(OnGetEventTaskCompleted l) {
		mListener = l;
		return this;
	}
	
	/**Returns driven distance if there is logged route*/
	public GetEventTask setIncludeLocationInfo(boolean include) {
		mIncludeLocationInfo = include;
		return this;
	}
	
	@Override
	protected Event doInBackground(String... params) {
		if(mDb == null || !mDb.isOpen()){
			return null;
		}

		final Event event;
		final Cursor cursor;
		if(mRec){
			cursor = mDb.query(TABLE_EVENTS, columns, DataBaseHandlerConstants.KEY_EVENT_RECORDING + " =? ", new String[] { String.valueOf(1) }, null, null, null, null);
		} else {
			cursor = mDb.query(TABLE_EVENTS, columns, DataBaseHandlerConstants.KEY_ID + "=?", new String[] { String.valueOf(mRowId) }, null, null, null, null);
		}
			
		if (cursor != null && cursor.moveToFirst()){
			event = DatabaseEventUtils.getEvent(mDb, cursor, mIncludeLocationInfo);
			
			String locQuery = "SELECT count(*) FROM " + TABLE_LOCATIONS + " WHERE (" + DataBaseHandlerConstants.KEY_LOCATION_EVENT_ID + " = " + String.valueOf(event.getRowId()) + ") LIMIT 3";
			final Cursor locCursor = mDb.rawQuery(locQuery, null);
			
			if(locCursor != null && locCursor.moveToFirst()){
				event.setHasLoggedRoute(locCursor.getInt(0) > 1);
			} 
			if(locCursor != null) {
				locCursor.close();
			}
			cursor.close();
		} else if(cursor != null){
			cursor.close();
			return null;
		} else {
			return null;
		}
			
		return event;
	}
	
	@Override
	protected void onPostExecute(Event ev){
		if(mListener != null){
			mListener.onGetEvent(ev);
		}
		mListener = null;
		mDb = null;
	}

}
