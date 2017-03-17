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

package com.velli.tachograph;

import com.velli.tachograph.restingtimes.RegulationTimesSummary;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

class Alarms {
	private static final String TAG = "Alarms ";
	private static final String INTENT_ACTION_ALARM = "com.velli.tachograph.ALARM";
	private static final String INTENT_EXTRA_ALARM_TYPE = "com.velli.tachograph.alarm_type";
	
	private static final int ALARM_NOTIFICATION_ID = 679945034;
	private static final int ALARM_REQUEST_CODE = 2930459;
	private static final int ALARM_TYPE_BREAK = 0;
	private static final int ALARM_TYPE_DRIVE = 1;
	private static final int ALARM_TYPE_DAILY_REST = 2;
	private static final int ALARM_TYPE_WEEKLY_REST = 3;
	
	
	private static final boolean DEBUG = false;
	
	public static void sheduleAlarms(Context c, boolean breakTime, boolean driveTime, boolean dailyRest, boolean weeklyRest){
		if(DEBUG){
			Log.i(TAG, TAG + "sheduleAlarms()");
		}
		if(breakTime){
			scheduleAlarm(c, ALARM_TYPE_BREAK);
		} else {
			cancelAlarm(c, ALARM_TYPE_BREAK);
		}
		
		if(driveTime){
			scheduleAlarm(c, ALARM_TYPE_DRIVE);
		} else {
			cancelAlarm(c, ALARM_TYPE_DRIVE);
		}
		
		if(dailyRest){
			scheduleAlarm(c, ALARM_TYPE_DAILY_REST);
		} else {
			cancelAlarm(c, ALARM_TYPE_DAILY_REST);
		}
		
		if(weeklyRest){
			scheduleAlarm(c, ALARM_TYPE_WEEKLY_REST);
		} else {
			cancelAlarm(c, ALARM_TYPE_WEEKLY_REST);
		}
	}
	
