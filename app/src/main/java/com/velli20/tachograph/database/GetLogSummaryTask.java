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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.velli20.tachograph.DateUtils;
import com.velli20.tachograph.Event;
import com.velli20.tachograph.restingtimes.BreakTime;
import com.velli20.tachograph.restingtimes.Constants;
import com.velli20.tachograph.restingtimes.WeekHolder;
import com.velli20.tachograph.restingtimes.WorkDayHolder;

import java.util.ArrayList;


public class GetLogSummaryTask extends AsyncTask<Void, Void, ArrayList<WeekHolder>> {
    private static final String TAG = "GetLogSummaryTask";
    private static final boolean DEBUG = false;

    private SQLiteDatabase mDb;
    private boolean mCalculateDrivenDistance= false;
    private boolean mCalculateAllEvents = false;
    private boolean mIncludeRecordingEvents = true;
    private boolean mIncludeEventIds = false;

    private ArrayList<Integer> mRowIdsWeek;

    private OnWorkingTimeCalculationsReadyListener mListener;

    public interface OnWorkingTimeCalculationsReadyListener {
        void onWorkingTimeCalculationsReady(ArrayList<WeekHolder> workingWeeks);
    }

    public GetLogSummaryTask(SQLiteDatabase db) {
        mDb = db;
    }

    public GetLogSummaryTask includeAllEvents(boolean include) {
        mCalculateAllEvents = include;
        return this;
    }

    public GetLogSummaryTask includeDrivenDistance(boolean includeDrivenDistance) {
        mCalculateDrivenDistance = includeDrivenDistance;
        return this;
    }

    public GetLogSummaryTask includeRecordingEvents(boolean include) {
        mIncludeRecordingEvents = include;
        return this;
    }

    public GetLogSummaryTask setOnWorkingTimeCalculationsReadyListener(OnWorkingTimeCalculationsReadyListener l) {
        mListener = l;
        return this;
    }

    public GetLogSummaryTask withEventIds(ArrayList<Integer> rowIds) {
        mRowIdsWeek = rowIds;
        return this;
    }

    public GetLogSummaryTask includeEventIds(boolean includeEventIds) {
        mIncludeEventIds = includeEventIds;
        return this;
    }

    private String getDatabaseQuery() {
        DatabaseEventQueryBuilder builder = new DatabaseEventQueryBuilder()
                .fromTable(DataBaseHandlerConstants.TABLE_EVENTS)
                .selectAllColumns()
                .orderByKey(DataBaseHandlerConstants.KEY_EVENT_START_DATE, true);
        if(mCalculateAllEvents) {
            return builder.buildQuery();
        } else if(mRowIdsWeek != null) {
            return builder.whereEventsWithRowIds(mRowIdsWeek).buildQuery();
        } else {
            long timeNow = System.currentTimeMillis();
            long startTime = System.currentTimeMillis() - (Constants.SEVEN_DAYS_PERIOD_IN_MILLIS * 4);

            return builder.whereEventsInTimeFrame(startTime, timeNow, mIncludeRecordingEvents).buildQuery();
        }
    }

