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

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.velli.tachograph.R;
import com.velli.tachograph.DateCalculations;
import com.velli.tachograph.Utils;
import com.velli.tachograph.database.DataBaseHandler;
import com.velli.tachograph.database.DataBaseHandler.OnGetLocationsListener;
import com.velli.tachograph.location.GetAdressTask.OnGetAdressListener;
import com.velli.tachograph.views.Line;
import com.velli.tachograph.views.LinePoint;
import com.velli.tachograph.views.RobotoLightTextView;
import com.velli.tachograph.views.StaticSpeedChart;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;



public class ActivityRouteInfo extends AppCompatActivity implements ObservableScrollViewCallbacks, OnGetLocationsListener, OnMapReadyCallback {
	private static final float MAX_TEXT_SCALE_DELTA = 0.3f;
	private static final String TAG = "ActivityRouteInfo ";
	
	public static final String INTENT_EXTRA_START_DAY = "start day";
	public static final String INTENT_EXTRA_END_DAY = "end day";
	public static final String INTENT_EXTRA_EVENT_ID = "event id";
	
    private MapView mMapView;
    private GoogleMap mMap;
    private StaticSpeedChart mGraph;
    private RouteInfo mInfo;

	private RobotoLightTextView mAverageSpeed;
	private RobotoLightTextView mDuration;
	private RobotoLightTextView mStartLocation;
	private RobotoLightTextView mEndLocation;
    
    private View mOverlayView;
    private View mToolbarShadow;
    private TextView mTitleView;
    private ObservableScrollView mRecyclerView;
    
    private int mActionBarSize;
    private int mActionBarHeight;
    private int mContentInset;
    private int mFlexibleSpaceImageHeight;
    private int mToolbarColor;
    private int mEventId = -1;
    private int mColorBlue;
    
    private long mStart;
    private long mEnd;
    
