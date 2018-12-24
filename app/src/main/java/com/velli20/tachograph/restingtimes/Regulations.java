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

package com.velli20.tachograph.restingtimes;


import android.os.Handler;

import com.velli20.tachograph.DateUtils;
import com.velli20.tachograph.Event;
import com.velli20.tachograph.database.DataBaseHandler;
import com.velli20.tachograph.database.GetLogSummaryTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class Regulations implements DataBaseHandler.OnDatabaseEditedListener, DataBaseHandler.OnGetEventTaskCompleted {
    private static final boolean DEBUG = false;
    private static final String TAG = "Regulations";
    private static Regulations sRegulationsInstance;
    private WeekHolder mCurrentWeek;
    private WeekHolder mPreviousWeek;
    private WorkDayHolder mCurrentDay;
    private Event mCurrentEvent;

    private Handler mAutoUpdateHandler = new Handler();
    private ArrayList<WeakReference<OnRegulationsChangedListener>> mCallbacks = new ArrayList<>();
    private Runnable mAutoUpdater = new Runnable() {

        @Override
        public void run() {
            if (mAutoUpdateHandler != null) {
                mAutoUpdateHandler.postDelayed(mAutoUpdater, 60000);
                notifyCallBacks();
            }
        }

    };

    private Regulations() {
        update();
        DataBaseHandler.getInstance().registerOnDatabaseEditedListener(this);
        DataBaseHandler.getInstance().getRecordingEvent(this);
    }

    public static Regulations getInstance() {
        if (sRegulationsInstance == null) {
            sRegulationsInstance = new Regulations();
        }
        return sRegulationsInstance;
    }

    private void update() {
        DataBaseHandler.getInstance().getWorkingTimes(new GetLogSummaryTask.OnWorkingTimeCalculationsReadyListener() {
            @Override
            public void onWorkingTimeCalculationsReady(ArrayList<WeekHolder> workingWeeks) {
                if (workingWeeks != null) {

                    int weeks = workingWeeks.size();

                    if (weeks > 0) {
                        mCurrentWeek = workingWeeks.get(weeks - 1);
                        mPreviousWeek = (weeks >= 2 ? workingWeeks.get(weeks - 2) : null);
                        int dayCount = mCurrentWeek.getWorkDays() != null ? mCurrentWeek.getWorkDays().size() : 0;
                        mCurrentDay = dayCount > 0 ? mCurrentWeek.getWorkDays().get(dayCount - 1) : null;
                        notifyCallBacks();
                    }
                } else {
                    mCurrentWeek = null;
                    mPreviousWeek = null;
                    mCurrentDay = null;
                    notifyCallBacks();
                }

            }

        }, false, false, false, false);
    }

    /* Setup automatic updater that will notify callbacks every one minute.
     * Synchronization to match i.g with ongoing event
     */
    private void syncAutoUpdater(long sync) {
        mAutoUpdateHandler.postDelayed(mAutoUpdater, +(60000 - (sync)));
    }

    private void stopAutoUpdater() {
        if (mAutoUpdateHandler != null) {
            mAutoUpdateHandler.removeCallbacks(mAutoUpdater);
        }
    }

    /* Regulation (EC)561/2006 Maximum 56 hours weekly driving limit*/
    public int getWeeklyDrivingTimeLimit() {
        return Constants.LIMIT_WEEKLY_DRIVE;
    }

    public int getWeeklyDrivingTime() {
        if (mCurrentWeek != null) {
            return mCurrentWeek.getWeeklyDrivingTime() + getOngoingTime(Event.EVENT_TYPE_DRIVING);
        }
        return getOngoingTime(Event.EVENT_TYPE_DRIVING);
    }

    public int getRemainingWeeklyDrivingTime() {
        return Math.max(getWeeklyDrivingTimeLimit() - getWeeklyDrivingTime(), 0);
    }

    /* Regulation (EC)561/2006 9 hours daily driving limit (can be
     * increased to 10 hours twice a week) */
    public int getDailyDrivingTimeLimit() {
        if (getRemainingExtendedDrivingDaysLeft() > 0) {
            /* User has extended driving days left */
            return Constants.LIMIT_DAILY_DRIVE_EXTENDED_MIN;
        }
        return Constants.LIMIT_DAILY_DRIVE_MIN;
    }

    public int getDailyDrivingTime() {
        if (mCurrentDay == null) {
            return getOngoingTime(Event.EVENT_TYPE_DRIVING);
        }
        return mCurrentDay.getDailyDrivingTime() + getOngoingTime(Event.EVENT_TYPE_DRIVING);
    }

    public int getRemainingExtendedDrivingDaysLeft() {
        if (mCurrentWeek != null) {
            return Math.max(2 - mCurrentWeek.getExtendedDrivingDaysUsed(), 0);
        }
        return 2;
    }

    public int getRemainingDailyDrivingTime() {
        return Math.max(getDailyDrivingTimeLimit() - getDailyDrivingTime(), 0);
    }

    /* Regulation (EC)561/2006 Maximum 90 hours fortnightly driving limit*/
    public int getFortnightlyDrivingTimeLimit() {
        return Constants.LIMIT_FORTNIGHTLY_DRIVE;
    }

    public int getFortnightlyDrivingTime() {
        if (mPreviousWeek != null && mCurrentWeek != null) {
            return mPreviousWeek.getWeeklyDrivingTime() + mCurrentWeek.getWeeklyDrivingTime() + getOngoingTime(Event.EVENT_TYPE_DRIVING);
        } else if (mCurrentWeek != null) {
            return mCurrentWeek.getWeeklyDrivingTime() + getOngoingTime(Event.EVENT_TYPE_DRIVING);
        }
        return getOngoingTime(Event.EVENT_TYPE_DRIVING);
    }

    public int getRemainingFortnightlyDrivingTime() {
        return Math.max(getFortnightlyDrivingTimeLimit() - getFortnightlyDrivingTime(), 0);
    }

    /* Regulation (EC)561/2006 Maximum 4.5 hours continuous driving limit*/
    public int getContinuousDrivingTimeLimit() {
        int limit = Constants.LIMIT_CONTINUOUS_DRIVING;

        if (mCurrentDay != null) {
            int dailyDrivingTimeAvailable = Math.max(getDailyDrivingTimeLimit() - mCurrentDay.getDailyDrivingTime(), 0);

            return (dailyDrivingTimeAvailable < limit ? dailyDrivingTimeAvailable : limit);
        }
        return limit;
    }

    public int getContinuousDrivingTime() {
        if (mCurrentDay != null) {
            return mCurrentDay.getContinuousDrivingTimeAfterBreak() + getOngoingTime(Event.EVENT_TYPE_DRIVING);
        }
        return getOngoingTime(Event.EVENT_TYPE_DRIVING);
    }

    public int getRemainingContinuousDrivingTime() {
        return Math.max(getContinuousDrivingTimeLimit() - getContinuousDrivingTime(), 0);
    }

    /* Regulation (EC)561/2006 11 hours regular daily rest which can be reduced to 9 hours
     * no more than three times a week */
    public int getDailyRestLimit() {
        if (getRemainingReducedDailyRestsLeft() > 0) {
            /* User has reduced rests left */
            return Constants.LIMIT_DAILY_REST_REDUCED_MIN;
        }
        return Constants.LIMIT_DAILY_REST_MIN;
    }

    public int getDailyRest() {
        if (mCurrentDay != null) {
            return mCurrentDay.getDailyRest() + getOngoingTime(Event.EVENT_TYPE_DAILY_REST);
        }
        return getOngoingTime(Event.EVENT_TYPE_DAILY_REST);
    }

    public int getRemainingReducedDailyRestsLeft() {
        if (mCurrentWeek != null) {
            return Math.max(3 - mCurrentWeek.getReducedDailyRests(), 0);
        }
        return 3;
    }

    public int getRemainingDailyRest() {
        return Math.max(getDailyRestLimit() - getDailyRest(), 0);
    }

    /* Regulation (EC)561/2006 45 hours weekly rest, which can be reduced to 24 hours, provided at least one full rest
     * is taken in any fortnight. There should be no more than six consecutive 24 hour periods between weekly rests.
     */
    public int getWeeklyRestLimit() {
        if (getRemainingReducedWeeklyRestsLeft() > 0) {
            return Constants.LIMIT_WEEKLY_REST_REDUCED_MIN;
        }
        /* User already had reduced weekly rest in previous week. This weekly rest must be at least 45 hours */
        return Constants.LIMIT_WEEKLY_REST_MIN;
    }

    public int getWeeklyRest() {
        if (mCurrentWeek != null) {
            return mCurrentWeek.getWeeklyRest() + getOngoingTime(Event.EVENT_TYPE_WEEKLY_REST);
        }
        return getOngoingTime(Event.EVENT_TYPE_WEEKLY_REST);
    }

    public int getRemainingReducedWeeklyRestsLeft() {
        if (mPreviousWeek != null && mPreviousWeek.getWeeklyRest() < Constants.LIMIT_WEEKLY_REST_MIN) {
            return 0;
        }
        return 1;
    }

    public int getRemainingWeeklyRest() {
        return Math.max(getWeeklyRestLimit() - getWeeklyRest(), 0);
    }

    /* Regulation (EC)561/2006 45 minutes break after 4.5 hours driving. A break can be split into two periods,
     * the first being at least 15 minutes and the second at least 30 minutes (which must be completed after 4.5 hours
     * driving) */
    public int getBreakTimeLimit() {
        int fourAndHalfHoursInMillis = (1000 * 60 * 60 * 4) + (1000 * 60 * 30);

        if (mCurrentDay != null) {
            BreakTime firstSplit = mCurrentDay.getPreviousBreak();
            if (firstSplit != null
                    && (firstSplit.getDurationInMinutes() < Constants.LIMIT_BREAK)
                    && ((System.currentTimeMillis() - firstSplit.getStartTime()) < fourAndHalfHoursInMillis)) {
                return Constants.LIMIT_BREAK_SPLIT_2;
            }
        }
        return Constants.LIMIT_BREAK;
    }

    public int getBreakTime() {
        if (mCurrentDay != null && mCurrentDay.getPreviousBreak() != null) {
            return mCurrentDay.getPreviousBreak().getDurationInMinutes() + getOngoingTime(Event.EVENT_TYPE_NORMAL_BREAK);
        }
        return getOngoingTime(Event.EVENT_TYPE_NORMAL_BREAK);
    }

    public int getRemainingBreakTime() {
        return Math.max(getBreakTimeLimit() - getBreakTime(), 0);
    }

    /* Directive 2002/15/EC Maximum working time of 60 hours in one week (provided average not exceeded)*/
    public int getWtdWeeklyWorkingTimeLimit() {
        return Constants.LIMIT_WTD_WEEKLY_WORK;
    }

    public int getWtdWeeklyWorkingTime() {
        if (mCurrentWeek != null) {
            return mCurrentWeek.getWtdWeeklyWorkingTime()
                    + getOngoingTime(Event.EVENT_TYPE_DRIVING)
                    + getOngoingTime(Event.EVENT_TYPE_OTHER_WORK)
                    + getOngoingTime(Event.EVENT_TYPE_POA);
        }
        return getOngoingTime(Event.EVENT_TYPE_DRIVING)
                + getOngoingTime(Event.EVENT_TYPE_OTHER_WORK)
                + getOngoingTime(Event.EVENT_TYPE_POA);
    }

    public int getRemainingWtdWeeklyWorkingTime() {
        return Math.max(getWtdWeeklyWorkingTimeLimit() - getWtdWeeklyWorkingTime(), 0);
    }

    /* This method is implemented so we don't have to recalculate all working times
     * every minute when event is recording
     */
    private int getOngoingTime(int eventType) {
        if (mCurrentEvent == null || (eventType != mCurrentEvent.getEventType())) {
            return 0;
        } else {
            return DateUtils.getTimeDifferenceInMinutes(mCurrentEvent.getStartDateInMillis(), mCurrentEvent.getEndDateInMillis(), -1);
        }
    }

    public void registerOnRegulationsChangedListener(OnRegulationsChangedListener l) {
        mCallbacks.add(new WeakReference<>(l));
    }

    public void unregisterOnRegulationsChangedListener(OnRegulationsChangedListener l) {
        int pos = 0;

        for (WeakReference<OnRegulationsChangedListener> w : mCallbacks) {
            if (w != null && w.get() != null && w.get().equals(l)) {
                mCallbacks.remove(pos);
                break;
            }
            pos++;
        }
    }

    private void notifyCallBacks() {
        for (WeakReference<OnRegulationsChangedListener> l : mCallbacks) {
            if (l.get() != null) {
                l.get().onRegulationsChanged();
            }
        }
    }

    @Override
    public void onDatabaseEdited(int action, int rowId) {
        update();
        DataBaseHandler.getInstance().getRecordingEvent(this);
    }

    @Override
    public void onGetEvent(Event ev) {
        mCurrentEvent = ev;
        notifyCallBacks();
        if (mCurrentEvent != null) {
            syncAutoUpdater((System.currentTimeMillis() - ev.getStartDateInMillis()) % 60000);
        } else {
            stopAutoUpdater();
        }
    }

}
