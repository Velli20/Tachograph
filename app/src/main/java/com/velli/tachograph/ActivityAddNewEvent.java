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

package com.velli.tachograph;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.velli.tachograph.database.DataBaseHandler;

import com.velli.tachograph.database.DataBaseHandler.OnGetEventTaskCompleted;
import com.velli.tachograph.views.ChooseValueView;
import com.velli.tachograph.views.ListCircle;
import com.velli.tachograph.views.RobotoSwitchCompat;
import com.velli.tachograph.views.TimePickerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;

import pickers.android.datetimepicker.date.DatePickerDialogView;


public class ActivityAddNewEvent extends AppCompatActivity implements OnClickListener, OnItemSelectedListener, OnCheckedChangeListener {
	
    public static final int DISPLAY_MODE_CREATE_NEW = 0;
    public static final int DISPLAY_MODE_EDIT_OR_DELETE = 1;
    public static final String INTENT_EXTRA_DISPLAY_MODE = "display mode";
    public static final String INTENT_EXTRA_EVENT_ROW_ID = "event row id";
	public static final String INTENT_EXTRA_START_TIME= "event start time";
    public static final String INTENT_EXTRA_END_TIME= "event end time";
    public static final String INTENT_EXTRA_EVENT_TYPE = "event type";

    private Event mEvent;
    private int mDisplayMode = 0;
    private int mEventRowId = -1;
    private int mCurrentBackgroundColor;
    private int[] mColors;
    
    
    private Spinner mEventSpinner;
   
    private Button mStartTime;
    private Button mStartDate;
    private Button mEndTime;
    private Button mEndDate;
    private Button mMileageStart;
    private Button mMileageEnd;
    private RobotoSwitchCompat mIsSplitBreak;
 
    private EditText mNote;
    private EditText mStartLocation;
    private EditText mEndLocation;
    
    private Toolbar mToolbar;
    private View mRevealView;
    private View mRevealBackgroundView;
    private View mStatusRevealView;
    private View mStatusRevealBackgroundView;
    
