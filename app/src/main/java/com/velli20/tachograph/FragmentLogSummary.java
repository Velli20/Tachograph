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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

import com.velli20.tachograph.collections.SpacesItemDecorationEventDetails;
import com.velli20.tachograph.database.DataBaseHandler;
import com.velli20.tachograph.database.GetLogSummaryTask;
import com.velli20.tachograph.restingtimes.WeekHolder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

public class FragmentLogSummary extends Fragment implements OnGroupClickListener {
	public static final boolean DEBUG = false;
	public static final String TAG = "FragmentLogSummary ";
	private RecyclerView mList;
    private LinearLayout mNoItemsView;

    private WeekOverviewAdapter mListAdapter;
	private ArrayList<WeekHolder> mWorkingTimes;
	private String mWeek = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		final Resources res = getResources();
        mWeek = res.getString(R.string.title_week);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_log_summary_layout, container, false);

        mList = (RecyclerView) v.findViewById(R.id.working_days_fragment_expandableListView);
		mList.addItemDecoration(new SpacesItemDecorationEventDetails(getResources().getDimensionPixelSize(R.dimen.list_item_log_summary_week_divider_height)));

        mNoItemsView = (LinearLayout)v.findViewById(R.id.working_days_fragment_no_items);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		DataBaseHandler.getInstance().getWorkingTimes(new GetLogSummaryTask.OnWorkingTimeCalculationsReadyListener() {
			@Override
			public void onWorkingTimeCalculationsReady(ArrayList<WeekHolder> workingWeeks) {
				mWorkingTimes = workingWeeks;
				initializeList();
			}
		}, true, true, true, true);
	}

    private void initializeList() {
        if(mWorkingTimes != null && !mWorkingTimes.isEmpty()){
            mListAdapter = new WeekOverviewAdapter(getActivity(), mWorkingTimes);
            mListAdapter.setOnGroupClickListener(FragmentLogSummary.this);

            mList.setLayoutManager(new LinearLayoutManager(getActivity()));
            mList.setAdapter(mListAdapter);
            mList.setVisibility(View.VISIBLE);
            mNoItemsView.setVisibility(View.GONE);
        } else {
            mList.setVisibility(View.GONE);
            mNoItemsView.setVisibility(View.VISIBLE);
        }
    }
	
	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		if(DEBUG) {
			Log.i(TAG, TAG + "onGroupClick(groupPosition " + groupPosition + ")");
		}
		ArrayList<Integer> eventIds = mWorkingTimes.get(groupPosition).getEventIds();
        if(eventIds != null) {
            Intent i = new Intent(getActivity(), ActivityLogSummaryWeek.class);

            i.putExtra(ActivityLogSummaryWeek.INTENT_START_DATE, mWorkingTimes.get(groupPosition).getStartDate());
            i.putIntegerArrayListExtra(ActivityLogSummaryWeek.INTENT_EVENT_IDS, eventIds);

            getActivity().startActivity(i);
        }
		return false;
	}


	
	private class WeekOverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private final LayoutInflater mInflater;
		private ArrayList<WeekHolder> mItems;
		private int mItemCount = 0;
		private WeakReference<OnGroupClickListener> mGroupClickListener;
        private Drawable mElevationTop;

        private WeekOverviewAdapter(Context c, ArrayList<WeekHolder> list){
			mInflater = LayoutInflater.from(c);
			mItems = list;
			mItemCount = mItems.size();
            mElevationTop = c.getResources().getDrawable(R.drawable.dropdown_shadow_up);
		}

        private void setOnGroupClickListener(OnGroupClickListener l) {
			mGroupClickListener = new WeakReference<>(l);
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
			if(weekHolder == null || groupHolder == null) {
                return;
            }
			groupHolder.mTitle.setText(mWeek + " " + DateUtils.getWeekOfYear(weekHolder.getStartDate()));
			groupHolder.mWeeklyDate.setText(DateUtils.dateSummary(weekHolder.getStartDate(), weekHolder.getEndDate()));
			groupHolder.mWeeklyDriveTime.setText(DateUtils.convertMinutesToTimeString(weekHolder.getWeeklyDrivingTime()));
			groupHolder.mWeeklyWorkTime.setText(DateUtils.convertMinutesToTimeString(weekHolder.getWeeklyWorkingTime()));
			groupHolder.mWeeklyRestTime.setText(DateUtils.convertMinutesToTimeString(weekHolder.getWeeklyRest()));
			groupHolder.mWtdWeeklyWorkTime.setText(DateUtils.convertMinutesToTimeString(weekHolder.getWtdWeeklyWorkingTime()));
            groupHolder.mDrivenDistance.setText(String.format(Locale.getDefault(), "%.2f", weekHolder.getWeeklyDrivenDistance()) + " km");
            if(position == 0) {
                groupHolder.mShadowTop.setBackgroundColor(Color.WHITE);
            } else {
                groupHolder.mShadowTop.setBackgroundDrawable(mElevationTop);
            }
		}
		
	
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
			return new ViewHolderWeek(mInflater.inflate(R.layout.list_item_log_summary_week, root, false));
			
		}
		
		private class ViewHolderWeek extends RecyclerView.ViewHolder {
			private TextView mTitle;
			private TextView mWeeklyDate;
			private TextView mWeeklyDriveTime;
			private TextView mWeeklyWorkTime;
			private TextView mWeeklyRestTime;
			private TextView mWtdWeeklyWorkTime;
			private TextView mDrivenDistance;
			private View mShadowTop;
            private View mBackground;

			private ViewHolderWeek(View itemView) {
				super(itemView);
				mTitle = (TextView) itemView.findViewById(R.id.list_item_log_summary_week_title_week_of_year);
				mWeeklyDate = (TextView) itemView.findViewById(R.id.list_item_log_summary_week_date);
				mWeeklyDriveTime = (TextView) itemView.findViewById(R.id.list_item_log_summary_week_weekly_driving_time);
				mWeeklyWorkTime = (TextView) itemView.findViewById(R.id.list_item_log_summary_week_weekly_working_time);
				mWeeklyRestTime = (TextView) itemView.findViewById(R.id.list_item_log_summary_week_weekly_rest_time);
				mWtdWeeklyWorkTime = (TextView) itemView.findViewById(R.id.list_item_log_summary_week_wtd_weekly_working_time);
                mDrivenDistance = (TextView) itemView.findViewById(R.id.list_item_log_summary_week_driven_distance);
                mShadowTop = itemView.findViewById(R.id.list_item_log_summary_week_shadow_top);
                mBackground = itemView.findViewById(R.id.list_item_log_summary_week_background);

			}
		}



		private class ClickListener implements OnClickListener {
			private int mGroupPosition;

			private ClickListener(int groupPosition){
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
