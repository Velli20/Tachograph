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

package com.velli20.tachograph.views;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.velli20.tachograph.GoogleDetectedActivityListener;
import com.velli20.tachograph.GoogleDetectedActivityStatus;
import com.velli20.tachograph.GpsRouteLoggerStatus;
import com.velli20.tachograph.R;

public class ListItemGpsLogger extends CardView implements GpsRouteLoggerStatus.OnGpsLoggerStatusChangedListener, GoogleDetectedActivityStatus.OnGoogleDetectedActivityStatusChangedListener {
    private RobotoLightTextView mSpeed;
    private RobotoLightTextView mDetectedActivity;
    private AppCompatButton mEnableGpsProvider;
    private FrameLayout mActionButtonContainer;

    public ListItemGpsLogger(Context context) {
        super(context);
    }

    public ListItemGpsLogger(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public ListItemGpsLogger(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(!isInEditMode()) {
            GpsRouteLoggerStatus.INSTANCE.registerOnGpsLoggerStatusChangedListener(this);
            GoogleDetectedActivityStatus.INSTANCE.registerOnGoogleDetectedActivityStatusChangedListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(!isInEditMode()) {
            GpsRouteLoggerStatus.INSTANCE.unregisterOnGpsLoggerStatusChangedListener(null);
            GoogleDetectedActivityStatus.INSTANCE.unregisterOnGoogleDetectedActivityStatusChangedListener(this);
        }
    }


    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mSpeed = (RobotoLightTextView) findViewById(R.id.list_item_gps_logger_status_speed);
        mDetectedActivity = (RobotoLightTextView) findViewById(R.id.list_item_gps_logger_status_detected_activity);
        mEnableGpsProvider = (AppCompatButton) findViewById(R.id.list_item_gps_logger_status_button_enable_gps_provider);
        mActionButtonContainer = (FrameLayout) findViewById(R.id.list_item_gps_logger_status_action_button_container);
        update();
    }

    @Override
    public void onGpsLoggerStatusChanged() {
        update();
    }

    private void update() {
        if(isInEditMode() ||mSpeed == null || mDetectedActivity == null || mEnableGpsProvider == null) {
            return;
        }

        Resources res = getResources();
        GpsRouteLoggerStatus status = GpsRouteLoggerStatus.INSTANCE;

        switch (status.getStatus()) {
            case GpsRouteLoggerStatus.GPS_LOGGER_STATUS_GPS_NOT_ENABLED:
                displayGpsProviderNotEnabled();
                break;
            case GpsRouteLoggerStatus.GPS_LOGGER_STATUS_ACQUIRING_GPS_FIX:
                setGpsProvideButtonVisible(false);
                mSpeed.setText(res.getString(R.string.title_gps_logger_acquiring_gps_fix));
                break;
            case GpsRouteLoggerStatus.GPS_LOGGER_STATUS_VEHICLE_STOPPED:
                displayVehicleStopped();
                break;
            case GpsRouteLoggerStatus.GPS_LOGGER_STATUS_LOGGING_IN_PROGRESS:
                displayLoggingInProgress();
                break;
            case GpsRouteLoggerStatus.GPS_LOGGER_STATUS_GPS_FIX_NOT_ACCURATE_ENOUGH:
                mSpeed.setText(res.getString(R.string.title_gps_logger_signal_not_accurate_enough, status.getGpsProviderAccuracy()));
                break;
        }
        displayDetectedActivity();
    }

    private void displayDetectedActivity() {
        GoogleDetectedActivityStatus status = GoogleDetectedActivityStatus.INSTANCE;
        if(!status.isDetectedActivityEnabled()) {
            mDetectedActivity.setVisibility(View.GONE);
            return;
        } else {
            mDetectedActivity.setVisibility(View.VISIBLE);
        }
        Resources res = getResources();

        switch (status.getDetectedActivityStatus()) {
            case GoogleDetectedActivityListener.DETECTED_ACTIVITY_DRIVING:
                mDetectedActivity.setText(res.getString(R.string.title_detected_activity_driving));
                break;
            case GoogleDetectedActivityListener.DETECTED_ACTIVITY_WALKING:
                mDetectedActivity.setText(res.getString(R.string.title_detected_activity_walking));
                break;
            case GoogleDetectedActivityListener.DETECTED_ACTIVITY_STILL:
                mDetectedActivity.setText(res.getString(R.string.title_detected_activity_still));
                break;
            case GoogleDetectedActivityListener.DETECTED_ACTIVITY_UNKNOWN:
                mDetectedActivity.setText(res.getString(R.string.title_detected_activity_unknown));
                break;
        }
    }

    private void displayGpsProviderNotEnabled() {
        setGpsProvideButtonVisible(true);

        mSpeed.setText(getResources().getString(R.string.title_gps_logger_gps_provider_turned_disabled));
        mEnableGpsProvider.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
    }

    private void displayVehicleStopped() {
        setGpsProvideButtonVisible(false);
        GpsRouteLoggerStatus status = GpsRouteLoggerStatus.INSTANCE;
        Resources res = getResources();

        mSpeed.setText(res.getString(R.string.title_gps_logger_vehicle_stopped, status.getSpeed()));

    }

    private void displayLoggingInProgress() {
        setGpsProvideButtonVisible(false);
        GpsRouteLoggerStatus status = GpsRouteLoggerStatus.INSTANCE;

        mSpeed.setText(getResources().getString(R.string.title_gps_logger_logging_in_progress, status.getSpeed()));
    }

    private void setGpsProvideButtonVisible(boolean visible) {
        if(mActionButtonContainer.getVisibility() == (visible ? View.GONE : View.VISIBLE)) {
            mActionButtonContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onGoogleDetectedActivityStatusChanged(int newStatus) {
        update();
    }
}
