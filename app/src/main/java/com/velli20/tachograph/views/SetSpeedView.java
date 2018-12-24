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


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.velli20.tachograph.DateUtils;
import com.velli20.tachograph.R;

public class SetSpeedView extends RelativeLayout implements OnSeekBarChangeListener {
    public static final String UNIT_KM = " km/h";
    public static final String UNIT_MILES = " mph";
    public static final String UNIT_TIME = " time";

    private SeekBar mSeekBar;
    private RobotoLightTextView mLabel;
    private int mCurrentValue = 11;
    private int mMinValue = 1;
    private String mUnit = UNIT_KM;

    public SetSpeedView(Context context) {
        super(context);
    }

    public SetSpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SetSpeedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    public SetSpeedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        mSeekBar = (SeekBar) findViewById(R.id.dialog_seekbar);
        mSeekBar.setProgress(mCurrentValue);
        mSeekBar.setOnSeekBarChangeListener(this);

        mLabel = (RobotoLightTextView) findViewById(R.id.dialog_seekbar_label);
        mLabel.setText(String.valueOf(mCurrentValue));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (mMinValue > 0 && progress <= mMinValue) {
            seekBar.setProgress(mMinValue);
            setLabel(mMinValue);
        } else {
            setLabel(progress);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void setLabel(int value) {
        if (mUnit.equals(UNIT_TIME)) {
            mLabel.setText(DateUtils.convertMinutesToTimeString(value));
        } else {
            mLabel.setText(String.valueOf(value) + mUnit);
        }
    }

    public void setMax(int max) {
        mSeekBar.setMax(max);
    }

    public void setMin(int min) {
        mMinValue = min;
    }

    public void setUnit(String unit) {
        mUnit = unit;
    }

    public int getValue() {
        return mSeekBar.getProgress();
    }

    public void setValue(int value) {
        mCurrentValue = value;
        if (value < mMinValue) {
            mCurrentValue = mMinValue;
        } else {
            mCurrentValue = value;
        }
        if (mSeekBar != null) {
            mSeekBar.setProgress(mCurrentValue);
        }
    }

}
