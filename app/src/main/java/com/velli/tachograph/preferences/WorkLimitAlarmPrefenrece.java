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

package com.velli.tachograph.preferences;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.velli.tachograph.DateCalculations;
import com.velli.tachograph.R;
import com.velli.tachograph.restingtimes.Constants;
import com.velli.tachograph.views.SetSpeedView;

public class WorkLimitAlarmPrefenrece extends CustomPreference {
	private static final int DEFAULT_VALUE = 10;
	private int mCurrentValue; // In minutes
	
	public WorkLimitAlarmPrefenrece(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public WorkLimitAlarmPrefenrece(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public WorkLimitAlarmPrefenrece(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}
	
	@TargetApi(LOLLIPOP)
	public WorkLimitAlarmPrefenrece(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onClick() {
		final SetSpeedView mSeekbar = (SetSpeedView)View.inflate(getContext(), R.layout.view_dialog_seekbar, null);
		int current = getPersistedInt(5);
		
		if(current == -1){
			current = DEFAULT_VALUE;
		}
		
		mSeekbar.setMax(getMaxInMinutes(getKey()));		
		mSeekbar.setMin(0);
		mSeekbar.setUnit(SetSpeedView.UNIT_TIME);
		mSeekbar.setValue(current);
		
		new MaterialDialog.Builder(getContext())
        .customView(mSeekbar, true)
        .theme(Theme.DARK)
        .title(R.string.dialog_title_alarm_time)
        .positiveText(R.string.action_ok)
        .negativeText(R.string.action_cancel)
        .neutralText(R.string.action_disble)
        .callback(new MaterialDialog.ButtonCallback() {
        	
        	@Override
        	public void onPositive(MaterialDialog dialog){
            	persistInt(mSeekbar.getValue());
            	notifyDependencyChange(shouldDisableDependents());
            	notifyChanged();
        	}
        	
        	@Override
        	public void onNeutral(MaterialDialog dialog){
            	persistInt(-1);
            	notifyDependencyChange(shouldDisableDependents());
            	notifyChanged();
        	}
		})
        .show();

	}
	

	
	public boolean isAlarmEnabled(){
		return getPersistedInt(DEFAULT_VALUE) > -1;
	}
	
	public void setSummary(int value, boolean enabled){
		if(enabled){
			setSummary(DateCalculations.convertMinutesToTimeString(value) + " " + getSummary(getKey()));
		} else {
			setSummary(R.string.preference_summary_alarm_disabled);
		}
	}
	
	public String getSummary(String key){
        final Resources res = getContext().getResources();
		
		if(key.equals(res.getString(R.string.preference_key_alarm_break_time_ending))){
			return res.getString(R.string.preference_summary_alarm_break_time_ending);
		} else if(key.equals(res.getString(R.string.preference_key_alarm_daily_rest_ending))){
			return res.getString(R.string.preference_summary_alarm_daily_rest_ending);
		} else if(key.equals(res.getString(R.string.preference_key_alarm_drive_time_ending))){
			return res.getString(R.string.preference_summary_alarm_drive_time_ending);
		} else if(key.equals(res.getString(R.string.preference_key_alarm_weekly_rest_ending))){
			return res.getString(R.string.preference_summary_alarm_weekly_rest_ending);
		}
		return res.getString(R.string.preference_summary_alarm_disabled);
	}
	
	public int getMaxInMinutes(String key){
		final Resources res = getContext().getResources();
		
		if(key.equals(res.getString(R.string.preference_key_alarm_break_time_ending))){
			return Constants.LIMIT_BREAK - 5;
		} else if(key.equals(res.getString(R.string.preference_key_alarm_daily_rest_ending))){
			return 240;
		} else if(key.equals(res.getString(R.string.preference_key_alarm_drive_time_ending))){
			return Constants.LIMIT_CONTINIOUS_DRIVING - 5;
		} else if(key.equals(res.getString(R.string.preference_key_alarm_weekly_rest_ending))){
			return 240;
		}
		return 10;
	}
	


	@Override
	protected View onCreateView(ViewGroup parent) {
		setSummary(getPersistedInt(10), isAlarmEnabled());
		
		return super.onCreateView(parent);
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

}

