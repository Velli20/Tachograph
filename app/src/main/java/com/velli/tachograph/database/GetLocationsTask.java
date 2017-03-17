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

import static com.velli.tachograph.database.DataBaseHandlerConstants.LOCATION_COLUMNS;
import static com.velli.tachograph.database.DataBaseHandlerConstants.TABLE_LOCATIONS;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.AsyncTask;

import com.velli.tachograph.database.DataBaseHandler.OnGetLatestLocationListener;
import com.velli.tachograph.database.DataBaseHandler.OnGetLocationsListener;
import com.velli.tachograph.location.CustomLocation;

public class GetLocationsTask extends AsyncTask<Void, Void, ArrayList<CustomLocation>>{
	public static final String LOCATION_DEFAULT_QUERY = "SELECT " + LOCATION_COLUMNS + " FROM " + TABLE_LOCATIONS + " ORDER BY " + DataBaseHandlerConstants.KEY_LOCATION_TIME + " ASC";
	public static final String LOCATION_QUERY_LATEST =  "SELECT " + LOCATION_COLUMNS + " FROM " + TABLE_LOCATIONS + " ORDER BY " + DataBaseHandlerConstants.KEY_LOCATION_TIME + " DESC LIMIT 1";

	
	private OnGetLocationsListener mListener;
	private OnGetLatestLocationListener mOnGetLatestLocationListener;
	private String mQuery;
	private SQLiteDatabase mDb;
	
	public GetLocationsTask(SQLiteDatabase db, OnGetLocationsListener l, String query){
		mListener = l;
		mQuery = query;
		mDb = db;
	}
	
	public GetLocationsTask(SQLiteDatabase db, OnGetLatestLocationListener l, String query){
		mOnGetLatestLocationListener = l;
		mQuery = query;
		mDb = db;
	}
	
	public GetLocationsTask(SQLiteDatabase db, OnGetLatestLocationListener l, int eventId){
		mOnGetLatestLocationListener = l;
		mQuery = "SELECT " + LOCATION_COLUMNS + " FROM " + TABLE_LOCATIONS + " WHERE( " 
				+ DataBaseHandlerConstants.KEY_LOCATION_EVENT_ID + " = " + String.valueOf(eventId) + ") "
		        + " ORDER BY " + DataBaseHandlerConstants.KEY_LOCATION_TIME + " DESC LIMIT 1";
		mDb = db;
	}
	
	@Override
	protected ArrayList<CustomLocation> doInBackground(Void... params) {
		if(mDb == null || !mDb.isOpen()){
			return null;
		}
		
		final ArrayList<CustomLocation> list = new ArrayList<>();
	    final Cursor cursor = mDb.rawQuery(mQuery, null);
	    
	    if (cursor != null && cursor.moveToFirst()){
	    	do {
	    		CustomLocation loc = new CustomLocation(LocationManager.GPS_PROVIDER);
	    		loc.setLatitude(cursor.getDouble(0));
	    		loc.setLongitude(cursor.getDouble(1));
	    		loc.setSpeed((float)cursor.getDouble(3));
	    		loc.setTime(cursor.getLong(4));
	    		loc.setStatus(cursor.getInt(5));
	    		list.add(loc);
	    	
	    	} while(cursor.moveToNext());
	    }
	    
	    if(cursor != null){
	    	cursor.close();
	    }
	    
		return list;
	}
	
	@Override
	protected void onPostExecute(ArrayList<CustomLocation> list){
		mDb = null;
		if(mListener != null){
			mListener.onGetLocations(list);
			mListener = null;
		} else if(mOnGetLatestLocationListener != null){
			if(!list.isEmpty()){
				mOnGetLatestLocationListener.onGetLatestLocation(list.get(0));
			} else {
				mOnGetLatestLocationListener.onGetLatestLocation(null);
			}
		}
	}
	
}