    /* Calculate total driving time and rest periods that are described in Regulation No 561/2006
     * that provides a common set of EU rules for maximum daily and fortnightly driving times,
     * as well as daily and weekly minimum rest periods for all drivers of road haulage and passenger
     * transport vehicles
     */
    @Override
    protected ArrayList<WeekHolder> doInBackground(Void... params) {
        if(mDb == null || !mDb.isOpen()) {
            return null;
        }

        ArrayList<WeekHolder> weeks = new ArrayList<>();
        ArrayList<Integer> eventIdsWeek = null;
        ArrayList<Integer> eventIdsDay = null;

        WeekHolder currentWeek = null;
        WorkDayHolder currentDay = null;

        Event previousEvent = null;
        Event event;

        String query = getDatabaseQuery();
        Cursor eventCursor = mDb.rawQuery(query, null);

        if(eventCursor == null || !eventCursor.moveToFirst()) {
            return null;
        }

        do {
            event = DatabaseEventUtils.getEvent(eventCursor);
            if(event == null) {
                continue;
            }

            if(mCalculateDrivenDistance && DatabaseLocationUtils.checkForLoggedRoute(mDb, event)) {
                event.setDrivenDistance(DatabaseLocationUtils.getLoggedRouteDistance(mDb, event.getRowId()) / 1000);
            }

            if(isRequiredToStartNewDay(currentDay, event)) {
                /* End current day */
                if(currentDay != null && previousEvent != null && currentWeek != null) {
                    currentDay.setEndDate(previousEvent.getEndDateInMillis());
                    if(mIncludeEventIds && eventIdsDay != null) {
                        currentDay.setEventIds(eventIdsDay);
                    }
                    endWorkingDay(currentWeek, currentDay);
                }
                currentDay = startNewDay(event);
                if(mIncludeEventIds) {
                    eventIdsDay = new ArrayList<>();
                }
            }
            if(isRequiredToStartNewWeek(currentWeek, event)) {
                /* End current week */
                if(currentWeek != null && previousEvent != null ) {
                    currentWeek.setEndDate(previousEvent.getEndDateInMillis());
                    if(mIncludeEventIds && eventIdsWeek != null) {
                        currentWeek.setEventIds(eventIdsWeek);
                    }
                    weeks.add(currentWeek);
                }
                currentWeek = startNewWeek(event);
                if(mIncludeEventIds) {
                    eventIdsWeek = new ArrayList<>();
                }
            }

            int eventDurationInMinutes = getEventDuration(event);
            currentDay.setDailyDrivenDistance(currentDay.getDailyDrivenDistance() + event.getDrivenDistance());

            switch (event.getEventType()) {
                case Event.EVENT_TYPE_DRIVING:
                    currentDay.setDailyWorkingTime(currentDay.getDailyWorkingTime() + eventDurationInMinutes);
                    currentDay.setDailyDrivingTime(currentDay.getDailyDrivingTime() + eventDurationInMinutes);
                    currentDay.setContinuousDrivingTimeAfterBreak(currentDay.getContinuousDrivingTimeAfterBreak() + eventDurationInMinutes);
                    currentDay.setWtdWorkingTime(currentDay.getWtdDailyWorkingTime() + eventDurationInMinutes);
                    break;
                case Event.EVENT_TYPE_OTHER_WORK:
                    currentDay.setDailyWorkingTime(currentDay.getDailyWorkingTime() + eventDurationInMinutes);
                    currentDay.setOtherWorkingTime(currentDay.getDailyWorkingTime() + eventDurationInMinutes);
                    currentDay.setWtdWorkingTime(currentDay.getWtdDailyWorkingTime() + eventDurationInMinutes);
                    break;
                case Event.EVENT_TYPE_POA:
                    /* Periods of availability are not classed as working time. POA will pause the 6 hour WTD break requirement*/
                    currentDay.setDailyPoaTime(currentDay.getDailyPoaTime() + eventDurationInMinutes);
                    currentDay.setWtdWorkingTime(currentDay.getWtdDailyWorkingTime() + eventDurationInMinutes);
                    break;
                case Event.EVENT_TYPE_NORMAL_BREAK:
                    currentDay.setDailyWorkingTime(currentDay.getDailyWorkingTime() + eventDurationInMinutes);
                    currentDay.addNewBreakTime(getBreakTime(event));

                    if(checkIfLatestBreakTimeIsValid(currentDay)) {
                        /* Break time fulfilled. Reset continuous driving time */
                        currentDay.setContinuousDrivingTimeAfterBreak(0);
                    }
                    break;
                case Event.EVENT_TYPE_DAILY_REST:
                    currentDay.setDailyRestingTime(eventDurationInMinutes);
                    break;
                case Event.EVENT_TYPE_WEEKLY_REST:
                    currentWeek.setWeeklyRest(eventDurationInMinutes);
                    break;
            }
            if(mIncludeEventIds && eventIdsWeek != null && eventIdsDay != null) {
                eventIdsWeek.add(event.getRowId());
                eventIdsDay.add(event.getRowId());
            }
            previousEvent = event;

            if(eventCursor.isLast() && currentWeek != null) {

                currentDay.setEndDate(previousEvent.getEndDateInMillis());
                if(mIncludeEventIds && eventIdsDay != null) {
                    currentDay.setEventIds(eventIdsDay);
                }
                endWorkingDay(currentWeek, currentDay);

                currentWeek.setEndDate(previousEvent.getEndDateInMillis());
                if(mIncludeEventIds && eventIdsWeek != null) {
                    currentWeek.setEventIds(eventIdsWeek);
                }
                weeks.add(currentWeek);
            }
        } while (eventCursor.moveToNext());


        return weeks;
    }

    @Override
    protected void onPostExecute(ArrayList<WeekHolder> list){
        if(mListener != null){
            mListener.onWorkingTimeCalculationsReady(list);
        }
    }

    private WorkDayHolder startNewDay(Event event) {
        WorkDayHolder holder = new WorkDayHolder();
        holder.setStartDate(event.getStartDateInMillis());

        return holder;
    }

    private WeekHolder startNewWeek(Event event) {
        WeekHolder holder = new WeekHolder();
        holder.setStartDate(event.getStartDateInMillis());

        return holder;
    }

