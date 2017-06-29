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

package com.velli20.tachograph.preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.velli20.tachograph.R;
import com.velli20.tachograph.views.RobotoLightTextView;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class GpsAccuracyPreference extends CustomPreference {
    private static final int DEFAULT_VALUE = 4;
    private int mCurrentValue;

    public GpsAccuracyPreference(Context context) {
        super(context);
    }

    public GpsAccuracyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GpsAccuracyPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(LOLLIPOP)
    public GpsAccuracyPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onClick() {
        final int current = getPersistedInt(5);


        new MaterialDialog.Builder(getContext())
                .items(getContext().getResources().getStringArray(R.array.preference_entries_gps_accuracy))
                .title(getTitle())
                .theme(Theme.DARK)
                .positiveText(getContext().getResources().getString(R.string.action_ok))
                .itemsCallbackSingleChoice(current, new MaterialDialog.ListCallbackSingleChoice() {

                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        setCurrentValue(which);
                        persistInt(mCurrentValue);
                        notifyDependencyChange(shouldDisableDependents());
                        notifyChanged();
                        return true;
                    }
                }).show();


    }

    public void setCurrentValue(int value) {
        mCurrentValue = value;
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        setWidgetLayoutResource(R.layout.view_preference_widget_counter);
        return super.onCreateView(parent);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        RobotoLightTextView text = (RobotoLightTextView) view.findViewById(R.id.widget_counter);
        text.setText(getContext().getResources().getStringArray(R.array.preference_entries_gps_accuracy)[mCurrentValue]);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_VALUE);
    }


    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            mCurrentValue = getPersistedInt(DEFAULT_VALUE);
        } else {
            // Set default state from the XML attribute
            mCurrentValue = (Integer) defaultValue;
            persistInt(mCurrentValue);
        }
    }


}
