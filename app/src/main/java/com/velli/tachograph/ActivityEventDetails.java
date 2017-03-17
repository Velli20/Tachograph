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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import com.velli.tachograph.database.DataBaseHandler;
import com.velli.tachograph.database.DataBaseHandler.OnGetEventTaskCompleted;
import com.velli.tachograph.database.GetLocationInfo.OnGetLocationInfoListener;
import com.velli.tachograph.location.ActivityRouteInfo;
import com.velli.tachograph.location.RouteActivity;
import com.velli.tachograph.location.RouteInfo;
import com.velli.tachograph.views.RobotoLightTextView;
import com.velli.tachograph.views.StaticSpeedChart;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class ActivityEventDetails extends ActivityScrollableMapHeader implements OnGetLocationInfoListener {
	public static final String INTENT_EXTRA_EVENT_ID = "intent extra event id";
	
	private RobotoLightTextView mDate;
	private RobotoLightTextView mDuration;
	private RobotoLightTextView mStartLocation;
	private RobotoLightTextView mEndLocation;
	private RobotoLightTextView mNote;
	private RobotoLightTextView mMileage;
	private RobotoLightTextView mAverageSpeed;

	private StaticSpeedChart mChart;
	private ProgressBar mProgress;
	private LinearLayout mContainer;
	
	private Event mEvent;
    private RouteInfo mInfo;
	
	private int mEventId = -1;
    private int mColorBlue;
	
    private float mDensity;
    
	 @Override
	 public void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
		 setContent(R.layout.activity_event_details);
		 
		 final Resources res = getResources();
		 
		 mDensity = res.getDisplayMetrics().density;
		 mColorBlue = res.getColor(R.color.color_primary_400);
		 
		 mDate = (RobotoLightTextView) findViewById(R.id.event_details_date);
		 mDuration = (RobotoLightTextView) findViewById(R.id.event_details_duration);
		 mStartLocation = (RobotoLightTextView) findViewById(R.id.event_details_start_location);
		 mEndLocation = (RobotoLightTextView) findViewById(R.id.event_details_end_location);
		 mNote = (RobotoLightTextView) findViewById(R.id.event_details_note);
		 mMileage = (RobotoLightTextView) findViewById(R.id.event_details_milage);
		 mAverageSpeed = (RobotoLightTextView) findViewById(R.id.event_details_average_speed);
		 
		 mProgress = (ProgressBar) findViewById(R.id.event_details_progress);
		 mContainer = (LinearLayout) findViewById(R.id.event_details_container);

		 mChart = (StaticSpeedChart) findViewById(R.id.event_details_speed_graph);
		 
		 final Intent intent = getIntent();
		 
		 if(intent != null && intent.getExtras() != null){
			 mEventId = intent.getExtras().getInt(INTENT_EXTRA_EVENT_ID, -1);
		 }
		 
		 final ActionBar ab = getSupportActionBar();
	     ab.setDisplayShowHomeEnabled(true);
	     ab.setDisplayHomeAsUpEnabled(true);
	     ab.setDisplayShowTitleEnabled(false); 
	     
	     if(mEvent != null) {
	    	 mContainer.setVisibility(View.VISIBLE);
	    	 mProgress.setVisibility(View.GONE);
	     }

	 }
	 
	 @Override
	 public void onResume(){
		 super.onResume();
		 getEventData(mEventId);
	 }
	 
	 private void getEventData(int eventId){
		 if(eventId == -1){
			 return;
		 }
		 
		 DataBaseHandler.getInstance().getEvent(eventId, new OnGetEventTaskCompleted() {
			
			@Override
			public void onGetEvent(Event ev) {
				if(ev != null){
					setData(ev);
				} else {
					finish();
				}
				
			}
		}, false);
	 }
	 
	 private void setProgressVisible(boolean visible) {
		 mContainer.setVisibility(visible ? View.GONE : View.VISIBLE);
	     mProgress.setVisibility(visible? View.VISIBLE : View.GONE);	     
	 }
	
	 private void setData(Event ev) {
		 mEvent = ev;
		 if(ev == null){
			 return;
		 }
		 
		 if(!ev.hasLoggedRoute()) {
			 setMapVisibility(false);
			 mAverageSpeed.setVisibility(View.GONE);
             mChart.setVisibility(View.GONE);
		 } else {
			 DataBaseHandler.getInstance().getLocationInfo(-1, -1, ev.getRowId(), (int) mDensity * 2, mColorBlue, this);
		 }
		 
		 invalidateOptionsMenu();
		 String[] events = getResources().getStringArray(R.array.event_explanations);
		 int[] colors = getResources().getIntArray(R.array.event_colors);
		 
		 setOverlayColor(colors[ev.getEventType()]);
		 setProgressVisible(false);
		 

		 if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			 getWindow().setStatusBarColor(getStatusbarHue(colors[ev.getEventType()]));
		 }
		 
		 setTitle((events[ev.getEventType()]));
		 mDate.setText(DateCalculations.createDateTimeString(ev.getStartDateInMillis(), ev.getEndDateInMillis()));
		 mDuration.setText(DateCalculations.convertDatesInHours(ev.getStartDateInMillis(), ev.getEndDateInMillis()));
		 
		 if(ev.getStartLocation() != null && !ev.getStartLocation().isEmpty()){
			 mStartLocation.setText(ev.getStartLocation());
			 mStartLocation.setVisibility(View.VISIBLE);
		 } else {
			 mStartLocation.setVisibility(View.GONE);
		 }
		 
		 if(ev.getEndLocation() != null && !ev.getEndLocation().isEmpty()){
			 mEndLocation.setText(ev.getEndLocation());
			 mEndLocation.setVisibility(View.VISIBLE);
		 } else {
			 mEndLocation.setVisibility(View.GONE);
		 }
		 

		 if(ev.getNote() != null && !ev.getNote().isEmpty()){
			 mNote.setText(ev.getNote());
			 mNote.setVisibility(View.VISIBLE);
		 } else {
			 mNote.setVisibility(View.GONE);
		 }
		 
		 if(ev.getMileageStart() == 0 || ev.getMileageEnd() == 0 || ev.getEventType() != Event.EVENT_TYPE_DRIVING) {
			 mMileage.setVisibility(View.GONE);
		 } else {
			 mMileage.setVisibility(View.VISIBLE);
			 mMileage.setText(formatValueMileage(ev.getMileageStart()) + " km -\n" + formatValueMileage(ev.getMileageEnd()) + " km");
		 }
	 }

	
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		if(mEvent != null) {
			menu.findItem(R.id.menu_open_full_map).setVisible(mEvent.hasLoggedRoute());
		} else {
			menu.findItem(R.id.menu_open_full_map).setVisible(false);
		}
		
		addMenuItemToTint(menu.findItem(android.R.id.home));
		addMenuItemToTint(menu.findItem(R.id.menu_item_edit));
		addMenuItemToTint(menu.findItem(R.id.menu_open_full_map));
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.menu_activity_event_details, menu);
		return super.onCreateOptionsMenu(menu);
	}
	 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		
		case android.R.id.home:
			finish();
			return true;
			
		case R.id.menu_item_edit:
			Intent intent = new Intent(this, ActivityAddNewEvent.class);
			intent.putExtra(ActivityAddNewEvent.INTENT_EXTRA_DISPLAY_MODE, ActivityAddNewEvent.DISPLAY_MODE_EDIT_OR_DELETE);
			intent.putExtra(ActivityAddNewEvent.INTENT_EXTRA_EVENT_ROW_ID, mEventId);
			startActivity(intent);
			return true;
			
		case R.id.menu_open_full_map:
            Intent i = new Intent(this, RouteActivity.class);
            i.putExtra(RouteActivity.EXTRAS_LOCATION_EVENT_ID, mEventId);
            if(mInfo != null) {
                i.putExtra(RouteActivity.EXTRAS_TITLE, String.format("%.2f", mInfo.getDistance()) + " km");
            }

			startActivity(i);
			return true;
		}
		
		
		
		return super.onOptionsItemSelected(item);
	}
	
	private int getStatusbarHue(int color){
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.8f; // value component
		color = Color.HSVToColor(hsv);
		return color;
	}

	 
	private static String formatValueMileage(int value){
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
		symbols.setGroupingSeparator(' ');

		DecimalFormat formatter = new DecimalFormat("###,###.##", symbols);
		return formatter.format(value);
	}

	@Override
	public void onGetLocationInfo(RouteInfo info) {
        mInfo = info;
		if(info != null) {
			setMapPolyLine(info.getMapPolyline());
			mAverageSpeed.setText(getResources().getString(R.string.title_average_speed) + " " + String.valueOf((int)info.getAverageSpeed()) + " km/h");
            if(info.getSpeedGraphLine() != null) {
                mChart.setLine(info.getSpeedGraphLine());
            }
		}
		
	}


	 
}
