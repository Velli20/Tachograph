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

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import com.velli20.tachograph.database.DataBaseHandler;
import com.velli20.tachograph.database.GetLogSummaryTask;
import com.velli20.tachograph.restingtimes.WeekHolder;
import com.velli20.tachograph.restingtimes.WorkDayHolder;
import com.velli20.tachograph.views.StepperCircle;
import com.velli20.tachograph.views.RobotoLightTextView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.AdapterView;

public class ActivityLogSummaryWeek extends AppCompatActivity implements AdapterView.OnItemClickListener {
	public static final boolean DEBUG = true;
	public static final String TAG = "ActivityLogSummaryWeek ";

    public static final String INTENT_EVENT_IDS = "eventIds";
    public static final String INTENT_START_DATE = "startDate";

	private RecyclerView mList;
	private WeekAdapter mAdapter;
	
	private ArrayList<Integer> mRowIds;
	private long mStartDate;

	private WeekHolder mWeek;

	@Override
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_log_summary_week);

		mList = (RecyclerView) findViewById(R.id.activity_week_overview_list);

		setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

		if(getIntent() != null) {
            mRowIds = getIntent().getIntegerArrayListExtra(INTENT_EVENT_IDS);
            mStartDate = getIntent().getLongExtra(INTENT_START_DATE, -1);
		}
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			findViewById(R.id.toolbar_shadow).setVisibility(View.GONE);
	    }
		
		final ActionBar ab = getSupportActionBar();
		if(ab != null) {
            if(mStartDate != -1) {
                ab.setTitle(getString(R.string.title_week) + " " + DateUtils.getWeekOfYear(mStartDate));
            }
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayShowHomeEnabled(true);
		}


        loadLogSummary(mRowIds);
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

    private void loadLogSummary(ArrayList<Integer> rowIds) {
        if(rowIds == null) {
            return;
        }
        DataBaseHandler.getInstance().getWorkingTimesWithinEventIds(new GetLogSummaryTask.OnWorkingTimeCalculationsReadyListener() {
            @Override
            public void onWorkingTimeCalculationsReady(ArrayList<WeekHolder> workingWeeks) {
                if(workingWeeks != null && !workingWeeks.isEmpty()) {
                    mWeek = workingWeeks.get(workingWeeks.size()-1);
                    initializeList();
                }
            }
        }, rowIds, true, true, true);
    }

    private void initializeList() {
        if(mAdapter == null && mWeek != null) {
            mAdapter = new WeekAdapter(this, mWeek);
            mAdapter.setOnClickListener(this);

            mList.setLayoutManager(new LinearLayoutManager(this));
            mList.setAdapter(mAdapter);
        }
    }
	
	private class WeekAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private static final int VIEW_TYPE_CHART = 0;
		private static final int VIEW_TYPE_NORMAL = 1;
		
		private final LayoutInflater mInflater;
		private AdapterView.OnItemClickListener mListener;
		private WeekHolder mItems;
		
		private final String mDailyRest;
		private final String[] SHORT_WEEK_DAYS = new DateFormatSymbols().getShortWeekdays();
		
		public WeekAdapter(Context context, WeekHolder week) {
			mInflater = LayoutInflater.from(context);
			mItems = week;
			
			final Resources res = context.getResources();
			
			mDailyRest = res.getString(R.string.log_summary_daily_rest_time);
		}
		
		public void setItems(WeekHolder week){
			mItems = week;
			notifyDataSetChanged();
		}
		
		public void setOnClickListener(AdapterView.OnItemClickListener l) {
			mListener = l;
		}
		
		@Override
		public int getItemCount() {
			return mItems.getWorkDays().size() +1;
		}
		
		@Override
	    public int getItemViewType(int position) {
			if(position == 0){
				return VIEW_TYPE_CHART;
			} else {
				return VIEW_TYPE_NORMAL;
			}
		}
		
		private String getShortWeekday(long startDay){
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(startDay);
			return SHORT_WEEK_DAYS[c.get(Calendar.DAY_OF_WEEK)];
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			int viewType = getItemViewType(position);
			
			if(viewType == VIEW_TYPE_CHART){
				setHeaderData(((ViewHolderHeader)holder), mItems);
			} else {
				holder.itemView.setOnClickListener(new ClickListener(position));
				setDayData(((ViewHolderStepper)holder), position);
			}
		}
		
		public void setDayData(ViewHolderStepper holder, int position){
			WorkDayHolder day = mItems.getWorkDays().get(position -1);

			holder.mWeekDay.setText(getShortWeekday(day.getStartDate()));
			holder.mTitle.setText(DateUtils.dateSummary(day.getStartDate(), day.getEndDate()));
			holder.mDailyRestTime.setText(DateUtils.convertMinutesToTimeString(day.getDailyRest()));

            holder.mDailyRestTitle.setText(mDailyRest);

            holder.mDailyDriveTime.setText(DateUtils.convertMinutesToTimeString(day.getDailyDrivingTime()));
            holder.mDailyWorkTime.setText(DateUtils.convertMinutesToTimeString(day.getDailyWorkingTime()));
            holder.mDailyDrivenDistance.setText(String.format("%.2f", day.getDailyDrivenDistance()) + " km");

			if(position == 1 && position == getItemCount() -1){
				holder.mIcon.setType(StepperCircle.TYPE_NO_BARS);
			} else if(position == 1){
				holder.mIcon.setType(StepperCircle.TYPE_START);
			} else if(position + 1 == getItemCount()){
				holder.mIcon.setType(StepperCircle.TYPE_END);
			} else {
				holder.mIcon.setType(StepperCircle.TYPE_NORMAL);
			}
		}

		private void setHeaderData(ViewHolderHeader holder, WeekHolder week){
			holder.mWeeklyDriveTime.setText(DateUtils.convertMinutesToTimeString(week.getWeeklyDrivingTime()));
			holder.mWeeklyWorkTime.setText(DateUtils.convertMinutesToTimeString(week.getWeeklyWorkingTime()));
			holder.mWtdWeeklyWorkTime.setText(DateUtils.convertMinutesToTimeString(week.getWtdWeeklyWorkingTime()));
			holder.mWeeklyRestTime.setText(DateUtils.convertMinutesToTimeString(week.getWeeklyRest()));
			holder.mWeeklyDrivenDistance.setText(String.format(Locale.getDefault(), "%.2f", week.getWeeklyDrivenDistance()) + " km");
		}
		
		

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
			if(viewType == VIEW_TYPE_CHART){
				return new ViewHolderHeader(mInflater.inflate(R.layout.list_item_log_summary_week_header, root, false));
			} else {
				return new ViewHolderStepper(mInflater.inflate(R.layout.list_item_log_summary_week_stepper, root, false));
			}
		}

		private class ViewHolderHeader extends RecyclerView.ViewHolder {
			private RobotoLightTextView mWeeklyDriveTime;
			private RobotoLightTextView mWeeklyWorkTime;
			private RobotoLightTextView mWtdWeeklyWorkTime;
			private RobotoLightTextView mWeeklyRestTime;
			private RobotoLightTextView mWeeklyDrivenDistance;

			private ViewHolderHeader(View itemView) {
				super(itemView);
				
				mWeeklyDriveTime = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_week_weekly_driving_time);
				mWeeklyWorkTime = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_week_weekly_working_time);
				mWtdWeeklyWorkTime = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_week_wtd_weekly_working_time);
				mWeeklyRestTime = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_week_weekly_rest_time);
				mWeeklyDrivenDistance = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_week_driven_distance);
			}
			
		}
		
		private class ViewHolderStepper extends RecyclerView.ViewHolder {
			private RobotoLightTextView mTitle;
			private RobotoLightTextView mWeekDay;
			private StepperCircle mIcon;

			private RobotoLightTextView mDailyDriveTime;
			private RobotoLightTextView mDailyWorkTime;
			private RobotoLightTextView mDailyRestTime;
			private RobotoLightTextView mDailyRestTitle;
			private RobotoLightTextView mDailyDrivenDistance;


			private ViewHolderStepper(View itemView) {
				super(itemView);
				
				mTitle = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_week_stepper_title);
				mWeekDay = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_week_stepper_weekday);
				mIcon = (StepperCircle) itemView.findViewById(R.id.list_item_log_summary_week_stepper_icon);
				
				mDailyDriveTime = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_week_stepper_daily_driving_time);
				mDailyWorkTime = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_week_stepper_daily_working_time);
				mDailyRestTime = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_week_stepper_daily_rest_time);
				mDailyRestTitle = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_week_stepper_daily_rest_time_title);
				mDailyDrivenDistance  = (RobotoLightTextView) itemView.findViewById(R.id.list_item_log_summary_week_stepper_daily_driven_distance);

			}
			
		}
		
		private class ClickListener implements OnClickListener {
			private final int mPosition;

			private ClickListener(int position) {
				mPosition = position;
			}

			@Override
			public void onClick(View v) {
				if(mListener != null) {
					mListener.onItemClick(null, v, mPosition, -1);
				}
				
			}
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        WorkDayHolder workday =  mWeek.getWorkDays().get(position-1);
        if(workday != null) {
            Intent i = new Intent(this, ActivityLogSummaryDay.class);
            i.putExtra(ActivityLogSummaryDay.INTENT_EXTRA_START, workday.getStartDate());
            i.putExtra(ActivityLogSummaryDay.INTENT_EXTRA_END, workday.getEndDate());
            i.putExtra(ActivityLogSummaryDay.INTENT_EXTRA_EVENT_IDS, workday.getEventIds());
            startActivity(i);
        }
	}
}
