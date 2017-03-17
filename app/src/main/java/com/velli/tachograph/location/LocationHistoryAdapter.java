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

package com.velli.tachograph.location;

import java.util.ArrayList;

import com.velli.tachograph.R;
import com.velli.tachograph.DateCalculations;
import com.velli.tachograph.views.ListCircle;
import com.velli.tachograph.views.RobotoLightTextView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class LocationHistoryAdapter extends BaseAdapter {
	private final LayoutInflater mInflater;
	private final ArrayList<LogListItemLocation> mLogItems;
	
	public LocationHistoryAdapter(Context context, ArrayList<LogListItemLocation> list){
		mInflater = LayoutInflater.from(context);
		mLogItems = list;
	}
	
	@Override
	public int getCount() {
		if(mLogItems == null){
			return 0;
		} else {
			return mLogItems.size();
		}
	}



	@Override
	public LogListItemLocation getItem(int position) {
		return mLogItems.get(position);
	}

	

	@Override
	public View getView(int groupPosition, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final LogListItemLocation loc = getItem(groupPosition);
		
		if(convertView == null){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_location_log, parent, false);
			holder.start = (RobotoLightTextView)convertView.findViewById(R.id.list_item_location_start_time);
			holder.duration = (RobotoLightTextView)convertView.findViewById(R.id.list_item_location_duration);
			holder.distance = (RobotoLightTextView)convertView.findViewById(R.id.list_item_location_distance);
			holder.icon = (ListCircle)convertView.findViewById(R.id.log_event_icon);
			holder.icon.setSelectable(false);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		int mins = DateCalculations.convertDatesToMinutes(loc.getStartDate(), loc.getEndDate());

		holder.start.setText(DateCalculations.createDateTimeString(loc.getStartDate()));
		holder.duration.setText(DateCalculations.convertMinutesToTimeString(mins));
		
		if(loc.getDistanceInMeters() < 1000){
			holder.distance.setText(String.valueOf(Math.round(loc.getDistanceInMeters())) + " m");
		} else {
			holder.distance.setText(String.valueOf(Math.round(loc.getDistanceInMeters() / 1000)) + " km");
		}
		
		return convertView;
	}


	static class ViewHolder {
		RobotoLightTextView start;
		RobotoLightTextView duration;
		RobotoLightTextView distance;
		ListCircle icon;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}


}

