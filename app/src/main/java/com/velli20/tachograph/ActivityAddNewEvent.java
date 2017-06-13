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


import com.afollestad.materialdialogs.MaterialDialog;
import com.velli20.tachograph.database.DataBaseHandler;

import com.velli20.tachograph.database.DataBaseHandler.OnGetEventTaskCompleted;
import com.velli20.tachograph.googledatetimepicker.DatePickerDialogView;
import com.velli20.tachograph.views.ChooseValueView;
import com.velli20.tachograph.views.ListCircle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.TimePickerDialog;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Locale;



public class ActivityAddNewEvent extends AppCompatActivity implements OnClickListener, OnItemSelectedListener {
    public static final int DISPLAY_MODE_CREATE_NEW = 0;
    public static final int DISPLAY_MODE_EDIT_OR_DELETE = 1;
    public static final String INTENT_EXTRA_DISPLAY_MODE = "display mode";
    public static final String INTENT_EXTRA_EVENT_ROW_ID = "event row mId";
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

    private EditText mNote;
    private EditText mStartLocation;
    private EditText mEndLocation;
    
    private Toolbar mToolbar;

    private View mRevealView;
    private View mRevealBackgroundView;
    private View mStatusRevealView;
    private View mStatusRevealBackgroundView;

    private LinearLayout mExtraInfoContainer;

    private TextView mEndTimeTitle;
    private TextView mEndDateTitle;

