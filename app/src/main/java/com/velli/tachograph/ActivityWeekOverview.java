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

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;

import com.velli.tachograph.database.DataBaseHandler;
import com.velli.tachograph.database.DataBaseHandler.OnDatabaseEditedListener;
import com.velli.tachograph.database.DataBaseHandler.OnTaskCompleted;
import com.velli.tachograph.restingtimes.BreakAndRestTimeCalculations;
import com.velli.tachograph.restingtimes.WeekHolder;
import com.velli.tachograph.restingtimes.BreakAndRestTimeCalculations.OnCalculatiosReadyListener;
import com.velli.tachograph.restingtimes.WorkDayHolder;
import com.velli.tachograph.views.OverviewCircle;
import com.velli.tachograph.views.RobotoLightTextView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.LinearLayout;

public class ActivityWeekOverview extends AppCompatActivity implements OnTaskCompleted, OnDatabaseEditedListener, AdapterView.OnItemClickListener {
	public static final boolean DEBUG = true;
	public static final String TAG = "ActivityWeekOverview ";
	
	public static final String INTENT_EXTRA_START = "start";
	public static final String INTENT_EXTRA_END = "end";

	private RecyclerView mList;
	private WeekAdapter mAdapter;
	
	private long mStart;
	private long mEnd;
	
