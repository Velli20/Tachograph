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

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;



public class DeleteEventTask extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = "DeleteEventTask ";
    private static final boolean DEBUG = false;

    private ArrayList<Integer> mRowIds;
    private int mRowId;

    private SQLiteDatabase mDb;
    private OnDatabaseActionCompletedListener mListener;

    /* Delete multiple events on database */
    public DeleteEventTask(SQLiteDatabase db, ArrayList<Integer> ids){
        mDb = db;
        mRowIds = ids;
    }

    /* Delete one event on database with given row mId */
    public DeleteEventTask(SQLiteDatabase db, int rowId){
        mDb = db;
        mRowId = rowId;
    }

    public DeleteEventTask setOnDatabaseActionCompletedListener(OnDatabaseActionCompletedListener l) {
        mListener = l;
        return this;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        if (mDb == null || !mDb.isOpen() || mDb.isReadOnly()) {
            return -1;
        }
        mDb.beginTransaction();

        int rowsDeleted = 0;
        try {
            if (mRowIds != null) {
                for (Integer rowId : mRowIds) {
                    if (rowId != null) {
                        deleteEvent(mDb, rowId);
                        rowsDeleted++;
                    }
                }
            } else {
                rowsDeleted = 1;
                deleteEvent(mDb, mRowId);
            }
        } catch (Exception e) {
            if(DEBUG) {
                Log.e(TAG, TAG + e.getMessage());
            }
        } finally {
            mDb.setTransactionSuccessful();
        }

        mDb.endTransaction();
        return rowsDeleted;
    }

    private static void deleteEvent(SQLiteDatabase db, int rowId) throws Exception {
        String queryEvent = "DELETE FROM "
                + DataBaseHandlerConstants.TABLE_EVENTS + " WHERE("
                + DataBaseHandlerConstants.KEY_ID + " = "
                + String.valueOf(rowId) + ")";
        String queryLocations = "DELETE FROM "
                + DataBaseHandlerConstants.TABLE_LOCATIONS + " WHERE("
                + DataBaseHandlerConstants.KEY_LOCATION_EVENT_ID + " = "
                + String.valueOf(rowId) + ")";

        db.execSQL(queryEvent);
        db.execSQL(queryLocations);
    }

    @Override
    protected void onPostExecute(Integer rowsDeleted){
        if(mListener != null) {
            mListener.onDatabaseActionCompleted(DataBaseHandlerConstants.DATABASE_ACTION_DELETE_MULTIPLE, rowsDeleted);
        }
    }

}
