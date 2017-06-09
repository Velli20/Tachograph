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

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.velli20.tachograph.database.DataBaseHandler;


public class GpsBackgroundService extends Service implements DataBaseHandler.OnDatabaseEditedListener, DataBaseHandler.OnGetEventTaskCompleted, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final float[] ACCURACY_VALUES;
    private static final boolean DEBUG = true;
    private static final String TAG = "GpsBackgroundService ";

    private GoogleApiDetectedActivityListener mGoogleApiDetectedActivityListener;
    private GpsRouteLogger mGpsRouteLogger = new GpsRouteLogger();

    private int mThresholdTime;
    private int mThresholdSpeed;

    private float mGpsMinAccuracy = 20.0f;

    private boolean mUseGps = false;
    private boolean mShowNotifications = true;
    private boolean mNotificationShowing = false;
    private boolean mShuttingDownService = false;
    private boolean mStoppingRouteLogging = false;

    private final Handler mServiceShutdownHandler = new Handler();
    private final Handler mStoppingRouteLoggingHandler = new Handler();

    private Event mRecordingEvent;

    static {
        ACCURACY_VALUES = new float[]{10.0f, 20.0f, 50.0f, 100.0f, 200.0f, 500.0f, 1000.0f, 2000.0f, 5000.0f};
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        getPreferences();

        /* Listen preference changes */
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        /* Listen database changes */
        DataBaseHandler.getInstance().registerOnDatabaseEditedListener(this);
        DataBaseHandler.getInstance().getRecordingEvent(this);

        if(DEBUG) {
            Log.i(TAG, TAG + " onCreate()");
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        /* Check if intent contains ActivityRecognitionResult */
        if (intent != null && ActivityRecognitionResult.hasResult(intent) && mGoogleApiDetectedActivityListener != null) {
            /* Handle this intent */
            mGoogleApiDetectedActivityListener.handleActivityRecognitionResult(intent);
        }

        if(DEBUG) {
            Log.i(TAG, TAG + " onStartCommand()");
        }
        return Service.START_STICKY;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mGpsRouteLogger != null) {
            mGpsRouteLogger.unregisterLocationListener(this);
            mGpsRouteLogger = null;
        }
        if(mGoogleApiDetectedActivityListener != null) {
            mGoogleApiDetectedActivityListener.stop();
            mGoogleApiDetectedActivityListener = null;
        }
        DataBaseHandler.getInstance().unregisterOnDatabaseEditedListener(this);

        mServiceShutdownHandler.removeCallbacks(mServiceShutdownRunnable);
        mStoppingRouteLoggingHandler.removeCallbacks(mRouteLoggingShutdownRunnable);

        EventNotificationHandler.hideNotification(this);

        if(DEBUG) {
            Log.i(TAG, TAG + " onDestroy()");
        }
    }

    /* Check if we need this service. If user is not enabled GPS logging
     * or there are no event currently recording then stop this service
     */
    private boolean logRouteIfRequired(Event currentEvent) {
        if(DEBUG) {
            Log.i(TAG, TAG + " logRouteIfRequired() event null: " + (currentEvent == null ? "YES" : "NO"));
        }
        if(!mUseGps || currentEvent == null) {
            return false;
        }

        if (checkIfRequiredToTurnOnGps(currentEvent)) {
            /* Enable GPS route logging. This class is also responsible to switch from
             * driving event to other work and vice versa based on GPS speed
             */
            if(mGpsRouteLogger == null) {
                mGpsRouteLogger = new GpsRouteLogger();
            }
            mGpsRouteLogger.registerLocationListener(this);
            mGpsRouteLogger.setGpsMinAccuracy(mGpsMinAccuracy);
            mGpsRouteLogger.setStoppedSpeedThreshold(mThresholdSpeed);
            mGpsRouteLogger.setStoppedTimeThreshold(mThresholdTime);
            mGpsRouteLogger.setCurrentEvent(currentEvent);

            /* Initialize DetectedActivity listener. This class is used to determinate whether
             * user is walking or driving based on various sensor data
             */
            if (mGoogleApiDetectedActivityListener == null) {
                mGoogleApiDetectedActivityListener = new GoogleApiDetectedActivityListener(GpsBackgroundService.this);
            }

            return true;

        } else {
            return false;
        }

    }

    /* Check if need to turn on GPS */
    public boolean checkIfRequiredToTurnOnGps(Event currentEvent) {
        if(currentEvent == null) {
            return false;
        } else if(currentEvent.getEventType() == Event.EVENT_TYPE_DRIVING
                || currentEvent.getEventType() == Event.EVENT_TYPE_OTHER_WORK) {
            return true;

        }
        return false;
    }

    private void stopLoggingRoute() {
        if(DEBUG) {
            Log.i(TAG, TAG + " stopLoggingRoute()");
        }
        if(mGpsRouteLogger != null) {
            mGpsRouteLogger.unregisterLocationListener(this);
        }

        if(mGoogleApiDetectedActivityListener != null) {
            mGoogleApiDetectedActivityListener.stop();
            mGoogleApiDetectedActivityListener = null;
        }

    }

    private boolean showNotificationIfRequired(Event currentEvent) {
        if(!mShowNotifications || currentEvent == null) {
            if(mNotificationShowing) {
            /* User turned notifications off. Hide currently showing notification */
                EventNotificationHandler.hideNotification(this);
            }
            return false;
        }

        mNotificationShowing = true;
        EventNotificationHandler.showRecordingEventNotification(this, currentEvent.getEventType());

        return true;
    }



    /* Load user settings */
    private void getPreferences() {
        final Resources res = getResources();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mThresholdTime = prefs.getInt(res.getString(R.string.preference_key_threshold_time), 300);
        mThresholdSpeed = prefs.getInt(res.getString(R.string.preference_key_threshold_value), 7);
        mUseGps = prefs.getBoolean(res.getString(R.string.preference_key_use_gps), false);
        mShowNotifications = prefs.getBoolean(res.getString(R.string.preference_key_show_notifications), true);
        mGpsMinAccuracy = ACCURACY_VALUES[prefs.getInt(res.getString(R.string.preference_key_gps_min_accuracy), 4)];

        /* Edit GPS route logger settings */
        if(mGpsRouteLogger != null) {
            mGpsRouteLogger.setGpsMinAccuracy(mGpsMinAccuracy);
            mGpsRouteLogger.setStoppedSpeedThreshold(mThresholdSpeed);
            mGpsRouteLogger.setStoppedTimeThreshold(mThresholdTime);
        }
        checkIfRequiredToRunBackgroundService();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        getPreferences();
        if(DEBUG) {
            Log.d(TAG, "onSharedPreferenceChanged()");
        }
    }


    @Override
    public void onGetEvent(Event ev) {
        if(DEBUG) {
            Log.i(TAG, TAG + " onGetEvent()");
        }
        mRecordingEvent = ev;
        checkIfRequiredToRunBackgroundService();
    }


    @Override
    public void onDatabaseEdited(int action, int rowId) {
        if(DEBUG) {
            Log.i(TAG, TAG + " onDatabaseEdited()");
        }
        /* Loads asynchronously current recording Event from app database */
        DataBaseHandler.getInstance().getRecordingEvent(this);
    }

    /* Check if it is required to continue running this service. If not then
     * stop this service with a 5 second delay.
     */
    private void checkIfRequiredToRunBackgroundService() {
        if(DEBUG) {
            Log.i(TAG, TAG + " checkIfRequiredToRunBackgroundService()");
        }
        /* Check if it is required to run this service */
        boolean showNotifications = showNotificationIfRequired(mRecordingEvent);
        boolean logRoute = logRouteIfRequired(mRecordingEvent);

        if(mStoppingRouteLogging && logRoute) {
            mStoppingRouteLogging = false;
            mStoppingRouteLoggingHandler.removeCallbacks(mRouteLoggingShutdownRunnable);
            if(DEBUG) {
                Log.d(TAG, "checkIfRequiredToRunBackgroundService() continue route logging");
            }
        } else if(!mStoppingRouteLogging && !logRoute) {
            mStoppingRouteLogging = true;
            mStoppingRouteLoggingHandler.postDelayed(mRouteLoggingShutdownRunnable, 5000);
            if(DEBUG) {
                Log.d(TAG, "checkIfRequiredToRunBackgroundService() stopping route logging in 5 seconds");
            }
        }

        if(!showNotifications && !logRoute) {
            /* Wait 5 seconds until we stop this service. If user starts
             * another event that requires this service then cancel this Runnable
             */
            mShuttingDownService = true;
            mServiceShutdownHandler.postDelayed(mServiceShutdownRunnable, 5000);

            if(DEBUG) {
                Log.d(TAG, "checkIfRequiredToRunBackgroundService() shutting down service in 5 seconds");
            }
        } else {
            mShuttingDownService = false;
            mServiceShutdownHandler.removeCallbacksAndMessages(mServiceShutdownRunnable);

            if(DEBUG) {
                Log.d(TAG, "checkIfRequiredToRunBackgroundService() continue running service");
            }
        }
    }


    private final Runnable mServiceShutdownRunnable = new Runnable(){

        @Override
        public void run(){
            if(mShuttingDownService) {
                /* End this service */
                stopSelf();
            } else {
                if(DEBUG) {
                    Log.i(TAG, TAG + " run() cancel service shutdown");
                }
            }
        }
    };

    private final Runnable mRouteLoggingShutdownRunnable = new Runnable(){

        @Override
        public void run(){
            if(mStoppingRouteLogging) {
                stopLoggingRoute();
            } else {
                if(DEBUG) {
                    Log.i(TAG, TAG + " run() cancel service shutdown");
                }
            }
        }
    };




    /* This class is responsible for initializing GoogleApiClient and ActivityRecognitionApi.
     * ActivityRecognitionApi can detect if the user is currently on foot, in a car, on a bicycle or still
     * by periodically waking up the device and reading short bursts of sensor data
     */
    private class GoogleApiDetectedActivityListener implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        private PendingIntent mActivityRecognizedPendIntent;
        private GoogleApiClient mGoogleApiClient;
        private boolean mDetectedActivityIsDriving = true;

        public GoogleApiDetectedActivityListener(Context c) {
            mGoogleApiClient = new GoogleApiClient.Builder(c)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();

            if(DEBUG) {
                Log.i(TAG, "GoogleApiDetectedActivityListener()");
            }

        }

        @Override
        public void onConnected(@Nullable Bundle connectionHint) {
            /* After calling connect(), this method will be invoked asynchronously
             * when the connect request has successfully completed.
             */
            Intent intent = new Intent(getApplicationContext(), GpsBackgroundService.class);
            mActivityRecognizedPendIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 5000, mActivityRecognizedPendIntent);

            if(DEBUG) {
                Log.i(TAG, TAG + " GoogleApiDetectedActivityListener onConnected()");
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            /* Called when the client is temporarily in a disconnected state. */

            if(cause == GoogleApiDetectedActivityListener.CAUSE_NETWORK_LOST) {
                /* A suspension cause informing you that a peer device connection was lost. */
            } else if(cause == GoogleApiDetectedActivityListener.CAUSE_SERVICE_DISCONNECTED) {
                /* A suspension cause informing that the service has been killed. */
            }
            if(mGpsRouteLogger != null) {
                mGpsRouteLogger.setDetectedActivityState(GpsRouteLogger.DETECTED_ACTIVITY_NOT_ENABLED);
            }

            if(DEBUG) {
                Log.i(TAG, TAG + " GoogleApiDetectedActivityListener onConnectionSuspended() cause: " + cause);
            }
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            /* Called when there was an error connecting the client to the service. */

            if(mGpsRouteLogger != null) {
                mGpsRouteLogger.setDetectedActivityState(GpsRouteLogger.DETECTED_ACTIVITY_NOT_ENABLED);
            }
            int result = connectionResult.getErrorCode();

            switch (result) {
                case ConnectionResult.API_UNAVAILABLE:
                    break;
            }

            if(DEBUG) {
                Log.i(TAG, TAG + " GoogleApiDetectedActivityListener onConnectionFailed()");
            }
        }

        /* @ActivityRecognitionApi: The activities are detected by periodically waking up the device and reading short bursts of sensor data.
         * It only makes use of low power sensors in order to keep the power usage to a minimum. For example,
         * it can detect if the user is currently on foot, in a car, on a bicycle or still
         */
        public void handleActivityRecognitionResult(Intent intent) {
            // Get the update
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            // Get the most probable activity from the list of activities in the update
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            // Get the confidence percentage for the most probable activity
            int confidence = mostProbableActivity.getConfidence();

            // Get the type of activity
            int activityType = mostProbableActivity.getType();

            if (confidence >= 50) {
                mDetectedActivityIsDriving = (activityType == DetectedActivity.IN_VEHICLE);
            } else {
                mDetectedActivityIsDriving = false;
            }
            if(mGpsRouteLogger != null) {
                /* Pass result to GPS logger */
                mGpsRouteLogger.setDetectedActivityState(mDetectedActivityIsDriving ?
                        GpsRouteLogger.DETECTED_ACTIVITY_DRIVING : GpsRouteLogger.DETECTED_ACTIVITY_WALKING);
            }
            if(DEBUG) {
                Log.i(TAG, TAG + " GoogleApiDetectedActivityListener handleActivityRecognitionResult()");
            }
        }

        public void stop() {
            if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, mActivityRecognizedPendIntent);
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
                if (mGpsRouteLogger != null) {
                    mGpsRouteLogger.setDetectedActivityState(GpsRouteLogger.DETECTED_ACTIVITY_NOT_ENABLED);
                }
            }
            if(DEBUG) {
                Log.i(TAG, TAG + " GoogleApiDetectedActivityListener stop()");
            }
        }
    }
}
