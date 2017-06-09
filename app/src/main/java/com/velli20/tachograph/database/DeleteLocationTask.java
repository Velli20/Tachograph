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

public class DeleteLocationTask extends AsyncTask<Void, Void, Integer> {
    private int mEventId;

    private SQLiteDatabase mDb;
    private OnDatabaseActionCompletedListener mListener;



    public DeleteLocationTask(SQLiteDatabase db, int eventId){
        mDb = db;
        mEventId = eventId;
    }

    public DeleteLocationTask setOnDatabaseActionCompletedListener(OnDatabaseActionCompletedListener l) {
        mListener = l;
        return this;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        if (mDb == null || !mDb.isOpen() || mDb.isReadOnly()) {
            return -1;
        }

        String query = "DELETE FROM "
                + DataBaseHandlerConstants.TABLE_LOCATIONS + " WHERE("
                + DataBaseHandlerConstants.KEY_LOCATION_EVENT_ID + " = "
                + String.valueOf(mEventId) + ")";


        mDb.execSQL(query);



        return mEventId;
    }

    @Override
    protected void onPostExecute(Integer result){
        if (mListener != null) {
            mListener.onDatabaseActionCompleted(DataBaseHandlerConstants.DATABASE_ACTION_DELETE, result);
        }
    }
}
