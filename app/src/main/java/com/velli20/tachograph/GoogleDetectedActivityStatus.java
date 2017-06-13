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
import java.util.ArrayList;

public enum GoogleDetectedActivityStatus {
    INSTANCE;
    private boolean mIsDetectedActivityEnabled;

    public interface OnGoogleDetectedActivityStatusChangedListener {
        void onGoogleDetectedActivityStatusChanged(int newStatus);
    }

    private int mDetectedActivityStatus = GoogleDetectedActivityListener.DETECTED_ACTIVITY_UNKNOWN;
    private ArrayList<WeakReference<OnGoogleDetectedActivityStatusChangedListener>> mCallbacks = new ArrayList<>();

    public void setDetectedActivityStatus(int status) {
        mDetectedActivityStatus = status;
        notifyCallbacks();
    }

    public void setDetectedActivityEnabled(boolean enabled) {
        mIsDetectedActivityEnabled = enabled;
        notifyCallbacks();
    }

    public int getDetectedActivityStatus() { return mDetectedActivityStatus; }

    public boolean isDetectedActivityEnabled() { return mIsDetectedActivityEnabled; }

    public boolean isActivityDriving() {
        return mIsDetectedActivityEnabled && mDetectedActivityStatus == GoogleDetectedActivityListener.DETECTED_ACTIVITY_DRIVING;
    }

    public boolean isActivityOtherWork() {
        return mIsDetectedActivityEnabled && mDetectedActivityStatus != GoogleDetectedActivityListener.DETECTED_ACTIVITY_DRIVING
                && mDetectedActivityStatus != GoogleDetectedActivityListener.DETECTED_ACTIVITY_UNKNOWN;
    }

    public void registerOnGoogleDetectedActivityStatusChangedListener(OnGoogleDetectedActivityStatusChangedListener l) {
        if(l == null) {
            return;
        }
        mCallbacks.add(new WeakReference<>(l));
    }

    public void unregisterOnGoogleDetectedActivityStatusChangedListener(OnGoogleDetectedActivityStatusChangedListener l) {
        if(l == null) {
            return;
        }
        for(WeakReference<OnGoogleDetectedActivityStatusChangedListener> callback : mCallbacks) {
            if (callback != null && callback.get() != null && callback.get().equals(l)) {
                callback.clear();
                mCallbacks.remove(callback);
                break;
            }
        }
    }

    private void notifyCallbacks() {
        for(WeakReference<OnGoogleDetectedActivityStatusChangedListener> callback : mCallbacks) {
            if(callback != null && callback.get() != null) {
                callback.get().onGoogleDetectedActivityStatusChanged(mDetectedActivityStatus);
            }
        }
    }
}
