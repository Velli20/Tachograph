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
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.velli20.tachograph.DateUtils;
import com.velli20.tachograph.R;
import com.velli20.tachograph.views.RobotoLightTextView;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class CustomTimePickerPreference extends CustomPreference {
    private static final int DEFAULT_VALUE = 300;
    private int mCurrentValue; // In secs

    public CustomTimePickerPreference(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public CustomTimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public CustomTimePickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
    }

    @TargetApi(LOLLIPOP)
    public CustomTimePickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onClick() {
        final int current = getPersistedInt(DEFAULT_VALUE);


        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mCurrentValue = (hourOfDay * 60 * 60) + (minute * 60);

                persistInt(mCurrentValue);
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
        }, (current / 60) / 60, (current / 60) % 60, true);
        timePickerDialog.show();


    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        setWidgetLayoutResource(R.layout.view_preference_widget_counter);
        return super.onCreateView(parent);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        int minutes;
        if (mCurrentValue != 0) {
            minutes = (mCurrentValue / 60);
        } else {
            minutes = 0;
        }
        RobotoLightTextView text = (RobotoLightTextView) view.findViewById(R.id.widget_counter);
        text.setText(DateUtils.convertMinutesToTimeString(minutes));
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

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        // Check whether this Preference is persistent (continually saved)
        if (isPersistent()) {
            // No need to save instance state since it's persistent,
            // use superclass state
            return superState;
        }

        // Create instance of custom BaseSavedState
        final SavedState myState = new SavedState(superState);
        // Set the state's value with the class member that holds current
        // setting value
        myState.value = mCurrentValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // Set this Preference's widget to reflect the restored state
        mCurrentValue = myState.value;
    }

    private static class SavedState extends BaseSavedState {
        int value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            value = source.readInt();  // Change this to read the appropriate data type
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeInt(value);  // Change this to write the appropriate data type
        }


    }

}
