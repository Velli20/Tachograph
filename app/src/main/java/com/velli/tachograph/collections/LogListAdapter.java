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

package com.velli.tachograph.collections;

import java.util.ArrayList;

import com.velli.tachograph.Event;
import com.velli.tachograph.R;
import com.velli.tachograph.DateCalculations;
import com.velli.tachograph.views.ListCircle;
import com.velli.tachograph.views.ListCircle.OnCircleSelectedListener;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LogListAdapter extends BaseExpandableListAdapter {
	private final LayoutInflater mInflater;
	private ArrayList<LogListItemGroup> mItemsList;
	private ArrayList<Integer> mSelectedRows;
	private String mExplanations[];
	private int mColors[];
	public OnEventSelectedListener mListener;
	private String mStart;
	private String mEnd;
	private String mRec;
	
	public interface OnEventSelectedListener {
		void onEventSelected(int groupPosition, int childPosition, int dbRowID, boolean selected);
	}
	
	public LogListAdapter(Context context, ArrayList<LogListItemGroup> list){
		mInflater = LayoutInflater.from(context);
		mItemsList = list;
		mColors = context.getResources().getIntArray(R.array.event_colors);
		mExplanations = context.getResources().getStringArray(R.array.event_explanations);
		mStart = context.getString(R.string.title_started_at);
		mEnd = context.getString(R.string.title_ended_at);
		mRec = context.getString(R.string.title_recording_event).toLowerCase();
	}
	
	public void setItemsList(ArrayList<LogListItemGroup> list){
		mItemsList = list;
		notifyDataSetChanged();
	}
	
	public void deselectAllRows(){
		mSelectedRows.clear();
		for(LogListItemGroup group : mItemsList){
			for(Event ev : group.getChildList()){
				ev.setSelected(false);
			}
		}
		notifyDataSetChanged();
	}
	

	
	public ArrayList<Integer> getSelectedItems(){
		return mSelectedRows;
	}
	
	public void setOnEventSelectedListener(OnEventSelectedListener l){
		mListener = l;
	}
	
	public ArrayList<LogListItemGroup> getListItems(){
		return mItemsList;
	}
	
	@Override
	public int getGroupCount() {
		if(mItemsList == null) {
			return 0;
		}
		return mItemsList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if(mItemsList == null || mItemsList.size() == 0){
			return 0;
		}
		return mItemsList.get(groupPosition).getChildList().size();
	}

	@Override
	public LogListItemGroup getGroup(int groupPosition) {
		return mItemsList.get(groupPosition);
	}

	@Override
	public Event getChild(int groupPosition, int childPosition) {
		return getGroup(groupPosition).getChildList().get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		final GroupViewHolder groupHolder;
		final LogListItemGroup group = getGroup(groupPosition);
		
		if(convertView == null){
			groupHolder = new GroupViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_log_group, parent, false);
			groupHolder.mGroupTitle = (TextView)convertView.findViewById(R.id.log_event_group_title);
			groupHolder.mGroupHours = (TextView)convertView.findViewById(R.id.log_event_group_hour_summary);
			convertView.setTag(groupHolder);
		} else {
			groupHolder = (GroupViewHolder)convertView.getTag();
		}
		groupHolder.mGroupTitle.setText(group.getTitle());
		groupHolder.mGroupHours.setText(group.getSummary());

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		final ChildViewHolder holder;
		final Event ev = getChild(groupPosition, childPosition);
		
		if(convertView == null){
			holder = new ChildViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_log_child, parent, false);
			holder.mTitle = (TextView)convertView.findViewById(R.id.log_event_title);
			holder.mHours = (TextView)convertView.findViewById(R.id.log_event_hours);
			holder.mStartTime = (TextView)convertView.findViewById(R.id.log_event_start_time);
			holder.mEndTime = (TextView)convertView.findViewById(R.id.log_event_end_time);
			holder.mIcon = (ListCircle)convertView.findViewById(R.id.log_event_icon);
			holder.mContainer = (RelativeLayout)convertView.findViewById(R.id.log_child_container);
			convertView.setTag(holder);
		} else {
			holder = (ChildViewHolder)convertView.getTag();
		}
		
		holder.mTitle.setText(mExplanations[ev.getEventType()]);
		holder.mStartTime.setText(String.format("%s %s", mStart, DateCalculations.createShortDateTimeString(ev.getStartDateInMillis())));
		
		if(ev.isRecordingEvent()){
			holder.mHours.setText(DateCalculations.convertDatesInHours(ev.getStartDateInMillis(), System.currentTimeMillis()));
			holder.mEndTime.setText(String.format("%s %s", mEnd, mRec));
		} else {
			holder.mHours.setText(DateCalculations.convertDatesInHours(ev.getStartDateInMillis(), ev.getEndDateInMillis()));
			holder.mEndTime.setText(String.format("%s %s", mEnd, DateCalculations.createShortDateTimeString(ev.getEndDateInMillis())));
		}
		holder.mIcon.setCircleBackgroundColor(mColors[ev.getEventType()]);
		holder.mIcon.setCircleText(mExplanations[ev.getEventType()].substring(0, 1));
        holder.mIcon.setSelectable(ev.getRowId() != -1);
        holder.mIcon.setAlpha(ev.getRowId() != -1 ? 1f : 0.54f);

		if(mSelectedRows != null){
			holder.mIcon.setSelected(mSelectedRows.contains(ev.getRowId()));
		}
		holder.mIcon.setOnCircleSelectedListener(new CircleClickListener(childPosition, groupPosition, ev.getRowId()));
		
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	private class CircleClickListener implements OnCircleSelectedListener {
		private final int pos;
		private final int grouppos;
		private final int rowId;
		
		public CircleClickListener(int position, int groupPos, int dpRowId){
			pos = position;
			rowId = dpRowId;
			grouppos = groupPos;
		}

		@Override
		public void onCircleSelected(ListCircle v, boolean selected) {
			if(mSelectedRows == null){
				mSelectedRows = new ArrayList<>();
			}
			if(selected){
				mSelectedRows.add(rowId);
			} else {
				mSelectedRows.remove((Object)rowId);
			}
			getChild(grouppos, pos).setSelected(selected);
			if(mListener != null){
				mListener.onEventSelected(grouppos, pos, rowId, selected);
			}
			
		}
	}
	
	static class GroupViewHolder{
		TextView mGroupTitle;
		TextView mGroupHours;
	}
	
	static class ChildViewHolder{
		TextView mTitle;
		TextView mHours;
		TextView mStartTime;
		TextView mEndTime;
		ListCircle mIcon;
		RelativeLayout mContainer;
	}
	

}