    private BreakTime getBreakTime(Event event) {
        if(event == null) {
            return null;
        }

        BreakTime breakTime = new BreakTime();
        breakTime.setStart(event.getStartDateInMillis());
        breakTime.setEnd(event.getEndDateInMillis());
        breakTime.setDuration(DateUtils.getTimeDifferenceInMinutes(event.getStartDateInMillis(), event.getEndDateInMillis(), -1));

        return breakTime;
    }

    /* Checks if it is required to start a new week. */
    private static boolean isRequiredToStartNewWeek(WeekHolder currentWeek, Event nextEvent) {
        if(currentWeek == null) {
            return true;
        } else if((nextEvent.getStartDateInMillis() - currentWeek.getStartDate()) >= Constants.SEVEN_DAYS_PERIOD_IN_MILLIS) {
            /* It's over 7 days since we started counting current week */
            return true;
        } else if(checkIfWeeklyRestIsFulfilled(currentWeek)) {
            /* Weekly rest time length is at least 24 hours */
            return true;
        }
        return false;
    }

    private static boolean isRequiredToStartNewDay(WorkDayHolder currentDay, Event nextEvent) {
        if(currentDay == null) {
            return true;
        } else if((nextEvent.getStartDateInMillis() - currentDay.getStartDate()) >= Constants.ONE_DAY_PERIOD_IN_MILLIS) {
            return true;
        } else if(checkIfDailyRestIsFulfilled(currentDay)) {
            return true;
        }
        return false;
    }

    private static boolean checkIfWeeklyRestIsFulfilled(WeekHolder currentWeek) {
        if(currentWeek == null) {
            return false;
        }
        return currentWeek.getWeeklyRest() >= Constants.LIMIT_WEEKLY_REST_REDUCED_MIN;
    }

    private static boolean checkIfDailyRestIsFulfilled(WorkDayHolder currentDay) {
        if(currentDay == null) {
            return false;
        }
        return currentDay.getDailyRest() >= Constants.LIMIT_DAILY_REST_REDUCED_MIN;
    }

    /* Check if previous break time is valid. Breaks of at least 45 minutes (separable into 15 minutes followed by 30 minutes)
     * should be taken after 4 ½ hours at the latest.
     */
    private static boolean checkIfLatestBreakTimeIsValid(WorkDayHolder currentDay) {
        if(currentDay == null) {
            return false;
        }
        BreakTime latestBreak = currentDay.getLastBreak();
        BreakTime previousBreak = currentDay.getPreviousBreak();

        if(latestBreak == null) {
            return false;
        } else if(latestBreak.getDurationInMinutes() >= Constants.LIMIT_BREAK) {
            return true;
        } else if(previousBreak != null
                && ((previousBreak.getDurationInMinutes() + latestBreak.getDurationInMinutes()) >= Constants.LIMIT_BREAK)){
            /* Check that both breaks are within 4,5 hours time frame */
            int timeFrame = DateUtils.getTimeDifferenceInMinutes(latestBreak.getStartTime(), previousBreak.getStartTime(), -1);
            return timeFrame < 450;
        }
        return false;
    }

    private static int getEventDuration(Event event) {
        return DateUtils.getTimeDifferenceInMinutes(event.getStartDateInMillis(), event.getEndDateInMillis(), -1);
    }

    private static void endWorkingDay(WeekHolder currentWeek, WorkDayHolder dayToEnd) {
        if(currentWeek == null) {
            return;
        }

        currentWeek.setWeeklyDrivingTime(currentWeek.getWeeklyDrivingTime() + dayToEnd.getDailyDrivingTime());
        currentWeek.setWeeklyWorkingTime(currentWeek.getWeeklyWorkingTime() + dayToEnd.getDailyWorkingTime());
        currentWeek.setWeeklyOtherWorkingTime(currentWeek.getWeeklyOtherWorkingTime() + dayToEnd.getOtherWorkingTime());
        currentWeek.setWeeklyDrivenDistance(currentWeek.getWeeklyDrivenDistance() + dayToEnd.getDailyDrivenDistance());
        currentWeek.setWeeklyPoaTime(currentWeek.getWeeklyPoaTime() + dayToEnd.getDailyPoaTime());
        currentWeek.setWtdWeeklyWorkingTime(currentWeek.getWtdWeeklyWorkingTime() + dayToEnd.getWtdDailyWorkingTime());

        if(dayToEnd.getDailyRest() < Constants.LIMIT_DAILY_REST_MIN) {
            currentWeek.setReducedDailyRest(currentWeek.getReducedDailyRests()+1);
        }
        if(dayToEnd.getDailyDrivingTime() > Constants.LIMIT_DAILY_DRIVE_MIN) {
            currentWeek.setExtendedDrivingDaysUsed(currentWeek.getExtendedDrivingDaysUsed()+1);
        }
        currentWeek.addWorkDay(dayToEnd);
    }



}
