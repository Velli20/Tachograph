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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import com.velli20.tachograph.Event;
import com.velli20.tachograph.database.DataBaseHandler.OnGetEventTaskCompleted;

import java.util.ArrayList;

public class GetEventTask extends AsyncTask<String, String, ArrayList<Event>>{
	private OnGetEventTaskCompleted mListener;
    private DataBaseHandler.OnTaskCompleted mAsyncListener;

	private SQLiteDatabase mDb;
	private boolean mIncludeLocationInfo = false;

    private String mQuery;
	

	public GetEventTask(SQLiteDatabase db, String query){
		mDb = db;
		mQuery = query;
	}
	
	/* Includes also logged route distance if there is logged location points
	 * in database.
	 */
	public GetEventTask setIncludeLoggedRouteDistance(boolean include) {
		mIncludeLocationInfo = include;
		return this;
	}


    public GetEventTask setOnGetEventTaskCompleted(OnGetEventTaskCompleted l) {
        mListener = l;
        return this;
    }

    public GetEventTask setOnTaskCompleted(DataBaseHandler.OnTaskCompleted l) {
        mAsyncListener = l;
        return this;
    }
	
	@Override
	protected ArrayList<Event> doInBackground(String... params) {
		if(mDb == null || !mDb.isOpen() || mQuery == null){
			return null;
		}

		mDb.beginTransaction();

        final ArrayList<Event> list = new ArrayList<>();
        final Cursor cursor = mDb.rawQuery(mQuery, null);

        Event event;

		if (cursor != null && cursor.moveToFirst()){

            do {
                event = DatabaseEventUtils.getEvent(cursor);

                boolean hasLoggedRoute = DatabaseLocationUtils.checkForLoggedRoute(mDb, event);

                if (hasLoggedRoute && mIncludeLocationInfo) {
                    event.setHasLoggedRoute(true);
                        /* Get distance */
                    event.setDrivenDistance(DatabaseLocationUtils.getLoggedRouteDistance(mDb, event.getRowId()));
                }

                if (event != null) {
                    list.add(event);
                }

            } while(cursor.moveToNext());


		}
        if (cursor != null) {
            cursor.close();
        }

        mDb.setTransactionSuccessful();
        mDb.endTransaction();
			
		return list;
	}
	
	@Override
	protected void onPostExecute(ArrayList<Event> ev){
        if(mAsyncListener != null) {
            /* User requested to return list of Events */
            mAsyncListener.onTaskCompleted(ev);
        }
        if(mListener != null){
            /* User requested only one Event */
            if(ev != null && !ev.isEmpty()) {
                mListener.onGetEvent(ev.get(0));
            } else {
                mListener.onGetEvent(null);
            }
		}
        mAsyncListener = null;
		mListener = null;
		mDb = null;
	}



}
