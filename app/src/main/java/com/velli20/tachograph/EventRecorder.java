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
import android.preference.PreferenceManager;

import com.velli20.tachograph.database.DataBaseHandler;

public class EventRecorder {

    public EventRecorder() { }


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
                if(ev != null) {
                    /* Stop this event */
                    endRecordingEvent(ev, System.currentTimeMillis());
                }
                if((ev != null && ev.getEventType() != eventToRecord.getEventType()) || ev == null) {

                    /* Add recording event to the database */
                    DataBaseHandler.getInstance().addNewEvent(eventToRecord);
                }
            }
        });
    }

    /* End recording event */
    public void endRecordingEvent(Event ev, long endTime) {
        if(ev == null) {
            return;
        }
        final Resources res = App.get().getResources();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.get());

        boolean deleteOneMinEvents = prefs.getBoolean(res.getString(R.string.preference_key_remove_under_1_min_logs), true);
        ev.setRecording(false);
        ev.setEndDate(endTime);
        ev.setEndTime(DateUtils.getCurrentHour(endTime), DateUtils.getCurrentMinute(endTime));

        /* Avoid adding events to database with 0 min duration */
        if(ev.getEndDateInMillis() - ev.getStartDateInMillis() < (60*1000) && deleteOneMinEvents) {
            DataBaseHandler.getInstance().deleteEvent(ev.getRowId());

        } else {
            DataBaseHandler.getInstance().updateEvent(ev);
        }
    }
}
