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


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v4.app.NotificationCompat;

public class EventNotificationHandler {
    private static final int NOTIFICATION_ID_EVENT_END_START = 2849309;


    /* Show currently ongoing recording event on status bar */
    public static void showRecordingEventNotification(Context context, int currentEventType) {
        if(context == null) {
            return;
        }
        final Resources res = context.getResources();

        final int[] colors = res.getIntArray(R.array.event_colors);
        final String[] events = res.getStringArray(R.array.event_explanations);

        final TypedArray icons = res.obtainTypedArray(R.array.event_icons);

        /* Intent for the notification content (on notification click open app)*/
        final Intent intent = new Intent(context, ActivityMain.class);
        intent.setAction(Long.toString(System.currentTimeMillis()));

        /* Intent for the notification button to stop recording current event */
        final Intent cancel = new Intent(context, StartEventService.class);
        cancel.setAction(Long.toString(System.currentTimeMillis()+1));
        cancel.putExtra(StartEventService.INTENT_EXTRA_EVENT_TO_START, currentEventType);

        /* Intent to switch current event to start new event */
        final Intent nextEvent = new Intent(context, StartEventService.class);
        nextEvent.setAction(Long.toString(System.currentTimeMillis()+2));

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(icons.getResourceId(currentEventType, R.drawable.ic_truck_white))
                .setContentTitle(res.getString(R.string.title_recording_event))
                .setContentText(events[currentEventType])
                .setColor(colors[currentEventType])
                .setShowWhen(false)
                .setContentIntent(PendingIntent.getActivity(context, -1, intent, PendingIntent.FLAG_ONE_SHOT));


        notificationBuilder.addAction(R.drawable.ic_notification_action_pause, res.getString(R.string.notification_button_cancel_logging),
                PendingIntent.getService(context, -1, cancel, PendingIntent.FLAG_ONE_SHOT));


            switch (currentEventType) {
                case Event.EVENT_TYPE_NORMAL_BREAK:
                case Event.EVENT_TYPE_DAILY_REST:
                case Event.EVENT_TYPE_WEEKLY_REST:
                case Event.EVENT_TYPE_OTHER_WORK:
                case Event.EVENT_TYPE_POA:
                    nextEvent.putExtra(StartEventService.INTENT_EXTRA_EVENT_TO_START, Event.EVENT_TYPE_DRIVING);
                    notificationBuilder.addAction(R.drawable.ic_action_driving, res.getString(R.string.notification_button_start_drive_time), PendingIntent.getService(context, -1, nextEvent, android.app.PendingIntent.FLAG_ONE_SHOT));
                    break;
                case Event.EVENT_TYPE_DRIVING:
                    nextEvent.putExtra(StartEventService.INTENT_EXTRA_EVENT_TO_START, Event.EVENT_TYPE_NORMAL_BREAK);
                    notificationBuilder.addAction(R.drawable.ic_action_rest, res.getString(R.string.notification_button_start_break), PendingIntent.getService(context, -1, nextEvent, android.app.PendingIntent.FLAG_ONE_SHOT));
                    break;
            }



        final Notification notification = notificationBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_EVENT_END_START, notification);


        icons.recycle();
    }

    public static void hideNotification(Context c){
        NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID_EVENT_END_START);
    }
}
