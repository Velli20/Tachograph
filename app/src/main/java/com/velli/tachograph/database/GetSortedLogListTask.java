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
import static com.velli.tachograph.database.DataBaseHandlerConstants.columnsSelection;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.velli.tachograph.DateCalculations;
import com.velli.tachograph.Event;
import com.velli.tachograph.collections.LogListItemGroup;
import com.velli.tachograph.database.DataBaseHandler.OnGetSortedLogListener;

public class GetSortedLogListTask extends AsyncTask<Void, Void, ArrayList<LogListItemGroup>> {
	public static final int SORT_BY_MONTH = 0;
    public static final int SORT_BY_WEEK = 1;
    public static final int SORT_BY_DAY = 2;
    
	private boolean mCalculateRestingEvents = false;
	
	private int mSortBy;
	private int mMinuteSummary = 0;
	
	private String[] mMonths;
	private String mWeek;
	private String selectQuery;
	
	private GregorianCalendar mPreviousDate = null;
	private GregorianCalendar newDate = new GregorianCalendar();
	private OnGetSortedLogListener mListener;
	private SQLiteDatabase mDb;
	private ArrayList<Event> mChildList = new ArrayList<>();
	
	public GetSortedLogListTask(SQLiteDatabase db, String months[], String titleWeek){
		mDb = db;
		mMonths = months;
		mWeek = titleWeek;		
	}
	
	public GetSortedLogListTask setOnGetSortedLogListener(OnGetSortedLogListener l) {
		mListener = l;
		return this;
	}
	
	public GetSortedLogListTask setCalculateRestingEventsAutomatically(boolean s) {
		mCalculateRestingEvents = s;
		return this;
	}
	
	public GetSortedLogListTask setParams(String category, boolean ascending, int sortBy) {
		mSortBy = sortBy;
		
		StringBuilder b = new StringBuilder();
		b.append("SELECT " + columnsSelection + " FROM " + TABLE_EVENTS);
		if(category != null){
			b.append(" WHERE( " + category + " )");
		} 
		b.append(" ORDER BY " + DataBaseHandlerConstants.KEY_EVENT_START_DATE);
		b.append(ascending? " ASC" :" DESC");
		
		selectQuery = b.toString();
		return this;
	}
	
	@Override
	protected ArrayList<LogListItemGroup> doInBackground(Void... params) {
		if(mDb == null || ! mDb.isOpen() || selectQuery == null){
			return null;
		} 
		final Cursor cursor = mDb.rawQuery(selectQuery, null);
		final ArrayList<LogListItemGroup> groupList = new ArrayList<>();

		
		
		
	    if (cursor != null && cursor.moveToFirst()){
	    	Event previousEvent = null;
	    	do {
	    		Event event = DatabaseEventUtils.getEvent(mDb, cursor, false);
	    	  
	    		if(mCalculateRestingEvents && previousEvent != null) {
					ArrayList<Event> autoEvents = DatabaseEventUtils.calculateRestingEventBetweenTwoEvents(event, previousEvent);
	    			
	    			if(autoEvents != null) {
						for(Event e : autoEvents) {
							addEvent(groupList, e);
						}
	    			}
	    		} 
	    	   
	    		addEvent(groupList, event);
	    	    previousEvent = event;
	    	} while(cursor.moveToNext());
	    	
	    	cursor.close();
	    	
	    	if(!mChildList.isEmpty()){
	    		groupList.add(createGroup(mMinuteSummary, mPreviousDate, mChildList));
	    	}
	    	
		}
		return groupList;
	}
	
	private void addEvent(ArrayList<LogListItemGroup> groupList, Event event) {

		newDate.setTimeInMillis(event.getStartDateInMillis());

		int minutes = DateCalculations.convertDatesToMinutes(event.getStartDateInMillis(), event.getEndDateInMillis());
		if (mPreviousDate == null) {
			mPreviousDate = new GregorianCalendar();
			mPreviousDate.setTimeInMillis(event.getStartDateInMillis());
			mChildList.add(event);
			mMinuteSummary += minutes;
		} else if (sort(mPreviousDate, newDate, mSortBy)) {
			groupList.add(createGroup(mMinuteSummary, mPreviousDate, mChildList));
			mPreviousDate.setTimeInMillis(event.getStartDateInMillis());

			mChildList = new ArrayList<>();
			mChildList.add(event);

			mMinuteSummary = 0;
			mMinuteSummary += minutes;
		} else {
			mMinuteSummary += minutes;
			mChildList.add(event);
		}
	}
	
	private LogListItemGroup createGroup(int minuteSummary, GregorianCalendar c, ArrayList<Event> list){
		final LogListItemGroup group = new LogListItemGroup();
		final String summary = DateCalculations.convertMinutesToTimeString(minuteSummary);
		
		if(mSortBy == SORT_BY_MONTH){
			group.setTitle(mMonths[c.get(GregorianCalendar.MONTH)], summary);
        } else if(mSortBy == SORT_BY_WEEK){
        	group.setTitle(mWeek + " " + c.get(GregorianCalendar.WEEK_OF_YEAR), summary);
        } else if(mSortBy == SORT_BY_DAY){
        	group.setTitle(c.get(GregorianCalendar.DAY_OF_MONTH) + ". " + new DateFormatSymbols().getMonths()[c.get(GregorianCalendar.MONTH)], summary);
        }
		group.setChildList(list);
		return group;
	}
	
	
	
	@Override
	public void onPostExecute(ArrayList<LogListItemGroup> result){
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
