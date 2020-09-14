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


public class GpsRouteLogger implements LocationListener, GoogleDetectedActivityStatus.OnGoogleDetectedActivityStatusChangedListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "GpsRouteLogger ";

    private CustomLocation mLastLocation;

    private Event mCurrentEvent;
    private EventRecorder mEventRecorderInstance = EventRecorder.INSTANCE;
    private GoogleDetectedActivityStatus mDetectedActivityInstance = GoogleDetectedActivityStatus.INSTANCE;
    private GpsRouteLoggerStatus mGpsRouteLoggerStatusInstance = GpsRouteLoggerStatus.INSTANCE;

    private boolean mStopped = false;
    private boolean mLocationManagerLocationUpdatesRequested = false;

    private long mTimeAtStop = -1;

    private float mGpsMinAccuracy = 20.0f;

    private int mThresholdTime; /* In seconds */
    private int mThresholdSpeed;
    private int mCurrentSpeed = -1;


    public GpsRouteLogger() {
    }

    private static float getSpeedFromLocations(Location start, Location end) {
        final long millis = (end.getTime() - start.getTime());
        long secs = millis <= 0 ? 0 : (millis / 1000);

        final float dist = start.distanceTo(end);

        if (dist <= 0 || secs <= 0) {
            return 0;
        } else {
            return (dist / secs) * 3.6f;
        }
    }

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

    public void setCurrentEvent(Event ev) {
        if (ev == null || ev.getEventType() != Event.EVENT_TYPE_DRIVING) {
            mEventRecorderInstance.cancelScheduledEvent();
        }
        mCurrentEvent = ev;
    }

    @Override
    public void onGoogleDetectedActivityStatusChanged(int newStatus) {
        monitorEvent(mCurrentSpeed);
    }

    @Override
    public void onLocationChanged(Location loc) {
        if (!mGpsRouteLoggerStatusInstance.isGpsFixAcquired()) {
            mGpsRouteLoggerStatusInstance.setGpsFixAcquired(true);
        }
        if (!isGpsAccurateEnough(loc)) {
            return;
        }

        final CustomLocation location = new CustomLocation(loc);
        location.setTime(loc.getTime());

        int speed = -1;

        if (mLastLocation != null) {
            speed = (int) getSpeedFromLocations(mLastLocation, location);
            mCurrentSpeed = speed;
            location.setSpeed(speed);
            mGpsRouteLoggerStatusInstance.setSpeed(speed);
        }

        monitorEvent(speed);

        if (mCurrentEvent != null && mCurrentEvent.getEventType() == Event.EVENT_TYPE_DRIVING) {
            /* Save this location */
            location.setEventId(mCurrentEvent.getRowId());
            saveLocation(location);
        }
        mLastLocation = location;
    }

    private void monitorEvent(int speed) {
        boolean useDetectedActivity = mDetectedActivityInstance.isDetectedActivityEnabled();
        boolean detectedDriving = mDetectedActivityInstance.isActivityDriving();
        boolean detectedActivityOtherWork = mDetectedActivityInstance.isActivityOtherWork();

        if ((speed != -1 && speed > mThresholdSpeed && (!useDetectedActivity || detectedDriving)) && isVehicleStopped()) {
            /* Check if there is scheduled event and cancel it */
            if (mEventRecorderInstance.isEventScheduled()) {
                mEventRecorderInstance.cancelScheduledEvent();
            }

            setVehicleStopped(false);
            if (mCurrentEvent != null && mCurrentEvent.getEventType() == Event.EVENT_TYPE_NORMAL_BREAK || mCurrentEvent.getEventType() == Event.EVENT_TYPE_OTHER_WORK) {
                /* Switch to driving event if current Event is Other Work */
                mEventRecorderInstance.endRecordingEvent(mCurrentEvent, System.currentTimeMillis());
                mEventRecorderInstance.startRecordingEvent(Event.EVENT_TYPE_DRIVING, System.currentTimeMillis());
            }

        } else if (((speed != -1 && speed <= mThresholdSpeed) || (useDetectedActivity && detectedActivityOtherWork)) && (!isVehicleStopped() || !mEventRecorderInstance.isEventScheduled())) {
            setVehicleStopped(true);
            if ((!mEventRecorderInstance.isEventScheduled() || mEventRecorderInstance.getScheduledEventType() != Event.EVENT_TYPE_NORMAL_BREAK)
                    && mCurrentEvent != null && mCurrentEvent.getEventType() == Event.EVENT_TYPE_DRIVING) {
                /* Schedule to switch current Event to Other Work */
                mEventRecorderInstance.scheduleEvent(Event.EVENT_TYPE_NORMAL_BREAK, mTimeAtStop, System.currentTimeMillis() + (mThresholdTime * 1000), mCurrentEvent);
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int status, Bundle bundle) {
        if (!LocationManager.GPS_PROVIDER.equals(s)) {
            return;
        }
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                mGpsRouteLoggerStatusInstance.setGpsFixAcquired(false);
                break;
            case LocationProvider.AVAILABLE:
                mGpsRouteLoggerStatusInstance.setGpsFixAcquired(true);
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                mGpsRouteLoggerStatusInstance.setGpsFixAcquired(false);
                break;
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        if (DEBUG) {
            Log.d(TAG, "onProviderEnabled() " + s);
        }
        if (s.equals(LocationManager.GPS_PROVIDER)) {
            mGpsRouteLoggerStatusInstance.setGpsProviderEnabled(true);
        }
    }

    @Override
    public void onProviderDisabled(String s) {
        if (DEBUG) {
            Log.d(TAG, "onProviderDisabled() " + s);
        }
        if (s.equals(LocationManager.GPS_PROVIDER)) {
            mGpsRouteLoggerStatusInstance.setGpsProviderEnabled(false);
            mEventRecorderInstance.cancelScheduledEvent();
        }
    }

    /* Save this location to the app database */
    private void saveLocation(CustomLocation location) {
        if (location != null) {
            DataBaseHandler.getInstance().addLocation(location);
        }
    }

    private boolean isGpsAccurateEnough(Location location) {
        if (location != null && location.getAccuracy() <= mGpsMinAccuracy) {
            mGpsRouteLoggerStatusInstance.setGpsFixAccurateEnough(true);
            mGpsRouteLoggerStatusInstance.setGpsFixAccuracy((int) location.getAccuracy());
            return true;
        } else if (location != null) {
            mGpsRouteLoggerStatusInstance.setGpsFixAccurateEnough(false);
            mGpsRouteLoggerStatusInstance.setGpsFixAccuracy((int) location.getAccuracy());
            return false;
        }
        return false;
    }


    private boolean isVehicleStopped() {
        return mStopped && mTimeAtStop != -1;
    }

    private void setVehicleStopped(boolean stopped) {
        if (stopped) {
            mStopped = true;
            mTimeAtStop = System.currentTimeMillis();
            if (DEBUG) {
                Log.d(TAG, TAG + "setVehicleStopped(true)");
            }

        } else {
            mStopped = false;
            mTimeAtStop = -1;
            if (DEBUG) {
                Log.d(TAG, TAG + "setVehicleStopped(false)");
            }
        }
        mGpsRouteLoggerStatusInstance.setVehicleStopped(mTimeAtStop, mStopped);
    }


    public void unregisterLocationListener(Context c) {
        mEventRecorderInstance.cancelScheduledEvent();

        if (!mLocationManagerLocationUpdatesRequested) {
            return;
        }
        setVehicleStopped(true);

        boolean permissionGranted;

        LocationManager locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            permissionGranted = ActivityCompat.checkSelfPermission(c, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(c, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            permissionGranted = true;
        }

        if (permissionGranted) {
            locationManager.removeUpdates(this);
            mLocationManagerLocationUpdatesRequested = false;
            mGpsRouteLoggerStatusInstance.setGpsProviderEnabled(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            mGpsRouteLoggerStatusInstance.setGpsFixAcquired(false);
        }
        if (DEBUG) {
            mDetectedActivityInstance.unregisterOnGoogleDetectedActivityStatusChangedListener(this);
        }
    }

    public void registerLocationListener(Context c) {
        if (mLocationManagerLocationUpdatesRequested) {
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
        if (permissionGranted) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) == 0) {
                mGpsRouteLoggerStatusInstance.setGpsProviderEnabled(false);
                mLocationManagerLocationUpdatesRequested = false;
            } else if (locationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
                mLocationManagerLocationUpdatesRequested = true;

                mGpsRouteLoggerStatusInstance.setGpsFixAcquired(false);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, this);
                mGpsRouteLoggerStatusInstance.setGpsProviderEnabled(true);
            } else if (DEBUG) {
                Log.d(TAG, TAG + "registerLocationListener() NO GPS PROVIDER");
            }
        } else {
            mLocationManagerLocationUpdatesRequested = false;
        }
        mGpsRouteLoggerStatusInstance.setGpsPermissionGranted(permissionGranted);
        if (DEBUG) {
            mDetectedActivityInstance.registerOnGoogleDetectedActivityStatusChangedListener(this);
        }
    }


}
