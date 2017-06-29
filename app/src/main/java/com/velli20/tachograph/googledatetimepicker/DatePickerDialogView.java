/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.velli20.tachograph.googledatetimepicker;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.velli20.tachograph.R;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;


public class DatePickerDialogView extends LinearLayout implements DatePickerController {
    private static final String KEY_SELECTED_YEAR = "year";
    private static final String KEY_SELECTED_MONTH = "month";
    private static final String KEY_SELECTED_DAY = "day";
    private static final String KEY_LIST_POSITION = "list_position";
    private static final String KEY_WEEK_START = "week_start";
    private static final String KEY_YEAR_START = "year_start";
    private static final String KEY_YEAR_END = "year_end";

    private static final int DEFAULT_START_YEAR = 1970;
    private static final int DEFAULT_END_YEAR = 2100;
    private final Calendar mCalendar = Calendar.getInstance();
    private DayPickerView mDayPickerView;
    private AccessibleDateAnimator mAnimator;
    private Vibrator mVibrator;
    private long mLastVibrate;
    private int mWeekStart = mCalendar.getFirstDayOfWeek();
    private int mMinYear = DEFAULT_START_YEAR;
    private int mMaxYear = DEFAULT_END_YEAR;
    private int mListPosition = -1;

    private HashSet<OnDateChangedListener> mListeners = new HashSet<OnDateChangedListener>();


    public DatePickerDialogView(Context context) {
        super(context);
        init(context);
    }

    public DatePickerDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DatePickerDialogView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        final Bundle outState = new Bundle();
        final int listPosition = mDayPickerView.getMostVisiblePosition();

        outState.putInt(KEY_SELECTED_YEAR, mCalendar.get(Calendar.YEAR));
        outState.putInt(KEY_SELECTED_MONTH, mCalendar.get(Calendar.MONTH));
        outState.putInt(KEY_SELECTED_DAY, mCalendar.get(Calendar.DAY_OF_MONTH));
        outState.putInt(KEY_WEEK_START, mWeekStart);
        outState.putInt(KEY_YEAR_START, mMinYear);
        outState.putInt(KEY_YEAR_END, mMaxYear);
        outState.putInt(KEY_LIST_POSITION, listPosition);

        return outState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;

            mWeekStart = bundle.getInt(KEY_WEEK_START);
            mMinYear = bundle.getInt(KEY_YEAR_START);
            mMaxYear = bundle.getInt(KEY_YEAR_END);
            mListPosition = bundle.getInt(KEY_LIST_POSITION);

            if (mDayPickerView != null) {
                mDayPickerView.postSetSelection(mListPosition);
                mDayPickerView.onChange();
            }
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        mDayPickerView = new DayPickerView(getContext(), this);

        mAnimator = (AccessibleDateAnimator) findViewById(R.id.animator);
        mAnimator.addView(mDayPickerView);
        mAnimator.setDateMillis(mCalendar.getTimeInMillis());

        if (mListPosition != -1) {
            mDayPickerView.postSetSelection(mListPosition);
        }

    }

    private void adjustDayInMonthIfNeeded(int month, int year) {
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = Utils.getDaysInMonth(month, year);
        if (day > daysInMonth) {
            mCalendar.set(Calendar.DAY_OF_MONTH, daysInMonth);
        }
    }

    public void setDateInMillis(long date) {
        mCalendar.setTimeInMillis(date);
        if (mDayPickerView != null) {
            mDayPickerView.onChange();
            mDayPickerView.onDateChanged();
        }
    }

    public long getTimeInMillis() {
        return mCalendar.getTimeInMillis();
    }

    @Override
    public void onYearSelected(int year) {
        adjustDayInMonthIfNeeded(mCalendar.get(Calendar.MONTH), year);
        mCalendar.set(Calendar.YEAR, year);
        updatePickers();
    }

    @Override
    public void onDayOfMonthSelected(int year, int month, int day) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        updatePickers();
    }

    private void updatePickers() {
        Iterator<OnDateChangedListener> iterator = mListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().onDateChanged();
        }
    }

    @Override
    public void registerOnDateChangedListener(OnDateChangedListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void unregisterOnDateChangedListener(OnDateChangedListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public SimpleMonthAdapter.CalendarDay getSelectedDay() {
        return new SimpleMonthAdapter.CalendarDay(mCalendar);
    }

    @Override
    public int getFirstDayOfWeek() {
        return mWeekStart;
    }

    public void setFirstDayOfWeek(int startOfWeek) {
        if (startOfWeek < Calendar.SUNDAY || startOfWeek > Calendar.SATURDAY) {
            throw new IllegalArgumentException("Value must be between Calendar.SUNDAY and " + "Calendar.SATURDAY");
        }
        mWeekStart = startOfWeek;
        if (mDayPickerView != null) {
            mDayPickerView.onChange();
            mDayPickerView.onDateChanged();
        }
    }

    @Override
    public int getMinYear() {
        return mMinYear;
    }

    @Override
    public int getMaxYear() {
        return mMaxYear;
    }

    @Override
    public void tryVibrate() {
        if (mVibrator != null) {
            long now = SystemClock.uptimeMillis();
            if (now - mLastVibrate >= 125) {
                mVibrator.vibrate(5);
                mLastVibrate = now;
            }
        }
    }

}
