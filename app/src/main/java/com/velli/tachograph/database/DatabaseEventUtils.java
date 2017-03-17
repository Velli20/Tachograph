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


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.velli.tachograph.Event;
import com.velli.tachograph.restingtimes.Constants;

import java.util.ArrayList;
import java.util.Collections;

public class DatabaseEventUtils {

	
	public static ArrayList<Event> calculateRestingEventBetweenTwoEvents(Event event, Event previousEvent) {
		if(event == null || previousEvent == null) {
			return null;
		}
		final ArrayList<Event> list = new ArrayList<>();
		boolean descending =  previousEvent.getStartDateInMillis() > event.getStartDateInMillis() ;
		
		long difference = (descending ? previousEvent.getStartDateInMillis() : event.getStartDateInMillis()) -
				          (descending ? event.getEndDateInMillis() : previousEvent.getEndDateInMillis());
		int mins = (int)((difference / 1000) / 60);

		boolean iterated = false;

		if(mins > Constants.LIMIT_WEEKLY_REST_MIN) {
			int split = (mins - Constants.LIMIT_WEEKLY_REST_MIN) / Constants.ONE_DAY_PERIOD_IN_MINS;


			if(split >= 1) {
				long start = descending ? event.getEndDateInMillis() : previousEvent.getEndDateInMillis();

				for(int i = 0; i <= split; i++) {
					Event e = new Event();
					if(i == split) {
                        e.setEventType(Event.EVENT_TYPE_WEEKLY_REST);
                        e.setStartDate(start);
                        e.setEndDate(descending ? previousEvent.getStartDateInMillis() : event.getStartDateInMillis());
					} else {
                        e.setEventType(Event.EVENT_TYPE_DAILY_REST);
                        e.setStartDate(start);
                        e.setEndDate(start + Constants.LIMIT_DAILY_REST_MIN * 60 * 1000);
                        start += (Constants.ONE_DAY_PERIOD_IN_MILLIS);
					}
					list.add(e);
				}
				iterated = true;
                if(descending) {
                    Collections.reverse(list);
                }
			}
		}
		if(mins >= Constants.LIMIT_DAILY_REST_SPLIT_1 && !iterated) {
			Event autoEvent = new Event();
			
			autoEvent.setStartDate(descending ? event.getEndDateInMillis() : previousEvent.getEndDateInMillis());
			autoEvent.setEndDate(descending ? previousEvent.getStartDateInMillis() : event.getStartDateInMillis());
			
			if(mins >= Constants.LIMIT_WEEKLY_REST_MIN) {
				autoEvent.setEventType(Event.EVENT_TYPE_WEEKLY_REST);
			} else {
				autoEvent.setEventType(Event.EVENT_TYPE_DAILY_REST);
			}
			list.add(autoEvent);
		}
		return list;
	}
	
	public static Event getEvent(SQLiteDatabase mDb, Cursor cursor, boolean withLocationInfo) {
		Event event = new Event();
		
		event.setEventType(cursor.getInt(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_EVENT_TYPE)));
		event.setStartTime(cursor.getInt(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_EVENT_START_HOUR)), cursor.getInt(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_EVENT_START_MINUTE)));
		event.setEndTime(cursor.getInt(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_EVENT_END_HOUR)), cursor.getInt(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_EVENT_END_MINUTE)));
		event.setStartDate(Long.parseLong(cursor.getString(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS))));
		event.setEndDate(Long.parseLong(cursor.getString(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_EVENT_END_DATE_IN_MILLIS))));
		event.setMileageStart(cursor.getInt(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_EVENT_MILEAGE_START)));
		event.setMileageEnd(cursor.getInt(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_EVENT_MILEAGE_END)));
		event.setRecording(cursor.getInt(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_EVENT_RECORDING)) == Event.EVENT_LOGGING_IN_PROGRESS);
		event.setNote(cursor.getString(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_EVENT_NOTE)));
		event.setStartLocation(cursor.getString(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_EVENT_START_LOCATION)));
		event.setEndLocation(cursor.getString(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_EVENT_END_LOCATION)));
		event.setAsSplitBreak(cursor.getInt(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_EVENT_IS_SPLIT_BREAK)) == 1);
		event.setRowId(cursor.getInt(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_ID)));

		if(withLocationInfo) {
			event.setDrivenDistance(DatabaseLocationUtils.getDistanceInKmBetweenTimes(mDb, event.getStartDateInMillis(), event.getEndDateInMillis()));
		}
		
		return event;
	}
}
