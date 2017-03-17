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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import com.velli.tachograph.R;
import com.velli.tachograph.restingtimes.RegulationTimesSummary;
import com.velli.tachograph.restingtimes.WeekHolder;
import com.velli.tachograph.restingtimes.WorkDayHolder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FragmentWorkingDays extends Fragment implements OnGroupClickListener {
	public static final boolean DEBUG = true;
	public static final String TAG = "FragmentWorkingDays ";
	private RecyclerView mList;
	private WeekOverviewAdapter mListAdapter;
	private ArrayList<WeekHolder> mWeeklist;
	private String mWeek = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		final Resources res = getResources();
		mWeeklist = RegulationTimesSummary.getInstance().getWeekList();
		mWeek = res.getString(R.string.title_week);
		
		if(mWeeklist != null){
			mListAdapter = new WeekOverviewAdapter(getActivity(), mWeeklist);
			mListAdapter.setOnGroupClickListener(this);
		}
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_log_summary_layout, container, false);

		mList = (RecyclerView) v.findViewById(R.id.working_days_fragment_expandableListView);

		if(mWeeklist == null || mWeeklist.size() == 0){
			LinearLayout noItems = (LinearLayout)v.findViewById(R.id.working_days_fragment_no_items);
			noItems.setVisibility(View.VISIBLE);
		} else {
			mList.setVisibility(View.VISIBLE);
		}
		if(mListAdapter != null){
			mListAdapter.setOnGroupClickListener(this);
			mList.setAdapter(mListAdapter);
		}
		mList.setLayoutManager(new LinearLayoutManager(getActivity()));
		return v;
	}

	
	
	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		if(DEBUG) {
			Log.i(TAG, TAG + "onGroupClick(groupPosition " + groupPosition + ")");
		}
		
		Intent i = new Intent(getActivity(), ActivityWeekOverview.class);
		i.putExtra(ActivityWeekOverview.INTENT_EXTRA_START, mWeeklist.get(groupPosition).getWeekStart());
		i.putExtra(ActivityWeekOverview.INTENT_EXTRA_END, mWeeklist.get(groupPosition).getWeekEnd());
		
		getActivity().startActivity(i);
		return false;
	}


	
	public class WeekOverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private final LayoutInflater mInflater;
		private ArrayList<WeekHolder> mItems;
		private int mItemCount = 0;
		
		
		private WeakReference<OnGroupClickListener> mGroupClickListener;
		
		public WeekOverviewAdapter(Context c, ArrayList<WeekHolder> list){
			mInflater = LayoutInflater.from(c);
			mItems = list;
			mItemCount = mItems.size();
		}
		
		public void setOnGroupClickListener(OnGroupClickListener l) {
			mGroupClickListener = new WeakReference<>(l);
		}
		

		public WorkDayHolder getWorkDayItem(int groupPosition, int childPosition){
			return mItems.get(groupPosition).getWorkdaysList().get(childPosition);
		} 
		
		@Override
		public int getItemCount() {
			return mItemCount;
		}


		
		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			
			holder.itemView.setOnClickListener(new ClickListener(position));
			setWeekData(((ViewHolderWeek)holder), position);
			
			
		}

	
		
		private void setWeekData(ViewHolderWeek groupHolder, int position) {
			WeekHolder weekHolder = mItems.get(position);
			
			groupHolder.mTitle.setText(mWeek + " " + DateCalculations.getWeekOfYear(weekHolder.getWeekStart()));
			groupHolder.mWeeklyDate.setText(DateCalculations.dateSummary(weekHolder.getWeekStart(), weekHolder.getWeekEnd()));
			groupHolder.mWeeklyDriveTime.setText(DateCalculations.convertMinutesToTimeString(weekHolder.getWeeklyDrivetime()));
			groupHolder.mWeeklyWorkTime.setText(DateCalculations.convertMinutesToTimeString(weekHolder.getWeeklyWorktime()));
			groupHolder.mWeeklyRestTime.setText(DateCalculations.convertMinutesToTimeString(weekHolder.getWeeklyrest()));
			groupHolder.mWtdWeeklyRestTime.setText(DateCalculations.convertMinutesToTimeString(weekHolder.getWtdWeeklyRestingTime()));		
			groupHolder.mWtdWeeklyWorkTime.setText(DateCalculations.convertMinutesToTimeString(weekHolder.geWtdtWeeklyWorkingTime()));
	
		}
		
	
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
			return new ViewHolderWeek(mInflater.inflate(R.layout.list_item_work_week, root, false));
			
		}
		
		public class ViewHolderWeek extends RecyclerView.ViewHolder {
			protected TextView mTitle;
			protected TextView mWeeklyDate;
			protected TextView mWeeklyDriveTime;
			protected TextView mWeeklyWorkTime;
			protected TextView mWeeklyRestTime;
			protected TextView mWtdWeeklyRestTime;
			protected TextView mWtdWeeklyWorkTime;
			protected View mDivider;
			
			public ViewHolderWeek(View itemView) {
				super(itemView);
				mTitle = (TextView) itemView.findViewById(R.id.working_days_group_title);
				mWeeklyDate = (TextView) itemView.findViewById(R.id.working_days_group_date);
				mWeeklyDriveTime = (TextView) itemView.findViewById(R.id.working_days_weekly_drive_time);
				mWeeklyWorkTime = (TextView) itemView.findViewById(R.id.working_days_weekly_work_time);
				mWeeklyRestTime = (TextView) itemView.findViewById(R.id.working_days_weekly_rest_time);
				mWtdWeeklyRestTime = (TextView) itemView.findViewById(R.id.working_days_wtd_weekly_rest_time);
				mWtdWeeklyWorkTime = (TextView) itemView.findViewById(R.id.working_days_wtd_weekly_work_time);
				mDivider = itemView.findViewById(R.id.working_days_child_divider);
			}
			
		}
		

		
		public class ClickListener implements OnClickListener {
			private int mGroupPosition;
			
			public ClickListener(int groupPosition){
				mGroupPosition = groupPosition;
			}
			
			@Override
			public void onClick(View v) {
				if(mGroupClickListener != null && mGroupClickListener.get() != null) {
					mGroupClickListener.get().onGroupClick(null, v, mGroupPosition, -1);
				} 
				
			}
			
		}
		
	}

}
