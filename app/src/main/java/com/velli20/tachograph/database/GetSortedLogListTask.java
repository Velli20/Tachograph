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

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.velli20.tachograph.DateUtils;
import com.velli20.tachograph.Event;
import com.velli20.tachograph.collections.ListItemLogGroup;
import com.velli20.tachograph.database.DataBaseHandler.OnGetSortedLogListener;

public class GetSortedLogListTask extends AsyncTask<Void, Void, ArrayList<ListItemLogGroup>> {
	public static final int SORT_BY_MONTH = 0;
    public static final int SORT_BY_WEEK = 1;
    public static final int SORT_BY_DAY = 2;

	private int mSortBy;
	private int mMinuteSummary = 0;
	
	private String[] mMonths;
	private String mWeek;
	private String selectQuery;

    private boolean mIncludeLocationInfo = false;


    private GregorianCalendar mPreviousDate;
    private GregorianCalendar mNewDate = new GregorianCalendar();

	private OnGetSortedLogListener mListener;
	private SQLiteDatabase mDb;
	private ArrayList<Event> mChildList = new ArrayList<>();
	
	public GetSortedLogListTask(SQLiteDatabase db, String query, String months[], String titleWeek){
		mDb = db;
		selectQuery = query;
		mMonths = months;
		mWeek = titleWeek;		
	}
	
	public GetSortedLogListTask setOnGetSortedLogListener(OnGetSortedLogListener l) {
		mListener = l;
		return this;
	}

    public GetSortedLogListTask setIncludeLoggedRouteDistance(boolean include) {
        mIncludeLocationInfo = include;
        return this;
    }

	public GetSortedLogListTask setSortBy(int sortBy) {
		mSortBy = sortBy;
        return this;
	}
	
	@Override
	protected ArrayList<ListItemLogGroup> doInBackground(Void... params) {
		if(mDb == null || ! mDb.isOpen() || selectQuery == null){
			return null;
		} 
		final Cursor cursor = mDb.rawQuery(selectQuery, null);
		final ArrayList<ListItemLogGroup> groupList = new ArrayList<>();


	    if (cursor != null && cursor.moveToFirst()){
	    	do {
	    		Event event = DatabaseEventUtils.getEvent(cursor);

				boolean hasLoggedRoute = DatabaseLocationUtils.checkForLoggedRoute(mDb, event);

				if (hasLoggedRoute && mIncludeLocationInfo) {
					event.setHasLoggedRoute(true);
					event.setDrivenDistance(DatabaseLocationUtils.getLoggedRouteDistance(mDb, event.getRowId()) / 1000);
				}

	    		addEvent(groupList, event);
	    	} while(cursor.moveToNext());
	    	
	    	cursor.close();
	    	
	    	if(!mChildList.isEmpty()){
	    		groupList.add(createGroup(mMinuteSummary, mPreviousDate, mChildList));
	    	}
	    	
		}
		return groupList;
	}
	
	private void addEvent(ArrayList<ListItemLogGroup> groupList, Event event) {

        mNewDate.setTimeInMillis(event.getStartDateInMillis());

		int minutes = DateUtils.convertDatesToMinutes(event.getStartDateInMillis(), event.getEndDateInMillis());
		if (mPreviousDate == null) {
			mPreviousDate = new GregorianCalendar();
            mPreviousDate.setTimeInMillis(event.getStartDateInMillis());

			mChildList.add(event);
			mMinuteSummary += minutes;
		} else if (sort(mPreviousDate, mNewDate, mSortBy)) {
			groupList.add(createGroup(mMinuteSummary, mPreviousDate, mChildList));
            mPreviousDate.setTimeInMillis(event.getStartDateInMillis());

			mChildList = new ArrayList<>();
			mChildList.add(event);

			mMinuteSummary = minutes;
		} else {
			mMinuteSummary += minutes;
			mChildList.add(event);
		}
	}
	
	private ListItemLogGroup createGroup(int minuteSummary, GregorianCalendar date, ArrayList<Event> list){
		final ListItemLogGroup group = new ListItemLogGroup();
		final String summary = DateUtils.convertMinutesToTimeString(minuteSummary);
		
		if(mSortBy == SORT_BY_MONTH){
			group.setTitle(mMonths[date.get(GregorianCalendar.MONTH)], summary);
        } else if(mSortBy == SORT_BY_WEEK){
        	group.setTitle(mWeek + " " + date.get(GregorianCalendar.WEEK_OF_YEAR), summary);
        } else if(mSortBy == SORT_BY_DAY){
        	group.setTitle(date.get(GregorianCalendar.DAY_OF_MONTH) + ". " + DateFormatSymbols.getInstance(Locale.getDefault()).getMonths()[date.get(GregorianCalendar.MONTH)], summary);
        }
		group.setChildList(list);
		return group;
	}
	
	
	
	@Override
	public void onPostExecute(ArrayList<ListItemLogGroup> result){
		if(mListener != null){
			mListener.onTaskCompleted(result);
		}
		mDb = null;
	}
	
	private static boolean sort(GregorianCalendar oldestDate, GregorianCalendar newDate, int sortBy){
		
		boolean sort = false;
		
		if(sortBy == SORT_BY_MONTH){
			sort = oldestDate.get(GregorianCalendar.MONTH) != newDate.get(GregorianCalendar.MONTH);
        } else if(sortBy == SORT_BY_WEEK){
         	sort = oldestDate.get(GregorianCalendar.WEEK_OF_YEAR) != newDate.get(GregorianCalendar.WEEK_OF_YEAR);
        } else if(sortBy == SORT_BY_DAY){
         	sort = oldestDate.get(GregorianCalendar.DAY_OF_MONTH) != newDate.get(GregorianCalendar.DAY_OF_MONTH) ||
         			oldestDate.get(GregorianCalendar.MONTH) != newDate.get(GregorianCalendar.MONTH);
        }
		return sort;
	}
}