	private WeekHolder mWeek;
	private boolean mShowAutomaticallyCalculatedRestingEvents = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_week_overview);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		
		mList = (RecyclerView) findViewById(R.id.activity_week_overview_list);
		mList.setLayoutManager(new LinearLayoutManager(this));
		
		setSupportActionBar(mToolbar);
		
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    	mShowAutomaticallyCalculatedRestingEvents = prefs.getBoolean(getResources().getString(R.string.preference_key_count_daily_and_weekly_rest_automatically), false);
		
		if(getIntent() != null){
			mStart = getIntent().getLongExtra(INTENT_EXTRA_START, -1);
			mEnd = getIntent().getLongExtra(INTENT_EXTRA_END, -1);
			
			if(mStart != -1 && mEnd != -1){
				DataBaseHandler.getInstance().getEventsByTime(mStart, mEnd, this, true, mShowAutomaticallyCalculatedRestingEvents);
			}
		}
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			findViewById(R.id.toolbar_shadow).setVisibility(View.GONE);
	    }
		
		final ActionBar ab = getSupportActionBar();
		if(ab != null) {
			ab.setTitle(getString(R.string.title_week) + " " + DateCalculations.getWeekOfYear(mStart));
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayShowHomeEnabled(true);
		}
		DataBaseHandler.getInstance().registerOnDatabaseEditedListener(this);
	}

	@Override
	public void onTaskCompleted(ArrayList<Event> list) {
		if(list != null){			
			new BreakAndRestTimeCalculations().calculateBreakAndRestingtimes(list, new OnCalculatiosReadyListener() {
				
				@Override
				public void onCalculationsReady(ArrayList<WeekHolder> weeklist) {
					if(weeklist != null && !weeklist.isEmpty()){
						mWeek = weeklist.get(0);
						if(mAdapter != null) {
							mAdapter.setItems(mWeek);
							
						} else {
							mAdapter = new WeekAdapter(ActivityWeekOverview.this, mWeek, getLayoutInflater());
							mAdapter.setOnClickListener(ActivityWeekOverview.this);
							mList.setAdapter(mAdapter);
						}
					} 
					
				}
			});
		}
		
	}

	@Override
	public void onDatabaseEdited(int action, int rowId) {
		DataBaseHandler.getInstance().getEventsByTime(mStart, mEnd, this, true, mShowAutomaticallyCalculatedRestingEvents);
		
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
	
	private class WeekAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private static final int VIEW_TYPE_CHART = 0;
		private static final int VIEW_TYPE_NORMAL = 1;
		
		private final LayoutInflater mInflater;
		private AdapterView.OnItemClickListener mListener;
		private WeekHolder mItems;
		
		private final String mDailyRest;
		private final String mWeeklyRest;
		private final String[] SHORT_WEEK_DAYS = new DateFormatSymbols().getShortWeekdays();
		
		public WeekAdapter(Context context, WeekHolder week, LayoutInflater i) {
			mInflater = i;
			mItems = week;
			
			final Resources res = context.getResources();
			
			mDailyRest = res.getString(R.string.working_days_daily_rest_time);
			mWeeklyRest = res.getString(R.string.working_days_weekly_rest_time);
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
		
		public String getShortWeekday(long startDay){
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(startDay);
			return SHORT_WEEK_DAYS[c.get(Calendar.DAY_OF_WEEK)];
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			int viewType = getItemViewType(position);
			
			if(viewType == VIEW_TYPE_CHART){
				setChartData(((ViewHolderChart)holder), mItems);
				
			} else {
				holder.itemView.setOnClickListener(new ClickListener(position));
				setDayData(((ViewHolderItem)holder), position);

			}
			
		}
		
		public void setDayData(ViewHolderItem holder, int position){
			WorkDayHolder day = mItems.getWorkDays().get(position -1);
			holder.mWeekDay.setText(getShortWeekday(day.getWorkdayStart()));
			holder.mTitle.setText(DateCalculations.dateSummary(day.getWorkdayStart(), day.getWorkdayEnd()));			
			holder.mDailyRestTime.setText(DateCalculations.convertMinutesToTimeString(day.getDailyRest()));

			if(!day.isWeeklyRest()){
				holder.mDailyRestTitle.setText(mDailyRest);
			    
				holder.mDailyDriveTime.setVisibility(View.VISIBLE);
				holder.mDailyDriveConatiner.setVisibility(View.VISIBLE);
				holder.mDailyWorkTime.setVisibility(View.VISIBLE);
				holder.mDailyWorkContainer.setVisibility(View.VISIBLE);
				holder.mDailyDrivenDistanceContainer.setVisibility(View.VISIBLE);
				
				holder.mDailyDriveTime.setText(DateCalculations.convertMinutesToTimeString(day.getDailyDrivetime()));
				holder.mDailyWorkTime.setText(DateCalculations.convertMinutesToTimeString(day.getTotalWorktime()));
				holder.mDailyDrivenDistance.setText(String.format("%.2f", day.getDailyDrivenDistance()) + " km");
			} else {
				holder.mDailyRestTitle.setText(mWeeklyRest);
				holder.mDailyDriveTime.setVisibility(View.GONE);
				holder.mDailyDriveConatiner.setVisibility(View.GONE);
				holder.mDailyWorkTime.setVisibility(View.GONE);
				holder.mDailyWorkContainer.setVisibility(View.GONE);
				holder.mDailyDrivenDistanceContainer.setVisibility(View.GONE);
			}
			
			if(position == 1 && position == getItemCount() -1){
				holder.mIcon.setType(OverviewCircle.TYPE_NO_BARS);
			} else if(position == 1){
				holder.mIcon.setType(OverviewCircle.TYPE_START);
			} else if(position + 1 == getItemCount()){
				holder.mIcon.setType(OverviewCircle.TYPE_END);
			} else {
				holder.mIcon.setType(OverviewCircle.TYPE_NORMAL);
			}
		}
		
		
		
		private void setChartData(ViewHolderChart holder, WeekHolder week){
			int weeklyDrivingTime = week.getWeeklyDrivetime();
			int weeklyWorkingTime = week.getWeeklyWorktime();
			int weeklyRestingTime = week.getWeeklyrest();
			
			holder.mWeeklyDriveTime.setText(DateCalculations.convertMinutesToTimeString(weeklyDrivingTime));
			holder.mWeeklyWorkTime.setText(DateCalculations.convertMinutesToTimeString(weeklyWorkingTime));
			holder.mWeeklyRestTime.setText(DateCalculations.convertMinutesToTimeString(weeklyRestingTime));
			holder.mWeeklyDrivenDistance.setText(String.format("%.2f", week.getWeeklyDrivenDistance()) + " km");
		}
		
		

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
			if(viewType == VIEW_TYPE_CHART){
				return new ViewHolderChart(mInflater.inflate(R.layout.list_item_week_overview_summary, root, false));
			} else {
				return new ViewHolderItem(mInflater.inflate(R.layout.list_item_week_overview, root, false));
			}
		}
		
		public class ViewHolderChart extends RecyclerView.ViewHolder {
			final RobotoLightTextView mWeeklyDriveTime;
			final RobotoLightTextView mWeeklyWorkTime;
			final RobotoLightTextView mWeeklyRestTime;
			final RobotoLightTextView mWeeklyDrivenDistance;
			
			public ViewHolderChart(View itemView) {
				super(itemView);
				
				mWeeklyDriveTime = (RobotoLightTextView) itemView.findViewById(R.id.working_days_weekly_drive_time);
				mWeeklyWorkTime = (RobotoLightTextView) itemView.findViewById(R.id.working_days_weekly_work_time);
				mWeeklyRestTime = (RobotoLightTextView) itemView.findViewById(R.id.working_days_weekly_rest_time);
				mWeeklyDrivenDistance = (RobotoLightTextView) itemView.findViewById(R.id.working_days_weekly_driven_distance);
			}
			
		}
		
		public class ViewHolderItem extends RecyclerView.ViewHolder {
			final RobotoLightTextView mTitle;
			final RobotoLightTextView mWeekDay;
			final OverviewCircle mIcon;
			
			final RobotoLightTextView mDailyDriveTime;
			final RobotoLightTextView mDailyWorkTime;
			final RobotoLightTextView mDailyRestTime;
			final RobotoLightTextView mDailyRestTitle;
			final RobotoLightTextView mDailyDrivenDistance;
			
			final LinearLayout mDailyDriveConatiner;
			final LinearLayout mDailyWorkContainer;
			final LinearLayout mDailyDrivenDistanceContainer;

			public ViewHolderItem(View itemView) {
				super(itemView);
				
				mTitle = (RobotoLightTextView) itemView.findViewById(R.id.week_overview_title);
				mWeekDay = (RobotoLightTextView) itemView.findViewById(R.id.week_overview_weekday);
				mIcon = (OverviewCircle) itemView.findViewById(R.id.week_overview_bar);
				
				mDailyDriveTime = (RobotoLightTextView) itemView.findViewById(R.id.week_overview_daily_drive_time);
				mDailyWorkTime = (RobotoLightTextView) itemView.findViewById(R.id.week_overview_daily_work_time);
				mDailyRestTime = (RobotoLightTextView) itemView.findViewById(R.id.week_overview_daily_rest_time);
				mDailyRestTitle = (RobotoLightTextView) itemView.findViewById(R.id.week_overview_daily_rest_time_title);
				mDailyDrivenDistance  = (RobotoLightTextView) itemView.findViewById(R.id.week_overview_daily_driven_distance);
				
				mDailyDriveConatiner = (LinearLayout) itemView.findViewById(R.id.week_overview_daily_drive_container);
				mDailyWorkContainer = (LinearLayout) itemView.findViewById(R.id.week_overview_daily_work_container);
				mDailyDrivenDistanceContainer = (LinearLayout) itemView.findViewById(R.id.week_overview_daily_driven_distance_container);
				
			}
			
		}
		
		private class ClickListener implements OnClickListener {
			private final int mPosition;
			
			public ClickListener(int position) {
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
		Intent i = new Intent(this, ActivityDayOverview.class);
		i.putExtra(ActivityDayOverview.INTENT_EXTRA_START, mWeek.getWorkDays().get(position-1).getWorkdayStart());
		i.putExtra(ActivityDayOverview.INTENT_EXTRA_END, mWeek.getWorkDays().get(position-1).getWorkdayEnd());
		
		startActivity(i);
		
	}
}