	private static void scheduleAlarm(Context c, int alarmType){
		if(DEBUG){
			Log.i(TAG, TAG + "scheduleAlarm(" + alarmType + ")");
		}
		
		final AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        final PendingIntent pi;
        
        final long millis = getAlarmTime(c, alarmType);
        
        if(millis > System.currentTimeMillis()){
        	pi = PendingIntent.getService(c, ALARM_REQUEST_CODE + alarmType, getIntent(c, alarmType), 0);
        	if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
        		am.setExact(AlarmManager.RTC_WAKEUP, millis, pi);
        	} else {
        		am.set(AlarmManager.RTC_WAKEUP, millis, pi);
        	}
        	
        	if(DEBUG){
    			Toast.makeText(c, "Alarm scheduled on: " + DateCalculations.createDateTimeString(millis), Toast.LENGTH_LONG).show();
    		}
		} else {
			pi = PendingIntent.getService(c, ALARM_REQUEST_CODE + alarmType, getIntent(c, alarmType), PendingIntent.FLAG_UPDATE_CURRENT);
			am.cancel(pi);
		}
	}
	
	private static Intent getIntent(Context c, int alarmType){
        final Intent i = new Intent(c, BackgroundService.class);
        i.setAction(INTENT_ACTION_ALARM);
        i.putExtra(INTENT_EXTRA_ALARM_TYPE, alarmType);
        return i;
	}
	
	public static void cancelAllAlarms(Context c){
		final AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
		am.cancel(PendingIntent.getService(c, ALARM_REQUEST_CODE + ALARM_TYPE_BREAK, getIntent(c, ALARM_TYPE_BREAK), PendingIntent.FLAG_UPDATE_CURRENT));
		am.cancel(PendingIntent.getService(c, ALARM_REQUEST_CODE + ALARM_TYPE_DAILY_REST, getIntent(c, ALARM_TYPE_DAILY_REST), PendingIntent.FLAG_UPDATE_CURRENT));
		am.cancel(PendingIntent.getService(c, ALARM_REQUEST_CODE + ALARM_TYPE_DRIVE, getIntent(c, ALARM_TYPE_DRIVE), PendingIntent.FLAG_UPDATE_CURRENT));
		am.cancel(PendingIntent.getService(c, ALARM_REQUEST_CODE + ALARM_TYPE_WEEKLY_REST, getIntent(c, ALARM_TYPE_WEEKLY_REST), PendingIntent.FLAG_UPDATE_CURRENT));
	}
	
	private static void cancelAlarm(Context c, int alarmType){
		if(DEBUG){
			Toast.makeText(c,  "cancelAlarm(" + alarmType + ")", Toast.LENGTH_LONG).show();
		}
		
		final AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        final PendingIntent pi = PendingIntent.getService(c, ALARM_REQUEST_CODE + alarmType, getIntent(c, alarmType), PendingIntent.FLAG_UPDATE_CURRENT);
		am.cancel(pi);
	}
	
	private static long getAlarmTime(Context c, int alarmType){
		final RegulationTimesSummary summary = RegulationTimesSummary.getInstance();
		final Resources res = c.getResources();
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		
		int user = 0;
		long startTime = 0;
		
		switch(alarmType){
		case ALARM_TYPE_BREAK:
			user = prefs.getInt(res.getString(R.string.preference_key_alarm_break_time_ending), 10);
			startTime = summary.getNextEventStartTime(Event.EVENT_TYPE_NORMAL_BREAK);
			break;
		case ALARM_TYPE_DRIVE:
			user = prefs.getInt(res.getString(R.string.preference_key_alarm_drive_time_ending), 10);
			startTime = summary.getNextEventStartTime(Event.EVENT_TYPE_DRIVING);
			break;
		case ALARM_TYPE_DAILY_REST:
			user = prefs.getInt(res.getString(R.string.preference_key_alarm_daily_rest_ending), 10);
			startTime = summary.getNextEventStartTime(Event.EVENT_TYPE_DAILY_REST);
			break;
		case ALARM_TYPE_WEEKLY_REST:
			user = prefs.getInt(res.getString(R.string.preference_key_alarm_weekly_rest_ending), 10);
			startTime = summary.getNextEventStartTime(Event.EVENT_TYPE_WEEKLY_REST);
			break;
		}
		return startTime - (user * 60 * 1000);
	}
	
	
	public static void alarm(Context c, Intent i){
		if(DEBUG){
			Log.i(TAG, TAG + "alarm()");
		}
		if(i == null || c == null || !INTENT_ACTION_ALARM.equals(i.getAction())){
			return;
		}
		final Resources res = c.getResources();
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		final TypedArray icons = res.obtainTypedArray(R.array.event_icons);

		
		final Intent intent = new Intent(c, ActivityMain.class);		
		intent.setAction(Long.toString(System.currentTimeMillis()));
		
		final int alarmType = i.getIntExtra(INTENT_EXTRA_ALARM_TYPE, -1);
		final int event;
		final int[] colors = res.getIntArray(R.array.event_colors);
		
		if(alarmType == -1) {
			return;
		}
		final String title;
		final String sound = prefs.getString(res.getString(R.string.preference_key_notification_sound), "");
		
		final boolean vibrate = prefs.getBoolean(res.getString(R.string.preference_key_notification_vibrate), true);
		if(alarmType == ALARM_TYPE_BREAK){
			int mins = DateCalculations.getTimeDifferenceInMins(System.currentTimeMillis(), 
					RegulationTimesSummary.getInstance().getNextEventStartTime(Event.EVENT_TYPE_NORMAL_BREAK), -1);
			if(mins >= 1){
				title = res.getString(R.string.notification_break_time_ending) + " " + DateCalculations.convertMinutesToTimeString(mins) + " " + res.getString(R.string.notification_in_time);
			} else {
				title = res.getString(R.string.notification_break_time_ended);
			}
			event = Event.EVENT_TYPE_NORMAL_BREAK;
		} else if(alarmType == ALARM_TYPE_DRIVE){
			int mins = DateCalculations.getTimeDifferenceInMins(System.currentTimeMillis(), 
					RegulationTimesSummary.getInstance().getNextEventStartTime(Event.EVENT_TYPE_DRIVING), -1);
			if(mins >= 1){
				title = res.getString(R.string.notification_drive_time_ending) + " " + DateCalculations.convertMinutesToTimeString(mins) + " " + res.getString(R.string.notification_in_time);
			} else {
				title = res.getString(R.string.notification_drive_time_ended);
			}
			event = Event.EVENT_TYPE_DRIVING;
		} else if(alarmType == ALARM_TYPE_DAILY_REST){
			int mins = DateCalculations.getTimeDifferenceInMins(System.currentTimeMillis(), 
					RegulationTimesSummary.getInstance().getNextEventStartTime(Event.EVENT_TYPE_DAILY_REST), -1);
			if(mins >= 1){
				title = res.getString(R.string.notification_daily_rest_ending) + " " + DateCalculations.convertMinutesToTimeString(mins) + " " + res.getString(R.string.notification_in_time);
			} else {
				title = res.getString(R.string.notification_daily_rest_ended);
			}
			event = Event.EVENT_TYPE_DAILY_REST;
		} else {
			int mins = DateCalculations.getTimeDifferenceInMins(System.currentTimeMillis(), 
					RegulationTimesSummary.getInstance().getNextEventStartTime(Event.EVENT_TYPE_WEEKLY_REST), -1);
			if(mins >= 1){
				title = res.getString(R.string.notification_weekly_rest_ending) + " " + DateCalculations.convertMinutesToTimeString(mins) + " " + res.getString(R.string.notification_in_time);
			} else {
				title = res.getString(R.string.notification_weekly_rest_ended);
			}
			event = Event.EVENT_TYPE_WEEKLY_REST;
		}

		
		final NotificationCompat.Builder builder =
			    new NotificationCompat.Builder(c)
			    .setSmallIcon(icons.getResourceId(event, R.drawable.ic_truck_white))
			    .setContentTitle(title)
			    .setContentText(res.getString(R.string.notification_tap_to_show_more_info))
			    .setContentIntent(PendingIntent.getActivity(c, -1, intent, PendingIntent.FLAG_ONE_SHOT))
			    .setColor(colors[event]);
		        
	    if(vibrate) {
	    	builder.setVibrate(new long[] { 1000, 1000});
	    }
	    
	    if(!sound.isEmpty()) {
	    	builder.setSound(Uri.parse(sound));
	    }
	    
		icons.recycle();
		
		NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(ALARM_NOTIFICATION_ID, builder.build());
		
		if(DEBUG){
			Log.i(TAG, TAG + "alarm() alarming!");
		}
	}


}
