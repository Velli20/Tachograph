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


import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.velli20.tachograph.database.DataBaseHandler;

import java.lang.ref.WeakReference;

public enum EventRecorder {
    INSTANCE;

    private final Handler mScheduledEventHandler = new Handler();
    private boolean mEventRecordingScheduled = false;
    private int mScheduledEventType;
    private Event mScheduledEventIdToMatch;
    private long mScheduledDate;
    private long mScheduledEventStartDate;
    private WeakReference<OnEventScheduledListener> mListener;
    private final Runnable mScheduledEventRunnable = new Runnable() {

        @Override
        public void run() {
            if (mEventRecordingScheduled) {
                startRecordingEventExplicitlyWithCurrentEventId(mScheduledEventType, mScheduledEventStartDate, mScheduledEventIdToMatch);
                cancelScheduledEvent();
                notifyCallback();
            }
        }
    };

    /* Start recording new Event. This will end any ongoing recording events */
    public void startRecordingEvent(int eventType, long startTime) {
        final Event eventToRecord = new Event();
        eventToRecord.setRecording(true);
        eventToRecord.setStartDate(startTime);
        eventToRecord.setStartTime(DateUtils.getCurrentHour(startTime), DateUtils.getCurrentMinute(startTime));
        eventToRecord.setEventType(eventType);

        /* Make sure there is no any other event currently recording.
         * if there is, then end it so we don't overlap it with a new one.
         */
        DataBaseHandler.getInstance().getRecordingEvent(new DataBaseHandler.OnGetEventTaskCompleted() {

            @Override
            public void onGetEvent(Event ev) {
                if (ev != null) {
                    /* Stop this event */
                    endRecordingEvent(ev, System.currentTimeMillis() - 1);
                }
                if ((ev != null && ev.getEventType() != eventToRecord.getEventType()) || ev == null) {
                    /* Add recording event to the database */
                    DataBaseHandler.getInstance().addNewEvent(eventToRecord);
                }
            }
        });
    }

    /* Start recording new Event if currentEventId matches currently recording event.
     * This will end any ongoing recording events.
     */
    public void startRecordingEventExplicitlyWithCurrentEventId(int eventType, final long startTime, final Event eventToMatch) {
        final Event eventToRecord = new Event();
        eventToRecord.setRecording(true);
        eventToRecord.setStartDate(startTime);
        eventToRecord.setStartTime(DateUtils.getCurrentHour(startTime), DateUtils.getCurrentMinute(startTime));
        eventToRecord.setEventType(eventType);

        /* Make sure there is no any other event currently recording.
         * if there is, then end it so we don't overlap it with a new one.
         */
        DataBaseHandler.getInstance().getRecordingEvent(new DataBaseHandler.OnGetEventTaskCompleted() {

            @Override
            public void onGetEvent(Event ev) {
                if ((ev == null && eventToMatch != null) || (ev != null && eventToMatch != null && ev.getRowId() != eventToMatch.getRowId())) {
                    return;
                }
                if (ev != null) {
                    /* Stop this event */
                    endRecordingEvent(ev, startTime - 1);
                }
                if ((ev != null && ev.getEventType() != eventToRecord.getEventType()) || ev == null) {
                    /* Add recording event to the database */
                    DataBaseHandler.getInstance().addNewEvent(eventToRecord);
                }
            }
        });
    }

    /* End recording event */
    public void endRecordingEvent(Event ev, long endTime) {
        if (ev == null) {
            return;
        }
        final Resources res = App.get().getResources();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());

        boolean deleteOneMinEvents = prefs.getBoolean(res.getString(R.string.preference_key_remove_under_1_min_logs), true);
        ev.setRecording(false);
        ev.setEndDate(endTime);
        ev.setEndTime(DateUtils.getCurrentHour(endTime), DateUtils.getCurrentMinute(endTime));

        /* Avoid adding events to database with 0 min duration */
        if (ev.getEndDateInMillis() - ev.getStartDateInMillis() < (60 * 1000) && deleteOneMinEvents) {
            DataBaseHandler.getInstance().deleteEvent(ev.getRowId());

        } else {
            DataBaseHandler.getInstance().updateEvent(ev);
        }
    }

    /* Schedule Event recording.
     * @param eventStartTime: Start date of the scheduled event
     * @param dateToStartRecording: Date in Unix time when to trigger event recording
     * @param currentEvent: Event to match when the event recording is triggered. If the
     *                      currently recording Event id does not match given Event id then
     *                      cancel scheduled Event recording
     */
    public void scheduleEvent(int eventType, long eventStartTime, long dateToStartRecording, Event currentEvent) {
        cancelScheduledEvent();
        long delay = dateToStartRecording - System.currentTimeMillis();

        mEventRecordingScheduled = true;
        mScheduledEventType = eventType;
        mScheduledDate = dateToStartRecording;
        mScheduledEventStartDate = eventStartTime;
        mScheduledEventIdToMatch = currentEvent;

        if (delay < 0) {
            startRecordingEventExplicitlyWithCurrentEventId(mScheduledEventType, mScheduledEventStartDate, mScheduledEventIdToMatch);
            return;
        }
        mScheduledEventHandler.postDelayed(mScheduledEventRunnable, delay);
        notifyCallback();
    }

    public void cancelScheduledEvent() {
        mEventRecordingScheduled = false;
        mScheduledEventIdToMatch = null;
        if (mScheduledEventHandler != null) {
            mScheduledEventHandler.removeCallbacks(mScheduledEventRunnable);
        }
        notifyCallback();
    }

    public boolean isEventScheduled() {
        return mEventRecordingScheduled;
    }

    public int getScheduledEventType() {
        return mScheduledEventType;
    }

    public long getScheduledDate() {
        return mScheduledDate;
    }

    public void registerOnEventScheduledListener(OnEventScheduledListener l) {
        mListener = new WeakReference<>(l);
    }

    public void unregisterOnEventScheduledListener(OnEventScheduledListener l) {
        if (l == null && mListener != null && mListener.get().equals(l)) {
            mListener.clear();
            mListener = null;
        }
    }

    private void notifyCallback() {
        if (mListener == null || mListener.get() == null) {
            return;
        }
        /* Use UI thread to notify callback */
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mListener.get() != null) {
                    mListener.get().onEventScheduled(mEventRecordingScheduled, mScheduledEventType, mScheduledDate);
                }
            }
        });
    }


    public interface OnEventScheduledListener {
        void onEventScheduled(boolean isEventScheduled, int scheduledEventType, long scheduledDate);
    }
}
