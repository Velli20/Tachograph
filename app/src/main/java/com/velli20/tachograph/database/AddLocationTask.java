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

import com.velli20.tachograph.location.CustomLocation;

import static com.velli20.tachograph.database.DataBaseHandlerConstants.TABLE_LOCATIONS;

public class AddLocationTask extends AsyncTask<Void, Void, Integer> {
    private CustomLocation mLocation;
    private SQLiteDatabase mDb;
    private OnDatabaseActionCompletedListener mListener;

    public AddLocationTask(SQLiteDatabase db, CustomLocation location) {
        mDb = db;
        mLocation = location;
    }

    public AddLocationTask setOnDatabaseActionCompletedListener(OnDatabaseActionCompletedListener l) {
        mListener = l;
        return this;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        if (mDb == null || !mDb.isOpen() || mDb.isReadOnly() || mLocation == null) {
            return -1;
        }
        final ContentValues values = new ContentValues();
        values.put(DataBaseHandlerConstants.KEY_LOCATION_LATITUDE, mLocation.getLatitude());
        values.put(DataBaseHandlerConstants.KEY_LOCATION_LONGITUDE, mLocation.getLongitude());
        values.put(DataBaseHandlerConstants.KEY_LOCATION_TIME, mLocation.getTime());
        values.put(DataBaseHandlerConstants.KEY_LOCATION_SPEED, mLocation.getSpeed());
        values.put(DataBaseHandlerConstants.KEY_LOCATION_EVENT_ID, mLocation.getEventId());

        mDb.beginTransaction();

        int result = (int) mDb.insert(TABLE_LOCATIONS, null, values);


        mDb.setTransactionSuccessful();
        mDb.endTransaction();

        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if(mListener != null) {
            if(result == -1) {
                mListener.onDatabaseActionCompleted(DataBaseHandlerConstants.DATABASE_ACTION_ERROR, result);
            } else {
                mListener.onDatabaseActionCompleted(DataBaseHandlerConstants.DATABASE_ACTION_INSERT, result);
            }
        }
        mDb = null;
        mListener = null;
    }
}