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

package com.velli20.tachograph.collections;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.velli20.tachograph.DateUtils;
import com.velli20.tachograph.Event;
import com.velli20.tachograph.R;
import com.velli20.tachograph.views.ListCircle;
import com.velli20.tachograph.views.ListCircle.OnCircleSelectedListener;

import java.util.ArrayList;
import java.util.Locale;

public class ListAdapterFragmentLog extends BaseExpandableListAdapter {
    private final LayoutInflater mInflater;
    public OnEventSelectedListener mListener;
    private ArrayList<ListItemLogGroup> mItemsList;
    private ArrayList<Integer> mSelectedRows;
    private String mExplanations[];
    private int mColors[];
    private String mStart;
    private String mEnd;
    private String mRec;
    private String mDistance;

    public ListAdapterFragmentLog(Context context, ArrayList<ListItemLogGroup> list) {
        Resources res = context.getResources();

        mInflater = LayoutInflater.from(context);
        mItemsList = list;
        mColors = res.getIntArray(R.array.event_colors);
        mExplanations = res.getStringArray(R.array.events);
        mStart = res.getString(R.string.title_started_at);
        mEnd = res.getString(R.string.title_ended_at);
        mRec = res.getString(R.string.title_recording_event).toLowerCase();
        mDistance = res.getString(R.string.log_summary_driven_distance);

    }

    public void setItemsList(ArrayList<ListItemLogGroup> list) {
        mItemsList = list;
        notifyDataSetChanged();
    }

    public void deselectAllRows() {
        mSelectedRows.clear();
        notifyDataSetChanged();
    }

    public ArrayList<Integer> getSelectedItems() {
        return mSelectedRows;
    }

    public void setOnEventSelectedListener(OnEventSelectedListener l) {
        mListener = l;
    }

    public ArrayList<ListItemLogGroup> getListItems() {
        return mItemsList;
    }

    @Override
    public int getGroupCount() {
        if (mItemsList == null) {
            return 0;
        }
        return mItemsList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (mItemsList == null || mItemsList.size() == 0) {
            return 0;
        }
        return mItemsList.get(groupPosition).getChildList().size();
    }

    @Override
    public ListItemLogGroup getGroup(int groupPosition) {
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
        final ListItemLogGroup group = getGroup(groupPosition);

        if (convertView == null) {
            groupHolder = new GroupViewHolder();
            convertView = mInflater.inflate(R.layout.list_item_log_group, parent, false);
            groupHolder.mGroupTitle = (TextView) convertView.findViewById(R.id.log_event_group_title);
            groupHolder.mGroupHours = (TextView) convertView.findViewById(R.id.log_event_group_hour_summary);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupViewHolder) convertView.getTag();
        }
        groupHolder.mGroupTitle.setText(group.getTitle());
        groupHolder.mGroupHours.setText(group.getSummary());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ChildViewHolder holder;
        final Event ev = getChild(groupPosition, childPosition);

        if (convertView == null) {
            holder = new ChildViewHolder();
            convertView = mInflater.inflate(R.layout.list_item_log_child, parent, false);
            holder.mTitle = (TextView) convertView.findViewById(R.id.list_item_log_child_title);
            holder.mHours = (TextView) convertView.findViewById(R.id.list_item_log_child_duration);
            holder.mStartTime = (TextView) convertView.findViewById(R.id.list_item_log_child_start_time);
            holder.mEndTime = (TextView) convertView.findViewById(R.id.list_item_log_child_end_time);
            holder.mIcon = (ListCircle) convertView.findViewById(R.id.list_item_log_child_icon);
            holder.mContainer = (RelativeLayout) convertView.findViewById(R.id.log_child_container);
            holder.mDistance = (TextView) convertView.findViewById(R.id.list_item_log_child_distance);
            convertView.setTag(holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }

        holder.mTitle.setText(mExplanations[ev.getEventType()]);
        holder.mStartTime.setText(String.format("%s %s", mStart, DateUtils.createShortDateTimeString(ev.getStartDateInMillis())));

        if (ev.isRecordingEvent()) {
            holder.mHours.setText(DateUtils.convertDatesInHours(ev.getStartDateInMillis(), System.currentTimeMillis()));
            holder.mEndTime.setText(String.format("%s %s", mEnd, mRec));
        } else {
            holder.mHours.setText(DateUtils.convertDatesInHours(ev.getStartDateInMillis(), ev.getEndDateInMillis()));
            holder.mEndTime.setText(String.format("%s %s", mEnd, DateUtils.createShortDateTimeString(ev.getEndDateInMillis())));
        }
        if (ev.hasLoggedRoute()) {
            holder.mDistance.setVisibility(View.VISIBLE);
            holder.mDistance.setText(String.format(Locale.getDefault(), "%s %.2f km", mDistance, (float) ev.getDrivenDistance()));
        } else {
            holder.mDistance.setVisibility(View.GONE);
        }
        holder.mIcon.setCircleBackgroundColor(mColors[ev.getEventType()]);
        holder.mIcon.setCircleText(mExplanations[ev.getEventType()].substring(0, 1));
        holder.mIcon.setSelectable(ev.getRowId() != -1);
        holder.mIcon.setAlpha(ev.getRowId() != -1 ? 1f : 0.54f);

        if (mSelectedRows != null) {
            holder.mIcon.setSelected(mSelectedRows.contains(ev.getRowId()));
        }
        holder.mIcon.setOnCircleSelectedListener(new CircleClickListener(childPosition, groupPosition, ev.getRowId()));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public interface OnEventSelectedListener {
        void onEventSelected(int groupPosition, int childPosition, int dbRowID, boolean selected);
    }

    private class CircleClickListener implements OnCircleSelectedListener {
        private final int mPosition;
        private final int mGroupPosition;
        private final int mRowId;

        private CircleClickListener(int position, int groupPos, int dpRowId) {
            mPosition = position;
            mRowId = dpRowId;
            mGroupPosition = groupPos;
        }

        @Override
        public void onCircleSelected(ListCircle v, boolean selected) {
            if (mSelectedRows == null) {
                mSelectedRows = new ArrayList<>();
            }
            if (selected) {
                mSelectedRows.add(mRowId);
            } else {
                mSelectedRows.remove((Integer) mRowId);
            }
            if (mListener != null) {
                mListener.onEventSelected(mGroupPosition, mPosition, mRowId, selected);
            }

        }
    }

    private class GroupViewHolder {
        TextView mGroupTitle;
        TextView mGroupHours;
    }

    private class ChildViewHolder {
        TextView mTitle;
        TextView mHours;
        TextView mStartTime;
        TextView mEndTime;
        TextView mDistance;
        ListCircle mIcon;
        RelativeLayout mContainer;
    }


}
