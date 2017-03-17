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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.velli.tachograph.database.DataBaseHandler;
import com.velli.tachograph.database.DataBaseHandler.OnDatabaseEditedListener;
import com.velli.tachograph.database.DataBaseHandler.OnGetEventTaskCompleted;
import com.velli.tachograph.database.DataBaseHandler.OnGetLatestLocationListener;
import com.velli.tachograph.location.CustomLocation;
import com.velli.tachograph.restingtimes.RegulationTimesSummary;
import com.velli.tachograph.restingtimes.RegulationTimesSummary.OnTotalTimesChanged;
import com.velli.tachograph.restingtimes.RestingTimesUtils;

import android.*;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class BackgroundService extends Service implements OnDatabaseEditedListener, OnGetEventTaskCompleted, OnSharedPreferenceChangeListener, OnTotalTimesChanged, ConnectionCallbacks, OnConnectionFailedListener {
	private static final String TAG = "BackgroundService ";
	private static final float[] ACCURACY_VALUES;
	private static final int NOTIFICATION_ID_EVENT_END_START = 2849309;
	private static final boolean DEBUG = false;
	
	private int mThresholdTime;
	private int mThresholdSpeed;
	private int mCurrentDetectedActivityType;
	
	private float mGpsMinAccuracy = 20.0f;
	
	private Event mCurrentEvent;
    private boolean mGpsPermissionGranted = true;
	private boolean mUseGps = false;
	private boolean mStopped = true;
	private boolean mRemoveZeroMinsEvents = false;
	private boolean mShowNotifications = true;
	
	private boolean mAlarmBreak = false;
	private boolean mAlarmDrive = false;
	private boolean mAlarmDailyRest = false;
	private boolean mAlarmWeeklyRest = false;
	private boolean mAlarmPreferencesChanged = true;
	private boolean mDetectedActivityIsDriving = false;
	private boolean mSwitchAutomaticallyToDriveFromBreak = false;
	private boolean mBreakEventToggled = false;
	
	private final Handler mStopHandler = new Handler();
	
	private CustomLocation mPendingLocation;
	private CustomLocation mLastSavedLocation;
	private CustomLocation mLastLocation;
	private GoogleApiClient mGoogleApiClient;
	private PendingIntent mActivityRecogPendIntent;
	
	private SharedPreferences mPrefs;
	
	static {
		ACCURACY_VALUES = new float[]{10.0f, 20.0f, 50.0f, 100.0f, 200.0f, 500.0f, 1000.0f, 2000.0f, 5000.0f};
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		if(DEBUG){
			Log.i(TAG, TAG + "onCreate()");
		}
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		getPreferences();

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            mGpsPermissionGranted = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

        if (mUseGps && mGpsPermissionGranted) {
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);


		}
		DataBaseHandler.getInstance().registerOnDatabaseEditedListener(this);
		DataBaseHandler.getInstance().getRecordingEvent(this);
		RegulationTimesSummary.getInstance().registerForOnTotalTimesChanged(this);
		mPrefs.registerOnSharedPreferenceChangeListener(this);
		
		mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addApi(ActivityRecognition.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
		
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		if(DEBUG){
			Log.d(TAG, TAG + "onStartCommand()");
		}
		Alarms.alarm(this, intent);
		
		// If the intent contains an update
	    if (ActivityRecognitionResult.hasResult(intent)) {
	    	// Get the update
	        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
	        // Get the most probable activity from the list of activities in the update
	        DetectedActivity mostProbableActivity = result.getMostProbableActivity();

	        // Get the confidence percentage for the most probable activity
	        int confidence = mostProbableActivity.getConfidence();

	        // Get the type of activity
	        int activityType = mostProbableActivity.getType();
	        mostProbableActivity.getVersionCode();
	        
	        if (confidence >= 50) {
	            String mode = Utils.getNameFromType(activityType);
	            
	            mCurrentDetectedActivityType = activityType;
	            mDetectedActivityIsDriving = (activityType == DetectedActivity.IN_VEHICLE);
	            
	            if (activityType == DetectedActivity.ON_FOOT) {
	                DetectedActivity betterActivity = Utils.walkingOrRunning(result.getProbableActivities());

	                if (null != betterActivity) {
	                    mode = Utils.getNameFromType(betterActivity.getType());
	                }
	            }
	            if(DEBUG){
	            	Toast.makeText(this, mode, Toast.LENGTH_SHORT).show();
	            }
	        }
	    }
		return Service.START_STICKY;
	}
	

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		DataBaseHandler.getInstance().unregisterOnDatabaseEditedListener(this);
		mPrefs.unregisterOnSharedPreferenceChangeListener(this);
		if(mGoogleApiClient != null && (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting())){
			mGoogleApiClient.disconnect();
		}
	}
	
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		
	}
	
	private boolean isDetectedActivityDriving(){
		if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			return mDetectedActivityIsDriving;
		} else {
			return true;
		}
	}
	
	private final LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location loc) {
			if(loc == null){
				return;
			}
			if(!isGpsAccurateEnough(loc)){
				if(DEBUG){
					Log.i(TAG, TAG + "GPS not accurate enough, accuracy: " + loc.getAccuracy());
				}
				return;
			}
			
			final CustomLocation location = new CustomLocation(loc);
			location.setTime(loc.getTime());
			final int speed;
			
			if(location.hasSpeed()){
				speed = (int)(location.getSpeed() * 3.6f);
			} else {
				if (mLastLocation != null) {
					speed = (int)Utils.getSpeedFromLocations(location, mLastLocation);
					location.setSpeed(speed);
					
				} else {
					speed = -1;
					location.setSpeed(0);
					if(DEBUG){
						Log.i(TAG, TAG + "onLocationChanged() Speed from GPS unavailable");
					}

				}
			}
			
			if (!filterGpsSpeed(loc, mLastLocation)) {
				if(DEBUG){
					Log.i(TAG, TAG + "GPS speed spiked! ");
				}
				return;
			}
						
			ActivityMain.updateWidget(speed, mCurrentDetectedActivityType, BackgroundService.this);
			if(speed > mThresholdSpeed && speed != -1
					&& isDetectedActivityDriving()
					&& mCurrentEvent != null
					&& (mCurrentEvent.getEventType() == Event.EVENT_TYPE_DRIVING 
					|| mCurrentEvent.getEventType() == Event.EVENT_TYPE_OTHER_WORK)) {
				
				mPendingLocation = null;
				mStopped = false;
				mStopHandler.removeCallbacksAndMessages(null);
				
				
				if (mCurrentEvent.getEventType() != Event.EVENT_TYPE_DRIVING) {
					toggleRecordingEvent(Event.EVENT_TYPE_DRIVING, System.currentTimeMillis(), location);
				} 
				
			
				location.setStatus(mLastSavedLocation == null ? CustomLocation.LOCATION_STATUS_START : CustomLocation.LOCATION_STATUS_RUNNING);
				location.setEventId(mCurrentEvent.getRowId());
				saveLocation(location);
				
				
			} else if(speed <= mThresholdSpeed 
					&& speed != -1
					&& !mStopped
					&& mCurrentEvent != null
					&& mCurrentEvent.getEventType() == Event.EVENT_TYPE_DRIVING) {
				
				mPendingLocation = null;
				mStopped = true;
				mStopHandler.removeCallbacksAndMessages(null);
				
				if(mThresholdTime == 0){
					
					toggleRecordingEvent(Event.EVENT_TYPE_OTHER_WORK, System.currentTimeMillis(), location);
					location.setStatus(Event.EVENT_TYPE_OTHER_WORK);
					location.setEventId(mCurrentEvent.getRowId());
					saveLocation(location);
					
					
				} else {
					mPendingLocation = location;
					mStopHandler.postDelayed(mStopRunnable, (mThresholdTime* 1000));
				}
				
			} else if(mCurrentEvent == null){
				mStopped = true;
				mPendingLocation = null;
				mStopHandler.removeCallbacksAndMessages(null);
				mLastLocation = null;
				return;
			}

			
			mLastLocation = location;
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			if(DEBUG){
				Log.i(TAG, TAG + "onProviderEnabled(" + provider + ")");
			}

			DataBaseHandler.getInstance().getLatestLocation(new OnGetLatestLocationListener() {
				
				@Override
				public void onGetLatestLocation(CustomLocation location) {
					if(location != null 
							&& (location.getStatus() == CustomLocation.LOCATION_STATUS_RUNNING 
							|| location.getStatus() == CustomLocation.LOCATION_STATUS_START)){
						
						mLastSavedLocation = location;
						
					}
				}
			}, mCurrentEvent.getRowId());
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			if(DEBUG){
				Log.i(TAG, TAG + "onProviderDisabled(" + provider + ")");
			}
			
			if (mLastSavedLocation != null) {
				mLastSavedLocation.setStatus(CustomLocation.LOCATION_STATUS_STOP);
				saveLocation(mLastSavedLocation);
				mLastSavedLocation = null;
			}
			
		}
	};


	
	private void saveLocation(CustomLocation loc){
        mLastSavedLocation = loc;
		if (loc != null) {
			DataBaseHandler.getInstance().addLocation(loc);
		}
	}
	

	@Override
	public void onDatabaseEdited(int action, int rowId) {
		DataBaseHandler.getInstance().getRecordingEvent(this);
		
	}

	@Override
	public void onGetEvent(Event ev) {
		mAlarmPreferencesChanged = true;
		if(ev == null){
			hideNotification(this);
			Alarms.cancelAllAlarms(this);
		}
		if(ev == null && mCurrentEvent != null){
			ActivityMain.updateWidget(-1, this);
			
			if(mLastSavedLocation != null){
				mLastSavedLocation.setStatus(CustomLocation.LOCATION_STATUS_STOP);
				saveLocation(mLastSavedLocation);
				mLastSavedLocation = null;
			}
			
			mBreakEventToggled = false;
			mStopped = true;
			mPendingLocation = null;
			mStopHandler.removeCallbacksAndMessages(null);
			
		} else if(ev != null) {
			ActivityMain.updateWidget(ev.getEventType(), this);
		}
		mStopped = false;
		mCurrentEvent = ev;
	
		enableLocationUpdatesIfNeed(mCurrentEvent, mUseGps);
		
	}

	
	private void enableLocationUpdatesIfNeed(Event currentEvent, boolean useGPS){
		final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if(currentEvent != null 
				&& useGPS
                && mGpsPermissionGranted
				&& (currentEvent.getEventType() == Event.EVENT_TYPE_DRIVING 
				|| currentEvent.getEventType() == Event.EVENT_TYPE_OTHER_WORK)){
			
			if(mGoogleApiClient != null && !mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()){
				mGoogleApiClient.connect();
			}
			if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
				locationListener.onProviderEnabled(LocationManager.GPS_PROVIDER);
			} 
			mLastLocation = null;
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0.0f, locationListener);
		} else {
			if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
				locationListener.onProviderDisabled(LocationManager.GPS_PROVIDER);
			}
			mLastLocation = null;
			locationManager.removeUpdates(locationListener);
			if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
				ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, mActivityRecogPendIntent);
				mGoogleApiClient.disconnect();
			}
		}
	}
	
	private final Runnable mStopRunnable = new Runnable(){
		
		@Override
		public void run(){
			if(mPendingLocation != null){
				mPendingLocation.setStatus(CustomLocation.LOCATION_STATUS_STOP);
				saveLocation(mPendingLocation);
			}
			
			toggleRecordingEvent(Event.EVENT_TYPE_OTHER_WORK, System.currentTimeMillis() - (mThresholdTime * 1000), mPendingLocation);

		}
	};
	

	private void toggleRecordingEvent(final int event, final long millis, final Location loc){
		if(DEBUG){
			Log.i(TAG, TAG + "toggleRecordingEvent()");
		}
		
		DataBaseHandler.getInstance().getRecordingEvent(new OnGetEventTaskCompleted() {
			
			@Override
			public void onGetEvent(Event ev) {
				
				if(ev != null && ev.isRecordingEvent()){
					ev.setRecording(false);
					ev.setEndDate(millis);
					ev.setEndTime(DateCalculations.getCurrentHour(millis), DateCalculations.getCurrentMinute(millis));
					
					if(mRemoveZeroMinsEvents && RestingTimesUtils.getEventDurationInMin(ev) == 0){
						DataBaseHandler.getInstance().deleteEvent(ev.getRowId());
					} else {
						if(loc != null && ev.getEventType() == Event.EVENT_TYPE_DRIVING){
							DataBaseHandler.getInstance()
							.updateEventWithLocation(ev, loc, false, BackgroundService.this);
						} else {
							DataBaseHandler.getInstance().updateEvent(ev, true);
						}
					}
					
					if(ev.getEventType() == event && mLastSavedLocation != null){
						mLastSavedLocation.setStatus(CustomLocation.LOCATION_STATUS_STOP);
						saveLocation(mLastSavedLocation);
						mLastSavedLocation = null;
					}
				}
				
				if((ev != null && ev.getEventType() != event) || ev == null){
					final Event eventToStart = new Event();
					eventToStart.setRecording(true);
					eventToStart.setStartDate(millis);
					eventToStart.setStartTime(DateCalculations.getCurrentHour(millis), DateCalculations.getCurrentMinute(millis));
					eventToStart.setEventType(event);
					
					if(loc != null && event == Event.EVENT_TYPE_DRIVING){
						DataBaseHandler.getInstance().addNewEventWithLocation(eventToStart, loc, true, BackgroundService.this);
					} else {
						DataBaseHandler.getInstance().addNewEvent(eventToStart);
					}
				}
				
			}
		});
	}
	
	

	private static void hideNotification(Context c){
		NotificationManager notificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID_EVENT_END_START);
	}
	
	private void showNotification(int event){
		if(!mShowNotifications){
			return;
		}
		
		final Resources res = getResources();
		final RegulationTimesSummary summary = RegulationTimesSummary.getInstance();
		final int nextEventType = RegulationTimesSummary.getInstance().getNextEvent(event);
		final int[] colors = res.getIntArray(R.array.event_colors);
		final TypedArray icons = res.obtainTypedArray(R.array.event_icons);
		int mins = 0;
		String subTitle = "";
		
		final Intent intent = new Intent(this, ActivityMain.class);		
		intent.setAction(Long.toString(System.currentTimeMillis()));
		
		final Intent cancel = new Intent(this, StartEventService.class);
		cancel.setAction(Long.toString(System.currentTimeMillis()));
		cancel.putExtra(StartEventService.INTENT_EXTRA_EVENT_TO_START, event);
		
		final Intent rest = new Intent(this, StartEventService.class);
		rest.setAction(Long.toString(System.currentTimeMillis()*2));
		rest.putExtra(StartEventService.INTENT_EXTRA_EVENT_TO_START, nextEventType);
		
		final PendingIntent pendIntent = PendingIntent.getService(this, -1, rest, PendingIntent.FLAG_ONE_SHOT);

		if(nextEventType == Event.EVENT_TYPE_NORMAL_BREAK) {
			mins = summary.getContiniousBreakLimit();
			subTitle = res.getString(R.string.notification_next_event_break);
		} else if(nextEventType == Event.EVENT_TYPE_DAILY_REST){
			mins = RegulationTimesSummary.getInstance().getDailyRestLimit();
			subTitle = res.getString(R.string.notification_next_event_daily_rest);
		} else if(nextEventType == Event.EVENT_TYPE_WEEKLY_REST){
			mins = RegulationTimesSummary.getInstance().getWeeklyRestLimit();
			subTitle = res.getString(R.string.notification_next_event_weekly_rest);
		} else if(nextEventType == Event.EVENT_TYPE_DRIVING){
			mins = RegulationTimesSummary.getInstance().getContiniousDriveLimit();
		    subTitle = res.getString(R.string.notification_next_event_driving);
		}
		
		NotificationCompat.Builder mBuilder =
			    new NotificationCompat.Builder(this)
			    .setSmallIcon(icons.getResourceId(event, R.drawable.ic_truck_white))
			    .setContentTitle(getString(R.string.title_recording_event))
			    .addAction(R.drawable.ic_notification_action_pause, 
			    		res.getString(R.string.notification_button_cancel_logging), 
			    		PendingIntent.getService(this, -1, cancel, PendingIntent.FLAG_ONE_SHOT))
			    .setColor(colors[event])
			    .setShowWhen(false)
			    .setContentIntent(PendingIntent.getActivity(this, -1, intent, PendingIntent.FLAG_ONE_SHOT));
		
		if(!subTitle.isEmpty()){
			mBuilder.setContentText(subTitle + ", " + DateCalculations.convertMinutesToTimeString(mins));

		}
		if(nextEventType != -1){
			if(nextEventType == Event.EVENT_TYPE_NORMAL_BREAK
					|| nextEventType == Event.EVENT_TYPE_DAILY_REST
					|| nextEventType == Event.EVENT_TYPE_DRIVING
					|| nextEventType == Event.EVENT_TYPE_WEEKLY_REST){
				mBuilder.setStyle(new NotificationCompat.BigTextStyle()
				.bigText(subTitle 
	        		 + "\n" 
			         + DateCalculations.convertMinutesToTimeString(mins) 
			         + "\n" +DateCalculations.createDateTimeString(summary.getNextEventStartTime(event))));
			}
			if(nextEventType == Event.EVENT_TYPE_NORMAL_BREAK){
				mBuilder.addAction(R.drawable.ic_action_rest, res.getString(R.string.notification_button_start_break), pendIntent);
			} else if(nextEventType == Event.EVENT_TYPE_DAILY_REST){
				mBuilder.addAction(R.drawable.ic_action_rest, res.getString(R.string.notification_button_start_daily_rest), pendIntent);
			} else if(nextEventType == Event.EVENT_TYPE_WEEKLY_REST){
				mBuilder.addAction(R.drawable.ic_action_rest, res.getString(R.string.notification_button_start_weekly_rest), pendIntent);
			} else if(nextEventType == Event.EVENT_TYPE_DRIVING){
				mBuilder.addAction(R.drawable.ic_action_driving, res.getString(R.string.notification_button_start_drive_time), pendIntent);
			}
			
		}
		
		final Notification notification = mBuilder.build();
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID_EVENT_END_START, notification);
		
		icons.recycle();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		mAlarmPreferencesChanged = true;
		
		getPreferences();
		enableLocationUpdatesIfNeed(mCurrentEvent, mUseGps);
		
		if(!mShowNotifications){
			hideNotification(this);
		} else if(mCurrentEvent != null){
			showNotification(mCurrentEvent.getEventType());
		}
	}
	
	private void getPreferences(){
		final Resources res = getResources();
		
		mSwitchAutomaticallyToDriveFromBreak = mPrefs.getBoolean(res.getString(R.string.preference_key_switch_automatically_to_drive), false);
		mThresholdTime = mPrefs.getInt(res.getString(R.string.preference_key_threshold_time), 300);
		mThresholdSpeed = mPrefs.getInt(res.getString(R.string.preference_key_threshold_value), 7);
		mUseGps = mPrefs.getBoolean(res.getString(R.string.preference_key_use_gps), false);
		mShowNotifications = mPrefs.getBoolean(res.getString(R.string.preference_key_show_notifications), true);
		mRemoveZeroMinsEvents = mPrefs.getBoolean(res.getString(R.string.preference_key_remove_under_1_min_logs), false);
		mGpsMinAccuracy = ACCURACY_VALUES[mPrefs.getInt(res.getString(R.string.preference_key_gps_min_accuracy), 4)];
		
		mAlarmBreak = mPrefs.getInt(res.getString(R.string.preference_key_alarm_break_time_ending), 0) > -1;
		mAlarmDrive = mPrefs.getInt(res.getString(R.string.preference_key_alarm_drive_time_ending), 0) > -1;
		mAlarmDailyRest = mPrefs.getInt(res.getString(R.string.preference_key_alarm_daily_rest_ending), 0) > -1;
		mAlarmWeeklyRest = mPrefs.getInt(res.getString(R.string.preference_key_alarm_weekly_rest_ending), 0) > -1;

		if (mAlarmPreferencesChanged && mShowNotifications && mCurrentEvent != null) {
			Alarms.sheduleAlarms(this, 
					mAlarmBreak && mCurrentEvent.getEventType() == Event.EVENT_TYPE_NORMAL_BREAK, 
					mAlarmDrive && mCurrentEvent.getEventType() == Event.EVENT_TYPE_DRIVING,
					mAlarmDailyRest && mCurrentEvent.getEventType() == Event.EVENT_TYPE_DAILY_REST,
					mAlarmWeeklyRest && mCurrentEvent.getEventType() == Event.EVENT_TYPE_WEEKLY_REST);
			mAlarmPreferencesChanged = false;
		} else if(!mShowNotifications){
			Alarms.cancelAllAlarms(this);
		}
	}
	

	private boolean isGpsAccurateEnough(Location location){
		if(location != null && location.getAccuracy() <= mGpsMinAccuracy){
			return true;
		} else {
			return false;
		}
	}
	
	private static boolean filterGpsSpeed(Location location, Location lastLocation){
		if(location != null && lastLocation != null){
			long time = lastLocation.getTime() - location.getTime();
			
			if(time >= 1000 && time < 2000){
				if(lastLocation.getSpeed() - location.getSpeed() > 6.94f){
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void timesChanged() {
		if(mCurrentEvent != null){
			showNotification(mCurrentEvent.getEventType());
		} else {
			hideNotification(this);
		}
		
		if (mAlarmPreferencesChanged && mShowNotifications && mCurrentEvent != null) {
			Alarms.sheduleAlarms(this, 
					mAlarmBreak && mCurrentEvent.getEventType() == Event.EVENT_TYPE_NORMAL_BREAK, 
					mAlarmDrive && mCurrentEvent.getEventType() == Event.EVENT_TYPE_DRIVING,
					mAlarmDailyRest && mCurrentEvent.getEventType() == Event.EVENT_TYPE_DAILY_REST,
					mAlarmWeeklyRest && mCurrentEvent.getEventType() == Event.EVENT_TYPE_WEEKLY_REST);
			mAlarmPreferencesChanged = false;
		} else if(!mShowNotifications){
			Alarms.cancelAllAlarms(this);
		}
		
		if(mSwitchAutomaticallyToDriveFromBreak 
				&& mCurrentEvent != null 
				&& mCurrentEvent.isRecordingEvent()
				&& !mBreakEventToggled
				&& mCurrentEvent.getEventType() == Event.EVENT_TYPE_NORMAL_BREAK) {
			final RegulationTimesSummary summary = RegulationTimesSummary.getInstance();
			
			if(summary.getContiniousBreak() >= summary.getContiniousBreakLimit()) {
				mBreakEventToggled = true;
				toggleRecordingEvent(Event.EVENT_TYPE_DRIVING, System.currentTimeMillis(), mLastLocation);
			}
		}
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Intent intent = new Intent(getApplicationContext(), BackgroundService.class); // your custom ARS class
		mActivityRecogPendIntent = PendingIntent.getService(getApplicationContext(), 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        
		ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 2000, mActivityRecogPendIntent);

		
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		
	}
	
	
}