    private RelativeLayout mEndTimeContainer;
    private RelativeLayout mEndDateContainer;
    private LinearLayout mExtraInfoContainer;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_add_new_event);
    	final Bundle bundle = getIntent().getExtras();
    	
    	mColors = getResources().getIntArray(R.array.event_colors);
    	mCurrentBackgroundColor = getResources().getColor(R.color.event_drving);
    	
    	mToolbar = (Toolbar)findViewById(R.id.toolbar_add_new_event);
    	mRevealView = findViewById(R.id.reveal);
        mRevealBackgroundView = findViewById(R.id.revealBackground);
        mStatusRevealView = findViewById(R.id.status_reveal);
        mStatusRevealBackgroundView = findViewById(R.id.status_revealBackground);
        
        mEventSpinner = (Spinner)findViewById(R.id.add_new_event_event_spinner);
        mStartTime = (Button)findViewById(R.id.add_new_event_button_start_time);
        mStartDate = (Button)findViewById(R.id.add_new_event_button_start_date);
        mEndTime = (Button)findViewById(R.id.add_new_event_button_end_time);
        mEndDate = (Button)findViewById(R.id.add_new_event_button_end_date);
        mMileageStart = (Button)findViewById(R.id.add_new_event_button_mileage_start);
        mMileageEnd = (Button)findViewById(R.id.add_new_event_button_mileage_end);
        mIsSplitBreak = (RobotoSwitchCompat)findViewById(R.id.add_new_event_event_is_split_break);
        mNote = (EditText)findViewById(R.id.add_new_event_note);
        mStartLocation = (EditText)findViewById(R.id.add_new_event_start_location);
        mEndLocation = (EditText)findViewById(R.id.add_new_event_end_location);
        
        mIsSplitBreak.setOnCheckedChangeListener(this);
        
        mEndTimeContainer = (RelativeLayout)findViewById(R.id.add_new_event_end_time_container);
        mEndDateContainer = (RelativeLayout)findViewById(R.id.add_new_event_end_date_container);
        mExtraInfoContainer = (LinearLayout)findViewById(R.id.add_new_event_extra_info);
        
        final Button[] mButtons = {mStartTime, mStartDate, mEndTime, mEndDate, mMileageStart, mMileageEnd};
        int length = mButtons.length;

        for(int i = 0; i < length; i++){
        	mButtons[i].setOnClickListener(this);
        }
        
        mEventSpinner.setAdapter(new EventSpinnerAdapter(this));
        mEventSpinner.setOnItemSelectedListener(this);
        setSupportActionBar(mToolbar);

        long start = -1;
        long end = -1;
        int eventType = 0;

    	if(bundle != null){
    		mDisplayMode = bundle.getInt(INTENT_EXTRA_DISPLAY_MODE, 0);
    		mEventRowId = bundle.getInt(INTENT_EXTRA_EVENT_ROW_ID, -1);
            start = bundle.getLong(INTENT_EXTRA_START_TIME, -1);
            end = bundle.getLong(INTENT_EXTRA_END_TIME, -1);
            eventType = bundle.getInt(INTENT_EXTRA_EVENT_TYPE, 0);
    	}
   
    	final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        
    	if(mDisplayMode == DISPLAY_MODE_EDIT_OR_DELETE){
    		ab.setTitle(R.string.title_edit_log_event);
    	} else {
    		ab.setTitle(R.string.title_add_new_log_event);
    	}
    	
    	initEvent(start, end, eventType, mEventRowId);
    }

    private void initEvent(long start, long end, int eventType, int rowId){
    	if(mDisplayMode == DISPLAY_MODE_EDIT_OR_DELETE && rowId != -1){
    		DataBaseHandler.getInstance().getEvent(rowId, new OnGetEventTaskCompleted() {
				
				@Override
				public void onGetEvent(Event ev) {
					mEvent = ev;
					setData(mEvent);
				}
			}, false);
    	} else {
            if(start == -1 && end == -1) {
                start = System.currentTimeMillis();
                end = System.currentTimeMillis();
            }
    		mEvent = new Event();
            mEvent.setEventType(eventType);
    		mEvent.setEndDate(end);
    		mEvent.setStartDate(start);
    		mEvent.setStartTime(DateCalculations.getCurrentHour(start), DateCalculations.getCurrentMinute(start));
    		mEvent.setEndTime(DateCalculations.getCurrentHour(end), DateCalculations.getCurrentMinute(end));
    		setData(mEvent);
    	}
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		menu.findItem(R.id.menu_item_discard).setVisible(mDisplayMode == DISPLAY_MODE_EDIT_OR_DELETE);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.menu_activity_add_new_event, menu);
		return super.onCreateOptionsMenu(menu);
	}
	 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_item_discard:
			DataBaseHandler.getInstance().deleteEvent(mEventRowId);
			
			final Intent data = new Intent();
			data.putExtra(FragmentLog.KEY_DELETED_ROW_ID, mEventRowId);
			setResult(Activity.RESULT_OK, data);
			
			finish();
			return true;
		case R.id.menu_item_save:
			mEvent.setNote(mNote.getText().toString());
			mEvent.setStartLocation(mStartLocation.getText().toString());
			mEvent.setEndLocation(mEndLocation.getText().toString());
			mEvent.setEndDate(DateCalculations.setTimeToDate(mEvent.getEndDateInMillis(), mEvent.getEndHour(), mEvent.getEndMinutes()));
			mEvent.setStartDate(DateCalculations.setTimeToDate(mEvent.getStartDateInMillis(), mEvent.getStartHour(), mEvent.getStartMinutes()));
			
			if(mEvent.getEventType() == Event.EVENT_TYPE_NORMAL_BREAK){
				mEvent.setAsSplitBreak(mIsSplitBreak.isChecked());
			} else {
				mEvent.setAsSplitBreak(false);
			}
			checkForErrorsAndSave();
			return true;
		case android.R.id.home:
			finish();
			return true;
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.add_new_event_button_start_time:
			setTime(true);
			break;
		case R.id.add_new_event_button_start_date:
			setDate(true);
			break;
		case R.id.add_new_event_button_end_time:
			setTime(false);
			break;
		case R.id.add_new_event_button_end_date:
			setDate(false);
			break;
		case R.id.add_new_event_button_mileage_start:
			setMileage(true);
			break;
		case R.id.add_new_event_button_mileage_end:
			setMileage(false);
			break;
		}
		
	}
	
	private void setData(Event ev){
		if(ev == null){
			return;
		}
		setToolbarColor(mColors[ev.getEventType()]);
		mEventSpinner.setSelection(ev.getEventType());
		mStartTime.setText(DateCalculations.createTimeString(ev.getStartHour(), ev.getStartMinutes()));
		mStartDate.setText(DateCalculations.createDateString(ev.getStartDateInMillis()));
		mEndTime.setText(DateCalculations.createTimeString(ev.getEndHour(), ev.getEndMinutes()));
		mEndDate.setText(DateCalculations.createDateString(ev.getEndDateInMillis()));
		mMileageStart.setText(String.format(Locale.getDefault(), "%d km", ev.getMileageStart()));
		mMileageEnd.setText(String.format(Locale.getDefault(), "%d km", ev.getMileageEnd()));
		mNote.setText(ev.getNote());
		mStartLocation.setText(ev.getStartLocation());
		mEndLocation.setText(ev.getEndLocation());
		mIsSplitBreak.setChecked(ev.isSplitBreak());
		
		if(ev.isRecordingEvent()){
			mEndTimeContainer.setVisibility(View.GONE);
			mEndDateContainer.setVisibility(View.GONE);
		}
		
		if(ev.getEventType() == Event.EVENT_TYPE_DRIVING){
			mExtraInfoContainer.setVisibility(View.VISIBLE);
		} else {
			mExtraInfoContainer.setVisibility(View.GONE);
		}
	}
	
	

	private void showErrorDialog(String error) {
		new MaterialDialog.Builder(ActivityAddNewEvent.this)
		        .title(R.string.dialog_title_fix_following_errors)
				.positiveText(R.string.action_ok)
				.positiveColor(mCurrentBackgroundColor)
				.content(error)
				.callback(new MaterialDialog.ButtonCallback() {

					@Override
					public void onPositive(MaterialDialog dialog) {

					}

				}).show();
	}
	
	private void checkForErrorsAndSave() {
		if(mEvent.getEndDateInMillis() < mEvent.getStartDateInMillis()){
			showErrorDialog(getResources().getString(R.string.dialog_text_error_start_end_time));
			
		} else {
            if(mDisplayMode == DISPLAY_MODE_CREATE_NEW){
                DataBaseHandler.getInstance().addNewEvent(mEvent);
            } else {
                DataBaseHandler.getInstance().updateEvent(mEvent, true);
            }
            finish();
        }

	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(mEvent != null && mEvent.getEventType() == Event.EVENT_TYPE_NORMAL_BREAK){
			mEvent.setAsSplitBreak(mIsSplitBreak.isChecked());
		}
		
	}

	private void setTime(final boolean startTime) {
		final TimePickerView timeView = (TimePickerView) View.inflate(
				ActivityAddNewEvent.this, R.layout.time_picker_dialog_layout,
				null);
		final MaterialDialog dialog = new MaterialDialog.Builder(ActivityAddNewEvent.this)
		        .customView(timeView, true)
				.positiveText(R.string.action_ok)
				.negativeText(android.R.string.cancel)
				.positiveColor(mCurrentBackgroundColor)
		        .negativeColor(mCurrentBackgroundColor)
				.callback(new MaterialDialog.ButtonCallback() {

					@Override
					public void onPositive(MaterialDialog dialog) {
						if (startTime) {
							mEvent.setStartTime(timeView.getHours(), timeView.getMinutes());
						} else {
							mEvent.setEndTime(timeView.getHours(), timeView.getMinutes());
						}
						setData(mEvent);
					}

				}).build();
		timeView.setPositiveButton(dialog.getActionButton(DialogAction.POSITIVE));
		timeView.setTime(startTime ? mEvent.getStartHour() : mEvent.getEndHour(), startTime ? mEvent.getStartMinutes() : mEvent.getEndMinutes());
		dialog.show();
	}

	private void setMileage(final boolean start) {
		final ChooseValueView valueView = (ChooseValueView) View.inflate(
				ActivityAddNewEvent.this, R.layout.choose_value_dialog_layout, null);
		new MaterialDialog.Builder(ActivityAddNewEvent.this)
				.customView(valueView, false)
				.positiveText(R.string.action_ok)
				.negativeText(android.R.string.cancel)
				.positiveColor(mCurrentBackgroundColor)
				.negativeColor(mCurrentBackgroundColor)
				.callback(new MaterialDialog.ButtonCallback() {

					@Override
					public void onPositive(MaterialDialog dialog) {
						if(start) {
							mEvent.setMileageStart(valueView.getValue());
						} else {
							mEvent.setMileageEnd(valueView.getValue());
						}
						setData(mEvent);
					}

				}).show();
		valueView.setValue(start ? mEvent.getMileageStart() : mEvent.getMileageEnd());
	}
	
	private void setDate(final boolean startDate){
		final DatePickerDialogView v = (DatePickerDialogView) View.inflate(this, R.layout.date_picker_dialog_view, null);
		
		new MaterialDialog.Builder(ActivityAddNewEvent.this)
		.customView(v, false)
		.backgroundColor(getResources().getColor(R.color.white))
		.dividerColor(getResources().getColor(R.color.white))
		.negativeText(R.string.action_cancel)
		.positiveText(R.string.action_ok)
		.neutralText(R.string.action_today)
		.positiveColor(mCurrentBackgroundColor)
		.negativeColor(mCurrentBackgroundColor)
		.neutralColor(mCurrentBackgroundColor)
		.callback(new MaterialDialog.ButtonCallback() {

			@Override
			public void onPositive(MaterialDialog dialog) {
				if (startDate) {
					mEvent.setStartDate(DateCalculations.formatDateMillisAndSeconds(v.getTimeInMillis()));
				} else {
					mEvent.setEndDate(DateCalculations.formatDateMillisAndSeconds(v.getTimeInMillis()));
				}
				setData(mEvent);
			}
			
			@Override
			public void onNeutral(MaterialDialog dialog) {
				if (startDate) {
					mEvent.setStartDate(DateCalculations.formatDateMillisAndSeconds(System.currentTimeMillis()));
				} else {
					mEvent.setEndDate(DateCalculations.formatDateMillisAndSeconds(System.currentTimeMillis()));
				}
				setData(mEvent);
			}
		}).show();
		v.setDateInMillis(startDate ? mEvent.getStartDateInMillis() : mEvent.getEndDateInMillis());
	}

	private class EventSpinnerAdapter extends BaseAdapter {
		private final LayoutInflater mInflater;
		private final String[] mEvents;
		
		
		public EventSpinnerAdapter(Context context){
			mInflater = LayoutInflater.from(context);
			mColors = context.getResources().getIntArray(R.array.event_colors);
			mEvents = context.getResources().getStringArray(R.array.event_explanations);
		}
		
		@Override
		public int getCount() {
			return mEvents.length;
		}

		@Override
		public String getItem(int position) {
			return mEvents[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			
			if(convertView == null){
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.add_new_event_spinner_item, parent, false);
				holder.mCircle = (ListCircle)convertView.findViewById(R.id.add_new_event_spinner_item_circle);
				holder.mCircle.setSelectable(false);
				holder.mText = (TextView)convertView.findViewById(R.id.add_new_event_spinner_item_title);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			holder.mCircle.setCircleBackgroundColor(mColors[position]);
			holder.mText.setText(mEvents[position]);
			
			return convertView;
		}
		
		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			
			if(convertView == null){
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.add_new_event_spinner_item_dropdown, parent, false);
				holder.mCircle = (ListCircle)convertView.findViewById(R.id.add_new_event_spinner_item_circle);
				holder.mCircle.setSelectable(false);
				holder.mText = (TextView)convertView.findViewById(R.id.add_new_event_spinner_item_title);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			holder.mCircle.setCircleBackgroundColor(mColors[position]);
			holder.mText.setText(mEvents[position]);
			
			return convertView;
		}
		
		
	}

	static class ViewHolder {
		ListCircle mCircle;
		TextView mText;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if(mEvent != null){
			mEvent.setEventType(position);
		}
		
		if(position == Event.EVENT_TYPE_DRIVING && mExtraInfoContainer.getVisibility() != View.VISIBLE){
			mExtraInfoContainer.setVisibility(View.VISIBLE);
		} else if(position != Event.EVENT_TYPE_DRIVING &&mExtraInfoContainer.getVisibility() != View.GONE){
			mExtraInfoContainer.setVisibility(View.GONE);
		}
		
		if(position == Event.EVENT_TYPE_NORMAL_BREAK){
			if(mIsSplitBreak.getVisibility() == View.GONE){
				mIsSplitBreak.setVisibility(View.VISIBLE);
			}
		} else if(mIsSplitBreak.getVisibility() == View.VISIBLE){
			mIsSplitBreak.setVisibility(View.GONE);
		}
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			animateStatusAndToolbarColor(mCurrentBackgroundColor, mColors[position]);
		} else {
			animateToolbarColor(mCurrentBackgroundColor, mColors[position]);
		}

	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}
	
	private void animateToolbarColor(int colorFrom, int colorTo){
		final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
		
		colorAnimation.addUpdateListener(new AnimatorUpdateListener() {

		    @Override
		    public void onAnimationUpdate(ValueAnimator animator) {
		    	mCurrentBackgroundColor = (int)animator.getAnimatedValue();
		    	mToolbar.setBackgroundColor(mCurrentBackgroundColor);
		    	
		    }

		});
		colorAnimation.setDuration(300).start();
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateStatusAndToolbarColor(int colorFrom, final int colorTo){
		mCurrentBackgroundColor = colorTo;
		Animator animator = ViewAnimationUtils.createCircularReveal(
                mRevealView,
                mToolbar.getWidth() / 2,
                mToolbar.getHeight() / 2, 0,
                mToolbar.getWidth() / 2);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mRevealView.setBackgroundColor(colorTo);
            }
        });
       
        Animator statusanimator = ViewAnimationUtils.createCircularReveal(
                mStatusRevealView,
                mToolbar.getWidth() / 2,
                mToolbar.getHeight() / 2, 0,
                mToolbar.getWidth() / 2);
        statusanimator.addListener(new AnimatorListenerAdapter() {
        	
        	@Override
            public void onAnimationStart(Animator animation) {
        		mStatusRevealView.setBackgroundColor(makeColorDarker(colorTo));
            }
        });
        mStatusRevealBackgroundView.setBackgroundColor(makeColorDarker(colorFrom));
        mRevealBackgroundView.setBackgroundColor(colorFrom);
        animator.setStartDelay(200);
        animator.setDuration(125);
        animator.start();
        
        statusanimator.setStartDelay(200);
        statusanimator.setDuration(125);
        statusanimator.start();
        mRevealView.setVisibility(View.VISIBLE);
        mStatusRevealView.setVisibility(View.VISIBLE);
    }
	
	private void setToolbarColor(int color){
		mCurrentBackgroundColor = color;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			mRevealView.setBackgroundColor(color);
			mStatusRevealView.setBackgroundColor(makeColorDarker(color));
		}
	}
	
	private int makeColorDarker(int color){
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.8f; // value component
		color = Color.HSVToColor(hsv);
		return color;
	}
	

	
	

}
