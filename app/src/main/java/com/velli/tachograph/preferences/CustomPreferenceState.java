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

import static android.text.TextUtils.isEmpty;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

public class CustomPreferenceState extends CustomPreference {
	private CharSequence mSummaryOn;
	private CharSequence mSummaryOff;
	private boolean mIsChecked;
	private boolean mIsCheckedSet;
	private boolean mDisableDependentsState;
	
	public CustomPreferenceState(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public CustomPreferenceState(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public CustomPreferenceState(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public CustomPreferenceState(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onClick() {
		super.onClick();
		boolean newValue = !isChecked();
		if (callChangeListener(newValue)) {
			setChecked(newValue);
		}
	}
	
	public void setChecked(boolean checked) {
		// Always persist/notify the first time; don't assume the field's
		// default of false.
		boolean changed = mIsChecked != checked;
		if (changed || !mIsCheckedSet) {
			mIsChecked = checked;
			mIsCheckedSet = true;
			persistBoolean(checked);
			if (changed) {
				notifyDependencyChange(shouldDisableDependents());
				notifyChanged();
			}
		}
	}
	
	public boolean isChecked() {
		return mIsChecked;
	}
	
	@Override
	public boolean shouldDisableDependents() {
		boolean shouldDisable = mDisableDependentsState ? mIsChecked : !mIsChecked;
		return shouldDisable || super.shouldDisableDependents();
	}
	
	public void setSummaryOn(String sum){
		mSummaryOn = sum;
		if (isChecked()) {
			notifyChanged();
		}
	}
	
	public void setSummaryOn(int summaryResId) {
		setSummaryOn(getContext().getString(summaryResId));
	}
	
	public CharSequence getSummaryOn() {
		return mSummaryOn;
	}
	
	public void setSummaryOff(CharSequence sum) {
		mSummaryOff = sum;
		if (!isChecked()) {
			notifyChanged();
		}
	}
	
	public void setSummaryOff(int summaryResId) {
		setSummaryOff(getContext().getString(summaryResId));
	}
	
	public CharSequence getSummaryOff() {
		return mSummaryOff;
	}
	
	public boolean getDisableDependentsState() {
		return mDisableDependentsState;
	}
	
	
	public void setDisableDependentsState(boolean disableDependentsState) {
		mDisableDependentsState = disableDependentsState;
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getBoolean(index, false);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		setChecked(restoreValue ? getPersistedBoolean(mIsChecked) : (Boolean) defaultValue);
	}
	
	public void syncSummaryView() {
		// Sync the summary view
		boolean useDefaultSummary = true;
		if (mIsChecked && !isEmpty(mSummaryOn)) {
			mSummary.setText(mSummaryOn);
			useDefaultSummary = false;
		} else if (!mIsChecked && !isEmpty(mSummaryOff)) {
			mSummary.setText(mSummaryOff);
			useDefaultSummary = false;
		}
		if (useDefaultSummary) {
			CharSequence summary = getSummary();
			if (!isEmpty(summary)) {
				mSummary.setText(summary);
				useDefaultSummary = false;
			}
		}
		int newVisibility = View.GONE;
		if (!useDefaultSummary) {
			// Someone has written to it
			newVisibility = View.VISIBLE;
		}
		if (newVisibility != mSummary.getVisibility()) {
			mSummary.setVisibility(newVisibility);
		}
	}
    
   
	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// No need to save instance state since it's persistent
			return superState;
		}
		SavedState myState = new SavedState(superState);
		myState.checked = isChecked();
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}
		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		setChecked(myState.checked);
	}

	static class SavedState extends BaseSavedState {
		boolean checked;

		public SavedState(Parcel source) {
			super(source);
			checked = source.readInt() == 1;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(checked ? 1 : 0);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

}
