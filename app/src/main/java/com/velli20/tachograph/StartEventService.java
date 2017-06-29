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

import android.app.IntentService;
import android.content.Intent;

public class StartEventService extends IntentService {
    public static final String INTENT_EXTRA_EVENT = "event widget";
    public static final String INTENT_EXTRA_CURRENT_EVENT = "current event";
    public static final String INTENT_EXTRA_EVENT_TO_START = "event to start";
    public static final String TAG = "StartEventService ";


    public StartEventService() {
        super(StartEventService.class.getName());
    }


    @Override
    protected void onHandleIntent(Intent i) {

        if (i != null) {
            int event = i.getIntExtra(INTENT_EXTRA_EVENT, -1);
            int eventToStart = i.getIntExtra(INTENT_EXTRA_EVENT_TO_START, -1);
            int currentEvent = i.getIntExtra(INTENT_EXTRA_CURRENT_EVENT, -1);

            if (eventToStart != -1) {
                EventRecorder.INSTANCE.startRecordingEvent(eventToStart, System.currentTimeMillis());
            } else {
                if (event == 0) {
                    EventRecorder.INSTANCE.startRecordingEvent(Event.EVENT_TYPE_DRIVING, System.currentTimeMillis());
                } else if (event == 1) {
                    final Intent choose = new Intent(this, ActivityRestTypeChooser.class);
                    choose.putExtra(ActivityRestTypeChooser.INTENT_EXTRA_CURRENT_EVENT_TYPE, currentEvent);
                    choose.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(choose);
                    stopSelf();
                }
                if (event == 2) {
                    EventRecorder.INSTANCE.startRecordingEvent(Event.EVENT_TYPE_OTHER_WORK, System.currentTimeMillis());
                }
                if (event == 3) {
                    EventRecorder.INSTANCE.startRecordingEvent(Event.EVENT_TYPE_POA, System.currentTimeMillis());

                }
            }
        }

    }

}