    @Override
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_add_new_event);
    	final Bundle bundle = getIntent().getExtras();
    	
    	mColors = getResources().getIntArray(R.array.event_colors);
    	mCurrentBackgroundColor = getResources().getColor(R.color.event_driving);
    	
    	mToolbar = (Toolbar)findViewById(R.id.activity_add_new_event_tool_bar);
    	mRevealView = findViewById(R.id.activity_add_new_event_tool_bar_reveal_view);
        mRevealBackgroundView = findViewById(R.id.activity_add_new_event_tool_bar_reveal_background);
        mStatusRevealView = findViewById(R.id.activity_add_new_event_status_bar_reveal_view);
        mStatusRevealBackgroundView = findViewById(R.id.activity_add_new_event_status_bar_reveal_background);
        
        mEventSpinner = (Spinner)findViewById(R.id.activity_add_new_event_spinner);
        mStartTime = (Button)findViewById(R.id.activity_add_new_event_button_start_time);
        mStartDate = (Button)findViewById(R.id.activity_add_new_event_button_start_date);
        mEndTime = (Button)findViewById(R.id.activity_add_new_event_button_end_time);
        mEndDate = (Button)findViewById(R.id.activity_add_new_event_button_end_date);
        mMileageStart = (Button)findViewById(R.id.activity_add_new_event_button_mileage_start);
        mMileageEnd = (Button)findViewById(R.id.activity_add_new_event_button_mileage_end);
        mNote = (EditText)findViewById(R.id.add_new_event_note);
        mStartLocation = (EditText)findViewById(R.id.activity_add_new_event_edit_text_start_location);
        mEndLocation = (EditText)findViewById(R.id.activity_add_new_event_edit_text_end_location);

        mEndTimeTitle = (TextView) findViewById(R.id.activity_add_new_event_title_end_time);
        mEndDateTitle = (TextView) findViewById(R.id.activity_add_new_event_title_end_date);

        mExtraInfoContainer = (LinearLayout)findViewById(R.id.activity_add_new_event_extra_info_container);
        
        final Button[] mButtons = {mStartTime, mStartDate, mEndTime, mEndDate, mMileageStart, mMileageEnd};

        for (Button mButton : mButtons) {
            mButton.setOnClickListener(this);
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
		if(ab != null) {
			ab.setDisplayShowHomeEnabled(true);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setTitle(mDisplayMode == DISPLAY_MODE_EDIT_OR_DELETE ? R.string.title_edit_log_event : R.string.title_add_new_log_event);
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
    		mEvent.setStartTime(DateUtils.getCurrentHour(start), DateUtils.getCurrentMinute(start));
    		mEvent.setEndTime(DateUtils.getCurrentHour(end), DateUtils.getCurrentMinute(end));
    		setData(mEvent);
    	}
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.menu_activity_add_new_event, menu);
		return super.onCreateOptionsMenu(menu);
	}
	 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_item_save:
			mEvent.setNote(mNote.getText().toString());
			mEvent.setStartLocation(mStartLocation.getText().toString());
			mEvent.setEndLocation(mEndLocation.getText().toString());
			mEvent.setEndDate(DateUtils.setTimeToDate(mEvent.getEndDateInMillis(), mEvent.getEndHour(), mEvent.getEndMinutes()));
			mEvent.setStartDate(DateUtils.setTimeToDate(mEvent.getStartDateInMillis(), mEvent.getStartHour(), mEvent.getStartMinutes()));

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
		case R.id.activity_add_new_event_button_start_time:
			setTime(true);
			break;
		case R.id.activity_add_new_event_button_start_date:
			setDate(true);
			break;
		case R.id.activity_add_new_event_button_end_time:
			setTime(false);
			break;
		case R.id.activity_add_new_event_button_end_date:
			setDate(false);
			break;
		case R.id.activity_add_new_event_button_mileage_start:
			setMileage(true);
			break;
		case R.id.activity_add_new_event_button_mileage_end:
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
		mStartTime.setText(DateUtils.createTimeString(ev.getStartHour(), ev.getStartMinutes()));
		mStartDate.setText(DateUtils.createDateString(ev.getStartDateInMillis()));
		mEndTime.setText(DateUtils.createTimeString(ev.getEndHour(), ev.getEndMinutes()));
		mEndDate.setText(DateUtils.createDateString(ev.getEndDateInMillis()));
		mMileageStart.setText(String.format(Locale.getDefault(), "%d km", ev.getMileageStart()));
		mMileageEnd.setText(String.format(Locale.getDefault(), "%d km", ev.getMileageEnd()));
		mNote.setText(ev.getNote());
		mStartLocation.setText(ev.getStartLocation());
		mEndLocation.setText(ev.getEndLocation());

		if(ev.isRecordingEvent()){
			mEndTime.setVisibility(View.GONE);
			mEndDate.setVisibility(View.GONE);
            mEndTimeTitle.setVisibility(View.GONE);
            mEndDateTitle.setVisibility(View.GONE);
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
                DataBaseHandler.getInstance().updateEvent(mEvent);
            }
            finish();
        }

	}


    private void setTime(final boolean startTime) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (startTime) {
                    mEvent.setStartTime(hourOfDay, minute);
                } else {
                    mEvent.setEndTime(hourOfDay, minute);
                }
                setData(mEvent);
            }
        }, startTime ? mEvent.getStartHour() : mEvent.getEndHour()
                , startTime ? mEvent.getStartMinutes() : mEvent.getEndMinutes(), true);
        timePickerDialog.show();
    }

	private void setMileage(final boolean start) {
		final ChooseValueView valueView = (ChooseValueView) View.inflate(
				ActivityAddNewEvent.this, R.layout.choose_value_dialog_layout, null);
		new MaterialDialog.Builder(ActivityAddNewEvent.this)
				.customView(valueView, true)
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
					mEvent.setStartDate(DateUtils.formatDateMillisAndSeconds(v.getTimeInMillis()));
				} else {
					mEvent.setEndDate(DateUtils.formatDateMillisAndSeconds(v.getTimeInMillis()));
				}
				setData(mEvent);
			}
			
			@Override
			public void onNeutral(MaterialDialog dialog) {
				if (startDate) {
					mEvent.setStartDate(DateUtils.formatDateMillisAndSeconds(System.currentTimeMillis()));
				} else {
					mEvent.setEndDate(DateUtils.formatDateMillisAndSeconds(System.currentTimeMillis()));
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
			mEvents = context.getResources().getStringArray(R.array.events);
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
		public long getItemId(int position) { return 0; }

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

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			animateStatusAndToolbarColor(mCurrentBackgroundColor, mColors[position]);
		} else {
			animateToolbarColor(mCurrentBackgroundColor, mColors[position]);
		}

	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) { }
	
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
