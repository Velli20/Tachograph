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


public class DatabaseLocationQueryBuilder {
    private StringBuilder mWhereClause;

    private String mTable;
    private String mOrderBy;
    private String mColumns;

    private boolean mOrderAscending;
    private boolean mWhereClauseIsOpen = false;
    private boolean mSelectDistinct = false;

    private int mMaxResults = 0;

    public DatabaseLocationQueryBuilder() {

    }

    public DatabaseLocationQueryBuilder fromTable(String table) {
        mTable = table;
        return this;
    }

    /* Select all columns from table */
    public DatabaseLocationQueryBuilder selectAllColumns() {
        mColumns = " * ";
        return this;
    }

    /* Skip duplicate columns */
    public DatabaseLocationQueryBuilder selectDistinct() {
        mSelectDistinct = true;
        return this;
    }

    /* Select count of all columns */
    public DatabaseLocationQueryBuilder selectCountOfAllColumns() {
        mColumns = " count(*) ";
        return this;
    }

    public DatabaseLocationQueryBuilder whereLocationEventIdIs(int eventId) {
        openWhereClause();
        mWhereClause.append(DataBaseHandlerConstants.KEY_LOCATION_EVENT_ID + " = " + String.valueOf(eventId));

        closeWhereClause();
        return this;
    }

    public DatabaseLocationQueryBuilder whereLocationsPointsInTimeFrame(long startDate, long endDate) {
        openWhereClause();
        mWhereClause.append(DataBaseHandlerConstants.KEY_LOCATION_TIME + " >= " + String.valueOf(startDate));
        mWhereClause.append(" AND " + DataBaseHandlerConstants.KEY_LOCATION_TIME + " <= " + String.valueOf(endDate));

        closeWhereClause();
        return this;
    }

    /* Set key to order query by */
    public DatabaseLocationQueryBuilder orderByKey(String key, boolean ascending) {
        mOrderBy = key;
        mOrderAscending = ascending;

        return this;
    }

    public DatabaseLocationQueryBuilder setMaxResults(int maxResults) {
        mMaxResults = maxResults;
        return this;
    }

    private void openWhereClause() {
        if (mWhereClause == null) {
            mWhereClause = new StringBuilder();
            mWhereClause.append("( ");
        } else {
            mWhereClause.append(" AND (");
        }

        mWhereClauseIsOpen = true;
    }

    private void closeWhereClause() {
        if (!mWhereClauseIsOpen) {
            return;
        } else {
            mWhereClause.append(")");
            mWhereClauseIsOpen = false;
        }
    }

    public String buildQuery() {
        String query = (mSelectDistinct ? "SELECT DISTINCT " : "SELECT ") + mColumns + " FROM " + mTable;

        if (mWhereClause != null && mWhereClause.length() > 0) {
            query += " WHERE(";
            query += mWhereClause.toString();
            query += ") ";
        }

        if (mOrderBy != null) {
            query += " ORDER BY ";
            query += mOrderBy;
            query += (mOrderAscending ? " ASC " : " DESC ");
        }
        if (mMaxResults > 0) {
            query += " LIMIT ";
            query += String.valueOf(mMaxResults);
        }
        return query;
    }
}
