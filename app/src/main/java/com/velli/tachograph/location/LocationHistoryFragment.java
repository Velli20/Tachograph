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
import com.velli.tachograph.database.DataBaseHandler;
import com.velli.tachograph.database.DataBaseHandler.OnDatabaseEditedListener;
import com.velli.tachograph.database.DataBaseHandler.OnGetLocationsLogListener;
import com.velli.tachograph.views.RobotoLightTextView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class LocationHistoryFragment extends Fragment implements OnDatabaseEditedListener, OnSharedPreferenceChangeListener, OnItemClickListener {
	public static final String TAG = "LocationHistoryFragment ";
	private ListView mList;
	private RobotoLightTextView mNoRoutes;
	
	private ArrayList<LogListItemLocation> mLocationsList;
	private LocationHistoryAdapter mAdapter;
	
	private boolean mSortAscending = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.i(TAG, TAG + "onCreate");
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    	final Resources res = getActivity().getResources();
    	
    	mSortAscending = prefs.getBoolean(res.getString(R.string.preference_key_log_order_asc_desc), false);
    	
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
		final View v = inflater.inflate(R.layout.fragment_location_history, root, false);
		
		mList = (ListView) v.findViewById(R.id.location_history_list);
		mList.setOnItemClickListener(this);
		
		mNoRoutes = (RobotoLightTextView) v.findViewById(R.id.location_history_no_routes);
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		initListItems();
		
	}
	
	@Override
	public void onPause(){
		super.onPause();
		DataBaseHandler.getInstance().unregisterOnDatabaseEditedListener(this);
		PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		DataBaseHandler.getInstance().registerOnDatabaseEditedListener(this);
		PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
	}

	
	private void initListItems() {
		Log.i(TAG, TAG + "initListItems()");
		DataBaseHandler.getInstance().getLocationLog(mSortAscending, new OnGetLocationsLogListener() {
			@Override
			public void onGetLocations(ArrayList<LogListItemLocation> locations) {
				if (locations != null && !locations.isEmpty()) {
					mLocationsList = locations;
					mAdapter = new LocationHistoryAdapter(getActivity(), mLocationsList);
					mList.setAdapter(mAdapter);
					mNoRoutes.setVisibility(View.GONE);
				} else {
					mNoRoutes.setVisibility(View.VISIBLE);
				}
			}
		});
	}


	@Override
	public void onDatabaseEdited(int action, int rowId) {
		if(action == DataBaseHandler.ACTION_DELETE_LOCATION){
			initListItems();
			Log.i(TAG, TAG + "onDatabaseEdited()");
		}
		
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		final Resources res = getActivity().getResources();
       	mSortAscending = sharedPreferences.getBoolean(res.getString(R.string.preference_key_log_order_asc_desc), false);
    	
	}




	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		long startDate = mLocationsList.get(position).getStartDate();
		long endDate = mLocationsList.get(position).getEndDate();
		
		final Bundle b = new Bundle();
		final Intent i = new Intent(getActivity(), ActivityRouteInfo.class);
		
		b.putLong(ActivityRouteInfo.INTENT_EXTRA_START_DAY, startDate);
		b.putLong(ActivityRouteInfo.INTENT_EXTRA_END_DAY, endDate);
		
		i.putExtras(b);
		
		getActivity().startActivity(i);
		
	}
	
}
