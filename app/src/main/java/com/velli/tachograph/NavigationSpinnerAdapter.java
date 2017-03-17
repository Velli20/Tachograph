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

import com.velli.tachograph.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class NavigationSpinnerAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private String mTitles[];

	public NavigationSpinnerAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		mTitles = context.getResources().getStringArray(R.array.navigation_drawer_titles);
	}

	@Override
	public int getCount() {
		return mTitles.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		ViewHolderDropDown holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.nav_spinner_item_dropdown, parent,
					false);
			holder = new ViewHolderDropDown();
			holder.mTitle = (TextView) convertView
					.findViewById(R.id.spinner_item_title);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolderDropDown) convertView.getTag();
		}
		holder.mTitle.setText(mTitles[position]);

		return convertView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.nav_spinner_item, parent,
					false);
			holder = new ViewHolder();
			holder.mTitle = (TextView) convertView
					.findViewById(R.id.spinner_item_title);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mTitle.setText(mTitles[position]);

		return convertView;
	}

	static class ViewHolder {
		TextView mTitle;
	}

	static class ViewHolderDropDown {
		TextView mTitle;
	}

}
