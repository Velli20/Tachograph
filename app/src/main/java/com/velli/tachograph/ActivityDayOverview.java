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

import java.util.ArrayList;

import com.velli.tachograph.database.DataBaseHandler;
import com.velli.tachograph.database.DataBaseHandler.OnDatabaseEditedListener;
import com.velli.tachograph.database.DataBaseHandler.OnTaskCompleted;
import com.velli.tachograph.restingtimes.BreakAndRestTimeCalculations;
import com.velli.tachograph.restingtimes.BreakAndRestTimeCalculations.OnCalculatiosReadyListener;
import com.velli.tachograph.restingtimes.WeekHolder;
import com.velli.tachograph.restingtimes.WorkDayHolder;
import com.velli.tachograph.views.OverviewCircle;
import com.velli.tachograph.views.RobotoLightTextView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;

public class ActivityDayOverview extends AppCompatActivity implements OnTaskCompleted, OnDatabaseEditedListener {
	public static final String INTENT_EXTRA_START = "start";
	public static final String INTENT_EXTRA_END = "end";
	private RecyclerView mList;
	private DayOverviewAdapter mAdapter;
	
	private long mStart;
	private long mEnd;
	
	private boolean mShowAutomaticallyCalculatedRestingEvents = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_day_overview);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		
		mList = (RecyclerView) findViewById(R.id.activity_day_overview_list);
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
		ab.setTitle(DateCalculations.dateSummary(mStart, mEnd));
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowHomeEnabled(true);
		
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
	
	@Override
	public void onTaskCompleted(final ArrayList<Event> list) {
		if(list != null){
			mAdapter = new DayOverviewAdapter(ActivityDayOverview.this, list);
			mList.setAdapter(mAdapter);
			
			new BreakAndRestTimeCalculations().calculateBreakAndRestingtimes(list, new OnCalculatiosReadyListener() {
				
				@Override
				public void onCalculationsReady(ArrayList<WeekHolder> weeklist) {
					if(weeklist != null && !weeklist.isEmpty() && !weeklist.get(0).getWorkDays().isEmpty()){
						mAdapter.setWorkDayHolder(weeklist.get(0).getWorkDays().get(0));
					} 
				}
			});
		}
		
	}
	
	private class DayOverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private static final int VIEW_TYPE_HEADER = 0;
		private static final int VIEW_TYPE_NORMAL = 1;
		
		private final LayoutInflater mInflater;
		private final ArrayList<Event> mListItems;
		private WorkDayHolder mWorkDay;
		private final String[] mExplanations;
		
		private final int[] mColors;
		
		public DayOverviewAdapter(Context c, ArrayList<Event> items) {
			mInflater = LayoutInflater.from(c);
			mListItems = items;
			mColors = c.getResources().getIntArray(R.array.event_colors);
			mExplanations = c.getResources().getStringArray(R.array.event_explanations);
			setHasStableIds(true);
		}

		public void setWorkDayHolder(WorkDayHolder holder){
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
				((ViewHolderHeader)holder).mDailyDrivingTime.setText(mWorkDay == null ? "--.--" : DateCalculations.convertMinutesToTimeString(mWorkDay.getDailyDrivetime()));
				((ViewHolderHeader)holder).mWtdDailyWorkingTime.setText(mWorkDay == null ? "--.--" : DateCalculations.convertMinutesToTimeString(mWorkDay.getWtdDailyWorkingTime()));
				((ViewHolderHeader)holder).mDailyWorkingTime.setText(mWorkDay == null ? "--.--" : DateCalculations.convertMinutesToTimeString(mWorkDay.getTotalWorktime()));
				((ViewHolderHeader)holder).mDailyRestTime.setText(mWorkDay == null ? "--.--" : DateCalculations.convertMinutesToTimeString(mWorkDay.getDailyRest()));
				((ViewHolderHeader)holder).mDailyDrivenDistance.setText(mWorkDay == null ? "--.--"  : String.format("%.2f", mWorkDay.getDailyDrivenDistance()) + " km");
				return;
			}
			Event ev = mListItems.get(position -1);
			((ViewHolderItem)holder).itemView.setOnClickListener(new ClickListener(position));
			((ViewHolderItem)holder).mTitle.setText(mExplanations[ev.getEventType()]);
			((ViewHolderItem)holder).mStartTime.setText(DateCalculations.createShortTimeString(ev.getStartDateInMillis()));
			
			if(ev.isRecordingEvent()){
				((ViewHolderItem)holder).mHours.setText(DateCalculations.convertDatesInHours(ev.getStartDateInMillis(), System.currentTimeMillis()));
			} else {
				((ViewHolderItem)holder).mHours.setText(DateCalculations.convertDatesInHours(ev.getStartDateInMillis(), ev.getEndDateInMillis()));
			}
			if(position == 1 && getItemCount() == 2){
				((ViewHolderItem)holder).mIcon.setType(OverviewCircle.TYPE_NO_BARS);
			} else if(position == 1){
				((ViewHolderItem)holder).mIcon.setType(OverviewCircle.TYPE_START);
			} else if(position + 1 == getItemCount()){
				
				((ViewHolderItem)holder).mIcon.setType(OverviewCircle.TYPE_END);
			} else {
				((ViewHolderItem)holder).mIcon.setType(OverviewCircle.TYPE_NORMAL);
			}
			((ViewHolderItem)holder).mIcon.setColor(mColors[ev.getEventType()]);
			
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
			if(viewType == VIEW_TYPE_HEADER){
				return new ViewHolderHeader(mInflater.inflate(R.layout.list_item_day_overview_summary, root, false));
			} else {
				return new ViewHolderItem(mInflater.inflate(R.layout.list_item_day_overview, root, false));
			}
		}
		
		public class ViewHolderItem extends RecyclerView.ViewHolder {
			final TextView mTitle;
			final TextView mHours;
			final TextView mStartTime;
			final OverviewCircle mIcon;
			
			public ViewHolderItem(View itemView) {
				super(itemView);
				mTitle = (TextView) itemView.findViewById(R.id.log_event_title);
				mHours = (TextView) itemView.findViewById(R.id.log_event_hours);
				mStartTime = (TextView) itemView.findViewById(R.id.log_event_start_time);
				mIcon = (OverviewCircle) itemView.findViewById(R.id.log_event_icon);
			}
			
		}
		
		public class ViewHolderHeader extends RecyclerView.ViewHolder {
			final RobotoLightTextView mDailyDrivingTime;
			final RobotoLightTextView mWtdDailyWorkingTime;
			final RobotoLightTextView mDailyWorkingTime;
			final RobotoLightTextView mDailyRestTime;
			final RobotoLightTextView mDailyDrivenDistance;
			
			public ViewHolderHeader(View itemView) {
				super(itemView);
				
				mDailyDrivingTime = (RobotoLightTextView) itemView.findViewById(R.id.working_days_daily_drive_time);
				mWtdDailyWorkingTime = (RobotoLightTextView) itemView.findViewById(R.id.working_days_wtd_daily_work_time);
				mDailyWorkingTime = (RobotoLightTextView) itemView.findViewById(R.id.working_days_daily_work_time);
				mDailyRestTime = (RobotoLightTextView) itemView.findViewById(R.id.working_days_daily_rest_time);
				mDailyDrivenDistance = (RobotoLightTextView) itemView.findViewById(R.id.working_days_daily_driven_distance);
			}
		}
		
		public class ClickListener implements OnClickListener {
			private final int mPosition;
			
			public ClickListener(int position){
				mPosition = position;
			}

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ActivityDayOverview.this, ActivityEventDetails.class);
				intent.putExtra(ActivityEventDetails.INTENT_EXTRA_EVENT_ID, mListItems.get(mPosition -1).getRowId());
				startActivity(intent);
				
			}
		}
	}

	@Override
	public void onDatabaseEdited(int action, int rowId) {
		DataBaseHandler.getInstance().getEventsByTime(mStart, mEnd, this, true, mShowAutomaticallyCalculatedRestingEvents);
	}

}
