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

import java.util.ArrayList;

public class WorkDayHolder {
    private long start = -1;
    private long end = -1;


    private int mTotalDrivingTime = 0;
    private int mDailyRestingTime = 0;
    private int mContinuousDrivingTimeAfterBreak = 0;
    private int mPeriodOfAvailability = 0;
    private int mTotalWorkingTime = 0;
    private int mOtherWorkingTime = 0;

    private int wtdDailyWorkingTime = 0;
    private double mDrivenDistance = 0;


    private ArrayList<BreakTime> mBreaks = new ArrayList<>();
    private ArrayList<Integer> mEventIds;

    public WorkDayHolder() {

    }

    public void setDailyRestingTime(int minutes) {
        this.mDailyRestingTime = minutes;
    }

    public void setWtdWorkingTime(int time) {
        wtdDailyWorkingTime = time;
    }

    public long getStartDate() {
        return start;
    }

    public void setStartDate(long startDate) {
        start = startDate;
    }

    public long getEndDate() {
        return end;
    }

    public void setEndDate(long endDate) {
        end = endDate;
    }

    public int getDailyDrivingTime() {
        return mTotalDrivingTime;
    }

    public void setDailyDrivingTime(int minutes) {
        this.mTotalDrivingTime = minutes;
    }

    public int getContinuousDrivingTimeAfterBreak() {
        return mContinuousDrivingTimeAfterBreak;
    }

    public void setContinuousDrivingTimeAfterBreak(int minutes) {
        mContinuousDrivingTimeAfterBreak = minutes;
    }

    public int getDailyRest() {
        return mDailyRestingTime;
    }

    public int getDailyWorkingTime() {
        return mTotalWorkingTime;
    }

    public void setDailyWorkingTime(int minutes) {
        this.mTotalWorkingTime = minutes;
    }

    public int getOtherWorkingTime() {
        return mOtherWorkingTime;
    }

    public void setOtherWorkingTime(int minutes) {
        mOtherWorkingTime = minutes;
    }

    public int getWtdDailyWorkingTime() {
        return wtdDailyWorkingTime;
    }

    public int getDailyPoaTime() {
        return mPeriodOfAvailability;
    }

    public void setDailyPoaTime(int minutes) {
        mPeriodOfAvailability = minutes;
    }

    public double getDailyDrivenDistance() {
        return mDrivenDistance;
    }

    public void setDailyDrivenDistance(double distance) {
        mDrivenDistance = distance;
    }

    public ArrayList<Integer> getEventIds() {
        return mEventIds;
    }

    public void setEventIds(ArrayList<Integer> eventIds) {
        mEventIds = eventIds;
    }

    public void addNewBreakTime(BreakTime breaktime) {
        if (breaktime != null) {
            mBreaks.add(breaktime);
        }
    }


    public BreakTime getLastBreak() {
        if (!mBreaks.isEmpty()) {
            return mBreaks.get(mBreaks.size() - 1);
        }
        return null;
    }

    public BreakTime getPreviousBreak() {
        if (mBreaks.size() >= 2) {
            mBreaks.get(mBreaks.size() - 2);
        }
        return null;
    }

    public ArrayList<BreakTime> getBreakTimes() {
        return mBreaks;
    }


}
