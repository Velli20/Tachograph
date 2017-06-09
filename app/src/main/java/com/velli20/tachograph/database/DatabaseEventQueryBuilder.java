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


import java.util.ArrayList;

public class DatabaseEventQueryBuilder {
    private StringBuilder mWhereClause;

    private String mTable;
    private String mOrderBy;
    private String mColumns;

    private boolean mOrderAscending;
    private boolean mWhereClauseIsOpen = false;

    private ArrayList<Integer> mEventsToInclude;

    public DatabaseEventQueryBuilder() {

    }

    public DatabaseEventQueryBuilder fromTable(String table) {
        mTable = table;
        return this;
    }

    /* Select all columns from table */
    public DatabaseEventQueryBuilder selectAllColumns() {
        mColumns = DataBaseHandlerConstants.columnsSelection;
        return this;
    }

    /* Get events with given time frame. Return also events that ends within time frame and starts
     * outside time frame. Also returns events that start within time frame and end outside time frame.
     */
    public DatabaseEventQueryBuilder whereEventsInTimeFrame(long startDate, long endDate, boolean includeRecordingEvents) {
        openWhereClause();

        mWhereClause.append("(" + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS  + " >= " + String.valueOf(startDate)
                + " AND " + DataBaseHandlerConstants.KEY_EVENT_END_DATE_IN_MILLIS  + " <= "+ String.valueOf(endDate) + ") ");

        mWhereClause.append("OR ( " + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " <= " + String.valueOf(startDate)
                + " AND " + DataBaseHandlerConstants.KEY_EVENT_END_DATE_IN_MILLIS + " >= " + String.valueOf(startDate) + ") ");

        mWhereClause.append("OR ( " + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " >= " + String.valueOf(startDate)
                + " AND " + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " <= "+ String.valueOf(endDate)
                + " AND " + DataBaseHandlerConstants.KEY_EVENT_END_DATE_IN_MILLIS + " >= "+ String.valueOf(endDate) + ")");

        if(includeRecordingEvents) {
            if(endDate > System.currentTimeMillis()) {
                mWhereClause.append("OR ( " + DataBaseHandlerConstants.KEY_EVENT_RECORDING + " = " + String.valueOf(1)
                        + " AND " + DataBaseHandlerConstants.KEY_EVENT_START_DATE_IN_MILLIS + " >= " + String.valueOf(startDate) + ")");
            }
            closeWhereClause();
            return this;
        } else {
            closeWhereClause();
            openWhereClause();
            mWhereClause.append(DataBaseHandlerConstants.KEY_EVENT_RECORDING + " = " + String.valueOf(0));
            closeWhereClause();
        }



        return this;
    }

    /* Get Events by event type */
    public DatabaseEventQueryBuilder whereEventTypeIs(int eventType) {
        if(mEventsToInclude == null) {
            mEventsToInclude = new ArrayList<>();
        }
        mEventsToInclude.add(eventType);
        return this;
    }

    /* Select recording event */
    public DatabaseEventQueryBuilder whereEventIsRecording() {
        openWhereClause();

        mWhereClause.append(DataBaseHandlerConstants.KEY_EVENT_RECORDING + " = " + String.valueOf(1));

        closeWhereClause();

        return this;
    }


    public DatabaseEventQueryBuilder whereEventWithRowId(int rowId) {
        openWhereClause();

        mWhereClause.append(DataBaseHandlerConstants.KEY_ID + " = " + String.valueOf(rowId));

        closeWhereClause();
        return this;
    }


    /* Select events with given row ids */
    public DatabaseEventQueryBuilder whereEventsWithRowIds(int rowIds[]) {
        openWhereClause();

        int length = rowIds.length;

        for(int i = 0; i < length; i++){
            if(i != 0){
                mWhereClause.append(" OR ");
            }
            mWhereClause.append(DataBaseHandlerConstants.KEY_ID + " = " + String.valueOf(rowIds[i]));
        }

        closeWhereClause();
        return this;
    }

    /* Select events with given row ids */
    public DatabaseEventQueryBuilder whereEventsWithRowIds(ArrayList<Integer> rowIds) {
        if(rowIds == null) {
            return this;
        }
        openWhereClause();

        boolean first = true;
        for(Integer rowId : rowIds) {
            if(rowId == null) {
                continue;
            }
            if(!first) {
                mWhereClause.append(" OR ");
            }
            mWhereClause.append(DataBaseHandlerConstants.KEY_ID + " = " + String.valueOf(rowId));
            first = false;
        }

        closeWhereClause();
        return this;
    }

    /* Set key to order query by */
    public DatabaseEventQueryBuilder orderByKey(String key, boolean ascending) {
        mOrderBy = key;
        mOrderAscending = ascending;

        return this;
    }

    private void openWhereClause() {
        if(mWhereClause == null) {
            mWhereClause = new StringBuilder();
            mWhereClause.append("( ");
        } else {
            mWhereClause.append(" AND (");
        }

        mWhereClauseIsOpen = true;
    }

    private void closeWhereClause() {
        if(!mWhereClauseIsOpen) {
            return;
        } else {
            mWhereClause.append(")");
            mWhereClauseIsOpen = false;
        }
    }

    public String buildQuery() {
        String query = "SELECT " + mColumns + " FROM " + mTable;

        if((mWhereClause != null && mWhereClause.length() > 0) || mEventsToInclude != null) {
            query += " WHERE(";
            if(mWhereClause != null) {
                query += mWhereClause.toString();
            }
            if(mEventsToInclude != null) {
                if(mWhereClause != null) {
                    query += " AND (";
                } else {
                    query += "";
                }

                boolean firstInList = true;
                for(Integer eventType : mEventsToInclude) {
                    if(!firstInList) {
                        query += " OR ";
                    } else {
                        firstInList = false;
                    }
                    query += DataBaseHandlerConstants.KEY_EVENT_TYPE + String.format(" = %d" ,eventType.intValue());

                }
                if(mWhereClause != null) {
                    query += ")";
                }
            }
            query += ") ";
        }

        if(mOrderBy != null) {
            query += " ORDER BY ";
            query += mOrderBy;
            query += (mOrderAscending ? " ASC " : " DESC ");
        }
        return query;
    }


}
