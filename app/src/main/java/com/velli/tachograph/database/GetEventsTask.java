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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.velli.tachograph.Event;
import com.velli.tachograph.database.DataBaseHandler.OnTaskCompleted;

public class GetEventsTask extends AsyncTask<String, String, ArrayList<Event>> {
	private OnTaskCompleted mAsyncListener;
	private boolean mIncludeLocationInfo = false;
	private boolean mCalculateRestingEvents = false;
	
	private SQLiteDatabase mDb;
	
	
	public GetEventsTask(SQLiteDatabase db){
		mDb = db;
	}
	
	public GetEventsTask setIncludeLocationInfo(boolean include) {
		mIncludeLocationInfo = include;
		return this;
	}

	public GetEventsTask setCalculateRestingEventsAutomatically(boolean s) {
		mCalculateRestingEvents = s;
		return this;
	}
	
	public GetEventsTask setOnTaskCompleted(OnTaskCompleted l) {
		mAsyncListener = l;
		return this;
	}
	
	@Override
	protected ArrayList<Event> doInBackground(String... params) {
		if(mDb == null || !mDb.isOpen()){
			return null;
		}
		
		ArrayList<Event> list = new ArrayList<>();
		Cursor cursor;
		
		String selectQuery = params[0];
		cursor = mDb.rawQuery(selectQuery, null);
		
	    if (cursor != null && cursor.moveToFirst()){
	    	Event previousEvent = null;
	    	
	    	do {
	    		
	    		Event event = DatabaseEventUtils.getEvent(mDb, cursor, mIncludeLocationInfo);
	    		
				if(mCalculateRestingEvents && previousEvent != null) {
	    			ArrayList<Event> autoEvents = DatabaseEventUtils.calculateRestingEventBetweenTwoEvents(event, previousEvent);
	    			
	    			if(autoEvents != null) {
						list.addAll(autoEvents);

	    			}
            		
	    		} 
				
				list.add(event);
				
				previousEvent = event;
			    
	    	} while(cursor.moveToNext());
	    	cursor.close();
		}
		return list;
	}
	
	@Override
	protected void onPostExecute(ArrayList<Event> list){
		if(mAsyncListener != null){
			mAsyncListener.onTaskCompleted(list);
		}
		mAsyncListener = null;
	}

}
