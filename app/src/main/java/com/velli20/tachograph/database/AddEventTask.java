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

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.velli20.tachograph.DateUtils;
import com.velli20.tachograph.Event;

import java.util.Date;



public class AddEventTask extends AsyncTask<Void, Integer, Integer> {
    private boolean mUpdate;

    private Event mEvent;
    private SQLiteDatabase mDb;
    private OnDatabaseActionCompletedListener mListener;

    public AddEventTask(SQLiteDatabase db, Event event, boolean update) {
        mDb = db;
        mEvent = event;
        mUpdate = update;
    }

    public AddEventTask setOnDatabaseActionCompletedListener(OnDatabaseActionCompletedListener l) {
        mListener = l;
        return this;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        if (mDb == null || !mDb.isOpen() || mDb.isReadOnly()) {
            return -1;
        }
        final ContentValues values = new ContentValues();


        values.put(DataBaseHandlerConstants.KEY_EVENT_START_HOUR, mEvent.getStartHour());
        values.put(DataBaseHandlerConstants.KEY_EVENT_START_MINUTE, mEvent.getStartMinutes());
        values.put(DataBaseHandlerConstants.KEY_EVENT_END_HOUR, mEvent.getEndHour());
        values.put(DataBaseHandlerConstants.KEY_EVENT_END_MINUTE, mEvent.getEndMinutes());
        values.put(DataBaseHandlerConstants.KEY_EVENT_TYPE, mEvent.getEventType());
        values.put(DataBaseHandlerConstants.KEY_EVENT_START_DATE, DateUtils.formatDate(new Date(mEvent.getStartDateInMillis()), mEvent.getStartHour(), mEvent.getStartMinutes()));
        values.put(DataBaseHandlerConstants.KEY_EVENT_END_DATE, DateUtils.formatDate(new Date(mEvent.getEndDateInMillis()), mEvent.getEndHour(), mEvent.getEndMinutes()));
        values.put(DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS, DateUtils.formatDateToMillis(mEvent.getStartDateInMillis(), mEvent.getStartHour(), mEvent.getStartMinutes()));
        values.put(DataBaseHandlerConstants.KEY_EVENT_END_DATE_IN_MILLIS, DateUtils.formatDateToMillis(mEvent.getEndDateInMillis(), mEvent.getEndHour(), mEvent.getEndMinutes()));
        values.put(DataBaseHandlerConstants.KEY_EVENT_MILEAGE_START, mEvent.getMileageStart());
        values.put(DataBaseHandlerConstants.KEY_EVENT_MILEAGE_END, mEvent.getMileageEnd());
        values.put(DataBaseHandlerConstants.KEY_EVENT_START_LOCATION, mEvent.getStartLocation());
        values.put(DataBaseHandlerConstants.KEY_EVENT_END_LOCATION, mEvent.getEndLocation());
        values.put(DataBaseHandlerConstants.KEY_EVENT_RECORDING, mEvent.isRecordingEvent());
        values.put(DataBaseHandlerConstants.KEY_EVENT_NOTE, mEvent.getNote());

        values.put(DataBaseHandlerConstants.KEY_EVENT_IS_SPLIT_BREAK, mEvent.isSplitBreak() ? 1 : 0);

        mDb.beginTransaction();
        int result = -1;
        try {
            if (mUpdate) {
                result = mEvent.getRowId();
                mDb.update(DataBaseHandlerConstants.TABLE_EVENTS, values, DataBaseHandlerConstants.KEY_ID + "=?", new String[]{String.valueOf(mEvent.getRowId())});
            } else {
                result = (int) mDb.insert(DataBaseHandlerConstants.TABLE_EVENTS, null, values);
            }
        } catch (Exception ignored) {}

        mDb.setTransactionSuccessful();
        mDb.endTransaction();


        return result;
    }

    public static String escapeSQLiteString(String toEscape) {
        if(toEscape == null) {
            return null;
        } else if (toEscape.contains("'")) {
            return toEscape.replaceAll("'", "''");
        }
        return toEscape;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if(mListener != null) {
            if(result == -1) {
                mListener.onDatabaseActionCompleted(DataBaseHandlerConstants.DATABASE_ACTION_ERROR, result);
            } else if(mUpdate) {
                mListener.onDatabaseActionCompleted(DataBaseHandlerConstants.DATABASE_ACTION_EDIT, result);
            } else {
                mListener.onDatabaseActionCompleted(DataBaseHandlerConstants.DATABASE_ACTION_INSERT, result);
            }
        }
        mDb = null;
        mListener = null;
    }

}
