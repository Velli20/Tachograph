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

import com.velli20.tachograph.Event;

public class DatabaseEventUtils {


	
	public static Event getEvent(Cursor cursor) {
		if(cursor == null) {
			return null;
		}
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
		event.setRowId(cursor.getInt(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_ID)));

		return event;
	}

}
