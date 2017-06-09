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

import java.util.ArrayList;

import com.velli20.tachograph.database.DataBaseHandler;
import com.velli20.tachograph.database.DataBaseHandler.OnDatabaseEditedListener;
import com.velli20.tachograph.database.DataBaseHandler.OnTaskCompleted;
import com.velli20.tachograph.database.GetLogSummaryTask;
import com.velli20.tachograph.restingtimes.WeekHolder;
import com.velli20.tachograph.restingtimes.WorkDayHolder;
import com.velli20.tachograph.views.StepperCircle;
import com.velli20.tachograph.views.RobotoLightTextView;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class ActivityLogSummaryDay extends AppCompatActivity implements OnDatabaseEditedListener {
	public static final String INTENT_EXTRA_START = "start";
	public static final String INTENT_EXTRA_END = "end";
    public static final String INTENT_EXTRA_EVENT_IDS = "eventIds";

	private RecyclerView mList;
	private ListAdapterActivityLogSummaryDay mAdapter;
    private WorkDayHolder mDaySummary;
	
	private long mStart;
	private long mEnd;
	private ArrayList<Integer> mRowIds;

	@Override
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_log_summary_day);

		mList = (RecyclerView) findViewById(R.id.activity_day_overview_list);

		
		setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
	
		if(getIntent() != null){
			mStart = getIntent().getLongExtra(INTENT_EXTRA_START, -1);
			mEnd = getIntent().getLongExtra(INTENT_EXTRA_END, -1);
            mRowIds = getIntent().getIntegerArrayListExtra(INTENT_EXTRA_EVENT_IDS);
            getEvents(mRowIds);
		}
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			findViewById(R.id.toolbar_shadow).setVisibility(View.GONE);
	    }

		final ActionBar ab = getSupportActionBar();

		if(ab != null) {
			ab.setTitle(DateUtils.dateSummary(mStart, mEnd));
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayShowHomeEnabled(true);
		}
		DataBaseHandler.getInstance().registerOnDatabaseEditedListener(this);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		DataBaseHandler.getInstance().unregisterOnDatabaseEditedListener(this);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

    private void getEvents(ArrayList<Integer> rowIds) {
        if(rowIds == null) {
            return;
        }
        DataBaseHandler.getInstance().getEventsByRowIds(new OnTaskCompleted() {
            @Override
            public void onTaskCompleted(ArrayList<Event> list) {
                initializeList(list);
            }
        }, rowIds);

		DataBaseHandler.getInstance().getWorkingTimesWithinEventIds(new GetLogSummaryTask.OnWorkingTimeCalculationsReadyListener() {
			@Override
			public void onWorkingTimeCalculationsReady(ArrayList<WeekHolder> workingWeeks) {
				if(workingWeeks != null && !workingWeeks.isEmpty()) {
					WeekHolder weekSummary = workingWeeks.get(workingWeeks.size()-1);
                    if(weekSummary != null && !weekSummary.getWorkdaysList().isEmpty()) {
                        mDaySummary = weekSummary.getWorkdaysList().get(weekSummary.getWorkdaysList().size()-1);
                        if(mAdapter != null) {
                            mAdapter.setWorkDayHolder(mDaySummary);
                        }
                    }

				}
			}
		}, rowIds, true, true, false);
    }

    private void initializeList(ArrayList<Event> events) {
        if(mAdapter == null && events != null) {
            mAdapter = new ListAdapterActivityLogSummaryDay(ActivityLogSummaryDay.this, events);
            if(mDaySummary != null) {
                mAdapter.setWorkDayHolder(mDaySummary);
            }

            mList.setLayoutManager(new LinearLayoutManager(this));
            mList.setAdapter(mAdapter);
        }
    }

	
	private class ListAdapterActivityLogSummaryDay extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private static final int VIEW_TYPE_HEADER = 0;
		private static final int VIEW_TYPE_NORMAL = 1;
		
		private final LayoutInflater mInflater;
		private final ArrayList<Event> mListItems;
		private WorkDayHolder mWorkDay;
		private final String[] mExplanations;
		
		private final int[] mColors;

		private ListAdapterActivityLogSummaryDay(Context c, ArrayList<Event> items) {
			mInflater = LayoutInflater.from(c);
			mListItems = items;
			mColors = c.getResources().getIntArray(R.array.event_colors);
			mExplanations = c.getResources().getStringArray(R.array.event_explanations);
			setHasStableIds(true);
		}

        private void setWorkDayHolder(WorkDayHolder holder){
			mWorkDay = holder;
			notifyItemChanged(0);
		}

		@Override
		public long getItemId(int position) {
			if(position == 0){
				return VIEW_TYPE_HEADER;
			}
			return mListItems.get(position -1).getRowId();
		}
		 
		@Override
		public int getItemCount() {
			return mListItems.size() +1 ;
		}
		
		@Override
	    public int getItemViewType(int position) {
			if(position == 0){
				return VIEW_TYPE_HEADER;
			} else {
				return VIEW_TYPE_NORMAL;
			}
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			if(getItemViewType(position) == VIEW_TYPE_HEADER){
				((ViewHolderHeader)holder).mDailyDrivingTime.setText(mWorkDay == null ? "--.--" : DateUtils.convertMinutesToTimeString(mWorkDay.getDailyDrivingTime()));
				((ViewHolderHeader)holder).mWtdDailyWorkingTime.setText(mWorkDay == null ? "--.--" : DateUtils.convertMinutesToTimeString(mWorkDay.getWtdDailyWorkingTime()));
				((ViewHolderHeader)holder).mDailyWorkingTime.setText(mWorkDay == null ? "--.--" : DateUtils.convertMinutesToTimeString(mWorkDay.getDailyWorkingTime()));
				((ViewHolderHeader)holder).mDailyRestTime.setText(mWorkDay == null ? "--.--" : DateUtils.convertMinutesToTimeString(mWorkDay.getDailyRest()));
				((ViewHolderHeader)holder).mDailyDrivenDistance.setText(mWorkDay == null ? "--.--"  : String.format("%.2f", mWorkDay.getDailyDrivenDistance()) + " km");
				return;
			}
			Event ev = mListItems.get(position -1);
			((ViewHolderStepper)holder).itemView.setOnClickListener(new ClickListener(position));
			((ViewHolderStepper)holder).mTitle.setText(mExplanations[ev.getEventType()]);
			((ViewHolderStepper)holder).mStartTime.setText(DateUtils.createShortTimeString(ev.getStartDateInMillis()));
			
			if(ev.isRecordingEvent()){
				((ViewHolderStepper)holder).mHours.setText(DateUtils.convertDatesInHours(ev.getStartDateInMillis(), System.currentTimeMillis()));
			} else {
				((ViewHolderStepper)holder).mHours.setText(DateUtils.convertDatesInHours(ev.getStartDateInMillis(), ev.getEndDateInMillis()));
			}
			if(position == 1 && getItemCount() == 2){
				((ViewHolderStepper)holder).mIcon.setType(StepperCircle.TYPE_NO_BARS);
			} else if(position == 1){
				((ViewHolderStepper)holder).mIcon.setType(StepperCircle.TYPE_START);
			} else if(position + 1 == getItemCount()){
				
				((ViewHolderStepper)holder).mIcon.setType(StepperCircle.TYPE_END);
			} else {
				((ViewHolderStepper)holder).mIcon.setType(StepperCircle.TYPE_NORMAL);
			}
			((ViewHolderStepper)holder).mIcon.setColor(mColors[ev.getEventType()]);
			
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
			if(viewType == VIEW_TYPE_HEADER){
				return new ViewHolderHeader(mInflater.inflate(R.layout.list_item_log_summary_day_header, root, false));
			} else {
				return new ViewHolderStepper(mInflater.inflate(R.layout.list_item_log_summary_day_stepper, root, false));
			}
		}
		
		private class ViewHolderStepper extends RecyclerView.ViewHolder {
			final TextView mTitle;
			final TextView mHours;
			final TextView mStartTime;
			final StepperCircle mIcon;

			private ViewHolderStepper(View itemView) {
				super(itemView);
				mTitle = (TextView) itemView.findViewById(R.id.list_item_log_summary_day_stepper_title);
				mHours = (TextView) itemView.findViewById(R.id.list_item_log_summary_day_stepper_duration);
				mStartTime = (TextView) itemView.findViewById(R.id.list_item_log_summary_day_stepper_start_date);
				mIcon = (StepperCircle) itemView.findViewById(R.id.list_item_log_summary_day_stepper);
			}
			
		}

		private class ViewHolderHeader extends RecyclerView.ViewHolder {
			final RobotoLightTextView mDailyDrivingTime;
			final RobotoLightTextView mWtdDailyWorkingTime;
			final RobotoLightTextView mDailyWorkingTime;
			final RobotoLightTextView mDailyRestTime;
			final RobotoLightTextView mDailyDrivenDistance;

			private ViewHolderHeader(View itemView) {
				super(itemView);
				
				mDailyDrivingTime = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_day_daily_driving_time);
				mWtdDailyWorkingTime = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_day_wtd_daily_work_time);
				mDailyWorkingTime = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_day_daily_work_time);
				mDailyRestTime = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_day_daily_rest_time);
				mDailyDrivenDistance = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_day_daily_driven_distance);
			}
		}

        private class ClickListener implements OnClickListener {
			private final int mPosition;

            private ClickListener(int position){
				mPosition = position;
			}

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ActivityLogSummaryDay.this, ActivityEventDetails.class);
				intent.putExtra(ActivityEventDetails.INTENT_EXTRA_EVENT_ID, mListItems.get(mPosition -1).getRowId());
				startActivity(intent);
				
			}
		}
	}

	@Override
	public void onDatabaseEdited(int action, int rowId) {
        getEvents(mRowIds);
	}

}
