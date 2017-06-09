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

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.velli20.tachograph.database.DataBaseHandler;
import com.velli20.tachograph.location.CustomLocation;


public class GpsRouteLogger implements LocationListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "GpsRouteLogger ";

    public static final int DETECTED_ACTIVITY_NOT_ENABLED = 0;
    public static final int DETECTED_ACTIVITY_DRIVING = 1;
    public static final int DETECTED_ACTIVITY_WALKING = 2;

    private CustomLocation mLastSavedLocation;
    private CustomLocation mLastLocation;

    private Event mCurrentEvent;

    private boolean mStopped = false;
    private boolean mLocationManagerLocationUpdatesRequested = false;

    private long mTimeAtStop = -1;

    private float mGpsMinAccuracy = 20.0f;

    private int mThresholdTime; /* In seconds */
    private int mThresholdSpeed;
    private int mDetectedActivityState = DETECTED_ACTIVITY_NOT_ENABLED;


    public GpsRouteLogger() { }

    /* Set min accuracy of GPS signal. Signals with accuracy above
     * this limit will be ignored.
     */
    public void setGpsMinAccuracy(float accuracy) {
        mGpsMinAccuracy = accuracy;
    }

    /* If user is stopped for longer period of time than this limit,
     * then switch to Other Work
     */
    public void setStoppedTimeThreshold(int time) {
        mThresholdTime = time;
    }

    /* This value is used to assume that vehicle is stopped when speed
     * drops below this limit
     */
    public void setStoppedSpeedThreshold(int speed) {
        mThresholdSpeed = speed;
    }


    /* Set state of the Google API DetectedActivity */
    public void setDetectedActivityState(int state) {
        if(DEBUG) {
            switch (state) {
                case DETECTED_ACTIVITY_DRIVING:
                    Log.d(TAG, "setDetectedActivityState() state: Driving");
                    break;
                case DETECTED_ACTIVITY_WALKING:
                    Log.d(TAG, "setDetectedActivityState() state: Walking");
                    break;
                case DETECTED_ACTIVITY_NOT_ENABLED:
                    Log.d(TAG, "setDetectedActivityState() state: Not enabled");
                    break;
            }
        }
        mDetectedActivityState = state;
    }

    public void setCurrentEvent(Event ev) {
        mCurrentEvent = ev;
    }


    @Override
    public void onLocationChanged(Location loc) {
        if(!isGpsAccurateEnough(loc)){
            if(DEBUG){
                Log.i(TAG, TAG + "GPS not accurate enough, accuracy: " + loc.getAccuracy());
            }
            return;
        }

        final CustomLocation location = new CustomLocation(loc);
        location.setTime(loc.getTime());

        int speed = -1;

        if (mLastLocation != null) {
            speed = (int) getSpeedFromLocations(mLastLocation, location);
            location.setSpeed(speed);
        }

        if((speed == 0 || mDetectedActivityState == DETECTED_ACTIVITY_WALKING) && !isVehicleStopped()) {
            setVehicleStopped(true);

            if(DEBUG) {
                Log.d(TAG, "onLocationChanged() vehicle stopped");
            }
        } else if(speed > 0 && mDetectedActivityState != DETECTED_ACTIVITY_WALKING && isVehicleStopped()) {
            setVehicleStopped(false);
            if(DEBUG) {
                Log.d(TAG, "onLocationChanged() vehicle moving at speed of " + String.valueOf(speed) + " km/h");
            }
        }

        if(checkIfRequiredToSwitchToOtherWork()) {
            /* User has started manual labor. Stop recording current event and start new */
            // End time of current event (Driving Event) = mTimeAtStop
            // Start time of the Other Work = mTimeAtStop
            EventRecorder handler = new EventRecorder();

            handler.endRecordingEvent(mCurrentEvent, mTimeAtStop);
            handler.startRecordingEvent(Event.EVENT_TYPE_OTHER_WORK, mTimeAtStop);

            setVehicleStopped(true);
        } else if(checkIfRequiredToSwitchToDriving(speed)) {
            /* End current event and start Driving event */
            EventRecorder handler = new EventRecorder();

            handler.endRecordingEvent(mCurrentEvent, System.currentTimeMillis());
            handler.startRecordingEvent(Event.EVENT_TYPE_DRIVING, System.currentTimeMillis());

            setVehicleStopped(false);
        }

        if(mCurrentEvent != null && mCurrentEvent.getEventType() == Event.EVENT_TYPE_DRIVING){

            /* Save this location */
            location.setEventId(mCurrentEvent.getRowId());
            saveLocation(location);
        }
        mLastLocation = location;
    }

    @Override
    public void onStatusChanged(String s, int status, Bundle bundle) {
        if(DEBUG) {
            switch(status) {
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d(TAG, "onStatusChanged() status: OUT_OF_SERVICE " + s);
                    break;
                case LocationProvider.AVAILABLE:
                    Log.d(TAG, "onStatusChanged() status: AVAILABLE " + s);
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d(TAG, "onStatusChanged() status: TEMPORARILY_UNAVAILABLE " + s);
                    break;
            }
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        if(DEBUG) {
            Log.d(TAG, "onProviderEnabled() " + s);
        }
    }

    @Override
    public void onProviderDisabled(String s) {
        if(DEBUG) {
            Log.d(TAG, "onProviderDisabled() " + s);
        }
    }

    /* Save this location to the app database */
    private void saveLocation(CustomLocation location) {
        if(mLastSavedLocation != null && location != null) {
            /* Location update frequency is higher than 2 s the skip */
            if((location.getTime() - mLastSavedLocation.getTime()) < 2000) {
                return;
            }
        }
        mLastSavedLocation = location;

        if (location != null) {
            DataBaseHandler.getInstance().addLocation(location);
        }
    }

    private static float getSpeedFromLocations(Location start, Location end){
        final long millis = (end.getTime() - start.getTime());
        long secs = millis <= 0 ? 0 : (millis / 1000);


        final float dist = start.distanceTo(end);

        if(dist <= 0 || secs <= 0){
            return 0;
        } else {
            return (dist / secs) * 3.6f;
        }
    }

    private boolean isGpsAccurateEnough(Location location){
        if(location != null && location.getAccuracy() <= mGpsMinAccuracy){
            return true;
        } else {
            return false;
        }
    }


    private boolean isVehicleStopped() {
        return mStopped && mTimeAtStop != -1;
    }

    private void setVehicleStopped(boolean stopped) {
        if(!isVehicleStopped() && stopped) {
            mStopped = true;
            mTimeAtStop = System.currentTimeMillis();
            if(DEBUG) {
                Log.d(TAG, TAG + "setVehicleStopped(true)");
            }
        } else {
            mStopped = false;
            mTimeAtStop = -1;
            if(DEBUG) {
                Log.d(TAG, TAG + "setVehicleStopped(false)");
            }
        }
    }

    /* Check if we need to change current event type to Other Work -event
     * from Driving -event.
     */
    private boolean checkIfRequiredToSwitchToOtherWork() {
        if(mCurrentEvent == null) {
            return false;
        }
        if(mCurrentEvent.getEventType() == Event.EVENT_TYPE_DRIVING && mStopped) {
            if(mTimeAtStop < 0) {
                /* Time when vehicle was stopped has not been set */
                return false;
            }
            long deltaTime = System.currentTimeMillis() - mTimeAtStop;

            return deltaTime >= (mThresholdTime * 1000);

        }

        return false;
    }

    /* Check if it is required to switch from ongoing event to Driving -event */
    private boolean checkIfRequiredToSwitchToDriving(int vehicleSpeed) {
        if(mCurrentEvent == null) {
            return false;
        }

        if(mCurrentEvent.getEventType() == Event.EVENT_TYPE_OTHER_WORK
                && vehicleSpeed >= mThresholdSpeed
                && (mDetectedActivityState == DETECTED_ACTIVITY_NOT_ENABLED || mDetectedActivityState == DETECTED_ACTIVITY_DRIVING)) {

            return true;
        }
        return false;
    }


    public void unregisterLocationListener(Context c) {
        if(!mLocationManagerLocationUpdatesRequested) {
            return;
        }
        setVehicleStopped(true);

        boolean permissionGranted;

        LocationManager locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            permissionGranted = ActivityCompat.checkSelfPermission(c, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(c, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            permissionGranted = true;
        }

        if(permissionGranted) {
            locationManager.removeUpdates(this);
            mLocationManagerLocationUpdatesRequested = false;
        }
    }

    public void registerLocationListener(Context c) {
        if(mLocationManagerLocationUpdatesRequested) {
            return;
        }
        LocationManager locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        boolean permissionGranted;

        /* Check permission */
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            permissionGranted = ActivityCompat.checkSelfPermission(c, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(c, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        } else {
            permissionGranted = true;
        }
        if(permissionGranted) {
            if (locationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, this);
                mLocationManagerLocationUpdatesRequested = true;
            } else if(DEBUG) {
                Log.d(TAG, TAG + "registerLocationListener() NO GPS PROVIDER");
            }
        }
    }





}
