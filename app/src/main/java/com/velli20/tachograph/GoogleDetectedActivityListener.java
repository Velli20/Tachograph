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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/* This class is responsible for initializing GoogleApiClient and ActivityRecognitionApi.
 * ActivityRecognitionApi can detect if the user is currently on foot, in a car, on a bicycle or still
 * by periodically waking up the device and reading short bursts of sensor data
 */
public class GoogleDetectedActivityListener implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "DetectedActivity ";

    public static final int DETECTED_ACTIVITY_WALKING = 1;
    public static final int DETECTED_ACTIVITY_DRIVING = 2;
    public static final int DETECTED_ACTIVITY_STILL = 3;
    public static final int DETECTED_ACTIVITY_UNKNOWN = 4;


    private PendingIntent mActivityRecognizedPendIntent;
    private GoogleApiClient mGoogleApiClient;

    public GoogleDetectedActivityListener(Context c) {
        GoogleDetectedActivityStatus.INSTANCE.setDetectedActivityStatus(DETECTED_ACTIVITY_UNKNOWN);
        GoogleDetectedActivityStatus.INSTANCE.setDetectedActivityEnabled(false);


        mGoogleApiClient = new GoogleApiClient.Builder(c)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        if (DEBUG) {
            Log.d(TAG, "GoogleApiDetectedActivityListener()");
        }

    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        GoogleDetectedActivityStatus.INSTANCE.setDetectedActivityStatus(DETECTED_ACTIVITY_UNKNOWN);
        GoogleDetectedActivityStatus.INSTANCE.setDetectedActivityEnabled(true);

        Context context = App.get().getApplicationContext();

        /* After calling connect(), this method will be invoked asynchronously
         * when the connect request has successfully completed.
         */
        Intent intent = new Intent(context, GpsBackgroundService.class);
        mActivityRecognizedPendIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 5000, mActivityRecognizedPendIntent);

        if (DEBUG) {
            Log.i(TAG, TAG + " GoogleApiDetectedActivityListener onConnected()");
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        GoogleDetectedActivityStatus.INSTANCE.setDetectedActivityEnabled(false);

        /* Called when the client is temporarily in a disconnected state. */

        if (cause == CAUSE_NETWORK_LOST) {
            /* A suspension cause informing you that a peer device connection was lost. */
        } else if (cause == CAUSE_SERVICE_DISCONNECTED) {
            /* A suspension cause informing that the service has been killed. */
        }

        if (DEBUG) {
            Log.i(TAG, TAG + " GoogleApiDetectedActivityListener onConnectionSuspended() cause: " + cause);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        GoogleDetectedActivityStatus.INSTANCE.setDetectedActivityEnabled(false);

        /* Called when there was an error connecting the client to the service. */
        int result = connectionResult.getErrorCode();

        switch (result) {
            case ConnectionResult.API_UNAVAILABLE:
                break;
        }

        if (DEBUG) {
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
        if(!GoogleDetectedActivityStatus.INSTANCE.isDetectedActivityEnabled()) {
            GoogleDetectedActivityStatus.INSTANCE.setDetectedActivityEnabled(true);
        }

        if (confidence >= 50) {
            switch (activityType) {
                case DetectedActivity.IN_VEHICLE:
                    GoogleDetectedActivityStatus.INSTANCE.setDetectedActivityStatus(DETECTED_ACTIVITY_DRIVING);
                    break;
                case DetectedActivity.RUNNING:
                case DetectedActivity.ON_FOOT:
                    GoogleDetectedActivityStatus.INSTANCE.setDetectedActivityStatus(DETECTED_ACTIVITY_WALKING);
                    break;
                case DetectedActivity.STILL:
                case DetectedActivity.TILTING:
                    GoogleDetectedActivityStatus.INSTANCE.setDetectedActivityStatus(DETECTED_ACTIVITY_STILL);
                    break;
                default:
                    GoogleDetectedActivityStatus.INSTANCE.setDetectedActivityStatus(DETECTED_ACTIVITY_UNKNOWN);
                    break;
            }
        }

        if (DEBUG) {
            Log.i(TAG, TAG + " GoogleApiDetectedActivityListener handleActivityRecognitionResult()");
        }
    }

    public void stop() {
        GoogleDetectedActivityStatus.INSTANCE.setDetectedActivityStatus(DETECTED_ACTIVITY_UNKNOWN);
        GoogleDetectedActivityStatus.INSTANCE.setDetectedActivityEnabled(false);

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, mActivityRecognizedPendIntent);
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }

        }
        if (DEBUG) {
            Log.i(TAG, TAG + " GoogleApiDetectedActivityListener stop()");
        }
    }
}