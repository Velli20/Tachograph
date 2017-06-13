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

package com.velli20.tachograph;


public class Event {

    public static final int EVENT_TYPE_DRIVING = 0;
    public static final int EVENT_TYPE_DAILY_REST = 1;
    public static final int EVENT_TYPE_WEEKLY_REST = 2;
    public static final int EVENT_TYPE_NORMAL_BREAK = 3;
    public static final int EVENT_TYPE_OTHER_WORK = 4;
    public static final int EVENT_TYPE_POA = 5;

    public static final int EVENT_LOGGING_IN_PROGRESS = 1;
    public static final int EVENT_LOGGING_STOPPED = 0;


    private String mNote;
    private String mStartLocation;
    private String mEndLocation;

    private int mEventType = 0;
    private int mStartHours = 0;
    private int mStartMinutes = 0;
    private int mEndHours = 0;
    private int mEndMinutes = 0;
    private int mMileageStart = 0;
    private int mMileageEnd = 0;
    private int mRecordingEvent = 0;
    private long mStartDateInMillis = 0;
    private long mEndDateInMillis = 0;

    private int rowId = -1;

    private boolean mIsSplitBreak = false;
    private boolean mHasLoggedRoute = false;

    private double mDistance = 0;

    public Event() {
    }

    public int getStartHour() {
        return mStartHours;
    }

    public int getStartMinutes() {
        return mStartMinutes;
    }

    public int getEndHour() {
        return mEndHours;
    }

    public int getEndMinutes() {
        return mEndMinutes;
    }

    public boolean isRecordingEvent() {
        return mRecordingEvent == EVENT_LOGGING_IN_PROGRESS;
    }

    public int getRowId() {
        return rowId;
    }

    public int getEventType() {
        return mEventType;
    }

    public long getStartDateInMillis() {
        return mStartDateInMillis;
    }

    public long getEndDateInMillis() {
        if (mRecordingEvent == EVENT_LOGGING_IN_PROGRESS) {
            return System.currentTimeMillis();
        } else {
            return mEndDateInMillis;
        }
    }

    public int getMileageStart() {
        return mMileageStart;
    }

    public int getMileageEnd() {
        return mMileageEnd;
    }


    public String getNote() {
        return mNote;
    }

    public String getStartLocation() {
        return mStartLocation;
    }

    public double getDrivenDistance() {
        return mDistance;
    }

    public boolean isSplitBreak() {
        return mIsSplitBreak;
    }

    public String getEndLocation() {
        return mEndLocation;
    }

    public boolean hasLoggedRoute() {
        return mHasLoggedRoute;
    }

    public void setEventType(int type) {
        mEventType = type;
    }


    public void setStartDate(long millis) {
        mStartDateInMillis = millis;
    }

    public void setEndDate(long millis) {
        mEndDateInMillis = millis;
    }

    public void setStartTime(int hour, int minutes) {
        mStartHours = hour;
        mStartMinutes = minutes;
    }

    public void setEndTime(int hour, int minutes) {
        mEndHours = hour;
        mEndMinutes = minutes;
    }

    public void setMileageStart(int mileage) {
        mMileageStart = mileage;
    }

    public void setMileageEnd(int mileage) {
        mMileageEnd = mileage;
    }

    public void setRowId(int id) {
        rowId = id;
    }


    public void setRecording(boolean recording) {
        mRecordingEvent = recording ? EVENT_LOGGING_IN_PROGRESS : EVENT_LOGGING_STOPPED;
    }

    public void setNote(String note) {
        mNote = note;
    }

    public void setStartLocation(String location) {
        mStartLocation = location;
    }

    public void setEndLocation(String location) {
        mEndLocation = location;
    }

    public void setHasLoggedRoute(boolean hasRoute) {
        mHasLoggedRoute = hasRoute;
    }

    public void setDrivenDistance(double dist) {
        mDistance = dist;
    }
}