    private float mDensity;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_info);
        MapsInitializer.initialize(this);
        
        Log.i(TAG, TAG + "onCreate()");
        
        final Resources res = getResources();
        mDensity = res.getDisplayMetrics().density;
        mToolbarColor = res.getColor(R.color.white);
        mFlexibleSpaceImageHeight = res.getDimensionPixelSize(R.dimen.list_header_map_height);
        mActionBarHeight = res.getDimensionPixelSize(R.dimen.toolbar_height);
        mContentInset = res.getDimensionPixelSize(R.dimen.toolbar_content_inset_route_info);
        mColorBlue = res.getColor(R.color.color_primary_400);
        
        mActionBarSize = getActionBarSize();

        mRecyclerView = (ObservableScrollView) findViewById(R.id.recycler);
        mRecyclerView.setScrollViewCallbacks(this);
        
        
        mMapView = (MapView)findViewById(R.id.list_item_map);
		mMapView.setClickable(true);
		mMapView.onCreate(null);
		mMapView.getMapAsync(this);
		
        mOverlayView = findViewById(R.id.overlay);
        mToolbarShadow = findViewById(R.id.toolbar_shadow);

        mTitleView = (TextView) findViewById(R.id.title);
        mTitleView.setText(getTitle());
        
        mStartLocation = (RobotoLightTextView) findViewById(R.id.route_info_start_place);
        mEndLocation = (RobotoLightTextView) findViewById(R.id.route_info_end_place);
        mDuration = (RobotoLightTextView) findViewById(R.id.route_info_duration);
        mAverageSpeed = (RobotoLightTextView) findViewById(R.id.route_info_average_speed);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mGraph = (StaticSpeedChart) findViewById(R.id.route_info_speed_graph);
        
        setTitle(null);
        setSupportActionBar(mToolbar);
        
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        
        ScrollUtils.addOnGlobalLayoutListener(mRecyclerView, new Runnable() {
        	@Override
            public void run() {
            	mRecyclerView.scrollTo(0, mFlexibleSpaceImageHeight - mActionBarSize);
            }
        });
        
        final Bundle b = getIntent().getExtras();
        if(b != null){
        	mStart = b.getLong(INTENT_EXTRA_START_DAY, -1);
        	mEnd = b.getLong(INTENT_EXTRA_END_DAY, -1);
        	mEventId = b.getInt(INTENT_EXTRA_EVENT_ID, -1);
        	
        	initListItems(mStart, mEnd, mEventId);
        }
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.menu_activity_route_info, menu);
	    return super.onCreateOptionsMenu(menu);
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_item_discard:
			showDeleteDialog();
			return true;
			
		case R.id.menu_open_full_map:
			Intent i = new Intent(this, RouteActivity.class);
			i.putExtra(RouteActivity.EXTRAS_LOCATION_EVENT_ID, mEventId);
			i.putExtra(RouteActivity.INTENT_EXTRA_START_DAY, mStart);
			i.putExtra(RouteActivity.INTENT_EXTRA_END_DAY, mEnd);
			if(mInfo != null) {
				i.putExtra(RouteActivity.EXTRAS_TITLE, String.format("%.2f", mInfo.getDistance()) + " km");
			}
			startActivity(i);
			return true;
		}
		return false;
    }
    
    private void showDeleteDialog(){
    	new MaterialDialog.Builder(this)
        .title(R.string.dialog_title_delete_route)
		.positiveText(R.string.action_ok)
		.negativeText(R.string.action_cancel)
		.content(R.string.dialog_text_delete_route)
		.callback(new MaterialDialog.ButtonCallback() {

			@Override
			public void onPositive(MaterialDialog dialog) {
				if(mEventId != -1) {
					DataBaseHandler.getInstance().deleteLocations(mEventId);
				} else if(mStart != -1 && mEnd != -1) {
					DataBaseHandler.getInstance().deleteLocations(mStart, mEnd);
				}
				finish();
			}

		}).show();
    }
    
	private void initListItems(long start, long end, int eventId) {
		if(eventId != -1){
			DataBaseHandler.getInstance().getAllLocationByEventId(eventId, this);
		} if(start != -1 && end != -1){
			DataBaseHandler.getInstance().getAllLocationByTimeFrame(
				start, end, ActivityRouteInfo.this);
		} 
	}
	
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		mMap.getUiSettings().setMapToolbarEnabled(false);
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		
		if(mInfo != null){
			setMapPolyLine(mInfo.getMapPolyline());
		}
	}
    
	private void setMapPolyLine(PolylineOptions opt){		
		if(opt == null || mMap == null || opt.getPoints().isEmpty()){
			return;
		}
		
		mMap.addPolyline(opt);
		
		final LatLngBounds.Builder builder = new LatLngBounds.Builder();
		builder.include(opt.getPoints().get(0));
		builder.include(opt.getPoints().get(opt.getPoints().size() -1));
		LatLngBounds bounds = builder.build();
		
		mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
	}
	
    private  int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }

	@Override
	public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
		// Translate overlay and image
        float flexibleRange = mFlexibleSpaceImageHeight - mActionBarSize;
        int minOverlayTransitionY = mActionBarSize - mOverlayView.getHeight();
        float overlayTranslation = ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0);
        
        mOverlayView.setTranslationY(overlayTranslation);
        mToolbarShadow.setTranslationY(overlayTranslation);
        mMapView.setTranslationY(ScrollUtils.getFloat(-scrollY / 2, minOverlayTransitionY, 0));

        // Change alpha of overlay
        float alpha = ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1);        
        mOverlayView.setBackgroundColor(adjustAlpha(mToolbarColor, alpha));
        
        mTitleView.setTranslationX(ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1) * mContentInset);
       
        // Scale title text
        float scale = 1 + ScrollUtils.getFloat((flexibleRange - scrollY) / flexibleRange, 0, MAX_TEXT_SCALE_DELTA);
        mTitleView.setPivotX(0);
        mTitleView.setPivotY(0);
        mTitleView.setScaleX(scale);
        mTitleView.setScaleY(scale);

        // Translate title text
        mTitleView.setTranslationY(ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0) + (mFlexibleSpaceImageHeight - mActionBarHeight));
        
		
	}

	@Override
	public void onDownMotionEvent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpOrCancelMotionEvent(ScrollState scrollState) {
		// TODO Auto-generated method stub
		
	}
	
	private int adjustAlpha(int color, float factor) {
	    int alpha = Math.round(Color.alpha(color) * factor);
	    int red = Color.red(color);
	    int green = Color.green(color);
	    int blue = Color.blue(color);
	    return Color.argb(alpha, red, green, blue);
	}

	@Override
	public void onGetLocations(ArrayList<CustomLocation> locations) {
		GetRoutePath task = new GetRoutePath(locations);
		task.execute();		
	}
	
	private class GetRoutePath extends AsyncTask<Void, Void, RouteInfo> {
		private final ArrayList<CustomLocation> mList;
		
		public GetRoutePath(ArrayList<CustomLocation> locationList){
			mList = locationList;
		}
		
		@Override
		protected RouteInfo doInBackground(Void... params) {	
			
			final PolylineOptions mMapPolyline = getNewLine();
			final Line mSpeedGraphLine = new Line();
			final RouteInfo info = new RouteInfo();

			long start = -1;
			long end = -1;
			
			double dist = 0.0;
			
			Location lastLoc = null;
			Location lastPoint = null;
			
			Location startLoc = null;
			
			if(mList == null) {
				return info;
			}
			for(CustomLocation location : mList){
				if(startLoc == null){
					startLoc = location;
				}
				
				if(lastLoc != null){
					double distance = Utils.calculateDistance(location.getLatitude(), location.getLongitude(), lastLoc.getLatitude(), lastLoc.getLongitude());
					dist += distance; //In meters
					
					float secs = (location.getTime() - lastPoint.getTime()) / 1000f;

					if (secs >= 10) {
						float speed = ((lastPoint.distanceTo(location) / secs) * 3.6f);
						
						final LinePoint point = new LinePoint(location.getTime(), speed);
						mSpeedGraphLine.addPoint(point);
						lastPoint = location;

					}

					lastLoc = location;
					
				} else {
					lastLoc = location;
					lastPoint = location;
				}
				
				mMapPolyline.add(new LatLng(location.getLatitude(), location.getLongitude()));
			}
			
			if (!mList.isEmpty()) {
				start = mList.get(0).getTime();
				end = mList.get(mList.size() - 1).getTime();
			}
			
			final int duration = DateCalculations.convertDatesToMinutes(start, end);
			final float averageSpeed = (dist == 0 || duration == 0) ? 0: ((float)(dist / (duration * 60)) * 3.6f);
					
        	info.setDistance((float)dist / 1000);
			info.setAverageSpeed(averageSpeed);
			info.setDuration(duration);
			info.setSpeedGraphLine(mSpeedGraphLine);
			info.setMapPolyline(mMapPolyline);
			
			if(startLoc != null){
				info.setStartLocation(startLoc.getLatitude(), startLoc.getLongitude());
				info.setEndLocation(lastLoc.getLatitude(), lastLoc.getLongitude());
			}
						
			return info;
		}
		
		@Override
		protected void onPostExecute(RouteInfo result){	
			mInfo = result;
			
			setData(mInfo);
			setMapPolyLine(result.getMapPolyline());
		}
		
		private PolylineOptions getNewLine(){
			PolylineOptions opt = new PolylineOptions();
			opt.width(mDensity * 3);
			opt.color(mColorBlue);
			opt.visible(true);
			
			return opt;
		}
		
		
	}
	
	
	
	

	private void setData(RouteInfo info){
		setMapPolyLine(info.getMapPolyline());

		mGraph.setLine(info.getSpeedGraphLine());
		mAverageSpeed.setText(String.valueOf((int)info.getAverageSpeed()) + " km/h");
		mDuration.setText(DateCalculations.convertMinutesToTimeString(info.getDuration()));
		mTitleView.setText(String.format("%.2f", info.getDistance()) + " km");
		
		new GetAdressTask(this, new OnGetAdressListener() {
			
			@Override
			public void onAdressReceived(String adress) {
				if(adress != null && !adress.isEmpty()){
					mStartLocation.setText(adress);
				} else {
					mStartLocation.setText(R.string.title_address_unavailable);
				}
				
			}
		}, info.getStartLatitude(), info.getStartLongitude()).execute();
		
        new GetAdressTask(this, new OnGetAdressListener() {
			
			@Override
			public void onAdressReceived(String adress) {
				if(adress != null && !adress.isEmpty()){
					mEndLocation.setText(adress);
				} else {
					mEndLocation.setText(R.string.title_address_unavailable);
				}
				
			}
		}, info.getEndLatitude(), info.getEndLongitude()).execute();
		
	}
	

	
}
