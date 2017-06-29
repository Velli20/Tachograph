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


import java.lang.ref.WeakReference;

public enum GpsRouteLoggerStatus {
    INSTANCE;

    public static final int GPS_LOGGER_STATUS_GPS_NOT_ENABLED = 0;
    public static final int GPS_LOGGER_STATUS_ACQUIRING_GPS_FIX = 1;
    public static final int GPS_LOGGER_STATUS_VEHICLE_STOPPED = 2;
    public static final int GPS_LOGGER_STATUS_LOGGING_IN_PROGRESS = 3;
    public static final int GPS_LOGGER_STATUS_NO_PERMISSION = 4;
    public static final int GPS_LOGGER_STATUS_GPS_FIX_NOT_ACCURATE_ENOUGH = 5;


    private int mSpeed;
    private int mGpsFixAccuracy;

    private long mTimeAtVehicleStopped;
    private boolean mStopped;
    private boolean mGpsProviderEnabled;
    private boolean mGpsFixAcquired;
    private boolean mGpsAccurateEnough;
    private boolean mGpsPermissionGranted;


    private WeakReference<OnGpsLoggerStatusChangedListener> mListener;

    public void registerOnGpsLoggerStatusChangedListener(OnGpsLoggerStatusChangedListener l) {
        if (mListener != null && mListener.get() != null) {
            mListener.clear();
        }
        if (l != null) {
            mListener = new WeakReference<>(l);
        }
    }

    public void unregisterOnGpsLoggerStatusChangedListener(OnGpsLoggerStatusChangedListener l) {
        if (l != null && mListener != null && mListener.get() != null && mListener.get().equals(l)) {
            mListener.clear();
            mListener = null;
        }

    }

    public int getSpeed() {
        return mSpeed;
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
        notifyCallback();
    }

    public long getTimeAtVehicleStopped() {
        return mTimeAtVehicleStopped;
    }

    public boolean isGpsProviderEnabled() {
        return mGpsProviderEnabled;
    }

    public void setGpsProviderEnabled(boolean enabled) {
        mGpsProviderEnabled = enabled;
        notifyCallback();
    }

    public boolean isVehicleStopped() {
        return mStopped;
    }

    public boolean isGpsFixAcquired() {
        return mGpsFixAcquired;
    }

    public void setGpsFixAcquired(boolean acquired) {
        mGpsFixAcquired = acquired;
        notifyCallback();
    }

    public boolean isGpsFixAccurateEnough() {
        return mGpsAccurateEnough;
    }

    public void setGpsFixAccurateEnough(boolean accurateEnough) {
        mGpsAccurateEnough = accurateEnough;
        notifyCallback();
    }

    public int getGpsProviderAccuracy() {
        return mGpsFixAccuracy;
    }

    public void setVehicleStopped(long timeAtVehicleStopped, boolean stopped) {
        mTimeAtVehicleStopped = timeAtVehicleStopped;
        mStopped = stopped;
        notifyCallback();
    }

    public void setGpsFixAccuracy(int accuracy) {
        mGpsFixAccuracy = accuracy;
        notifyCallback();
    }

    public void setGpsPermissionGranted(boolean permissionGranted) {
        mGpsPermissionGranted = permissionGranted;
        notifyCallback();
    }

    public int getStatus() {
        if (!mGpsPermissionGranted) {
            return GPS_LOGGER_STATUS_NO_PERMISSION;
        } else if (!mGpsProviderEnabled) {
            return GPS_LOGGER_STATUS_GPS_NOT_ENABLED;
        } else if (!mGpsPermissionGranted) {
            return GPS_LOGGER_STATUS_NO_PERMISSION;
        } else if (!mGpsFixAcquired) {
            return GPS_LOGGER_STATUS_ACQUIRING_GPS_FIX;
        } else if (!mGpsAccurateEnough) {
            return GPS_LOGGER_STATUS_GPS_FIX_NOT_ACCURATE_ENOUGH;
        } else if (mStopped) {
            return GPS_LOGGER_STATUS_VEHICLE_STOPPED;
        }

        return GPS_LOGGER_STATUS_LOGGING_IN_PROGRESS;
    }

    private void notifyCallback() {
        if (mListener != null && mListener.get() != null) {
            mListener.get().onGpsLoggerStatusChanged();
        }
    }

    public interface OnGpsLoggerStatusChangedListener {
        void onGpsLoggerStatusChanged();
    }
}
