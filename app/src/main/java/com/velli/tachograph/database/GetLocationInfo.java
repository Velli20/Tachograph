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

package com.velli.tachograph.database;

import static com.velli.tachograph.database.DataBaseHandlerConstants.LOCATION_COLUMNS;
import static com.velli.tachograph.database.DataBaseHandlerConstants.TABLE_LOCATIONS;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.velli.tachograph.DateCalculations;
import com.velli.tachograph.Utils;
import com.velli.tachograph.location.RouteInfo;
import com.velli.tachograph.views.Line;
import com.velli.tachograph.views.LinePoint;

public class GetLocationInfo extends AsyncTask<Void, Void, RouteInfo> {
	private SQLiteDatabase mDb;
	private OnGetLocationInfoListener mListener;
	
	private String mQuery;

	private int mMapLineStrokeWidth;
	private int mMapLineColor;
	
	public interface OnGetLocationInfoListener {
		void onGetLocationInfo(RouteInfo info);
	}
	
	public GetLocationInfo(SQLiteDatabase db) {
		mDb = db;
	}
	
	public GetLocationInfo setParams(long startTime, long endTime, int eventId) {		
		final StringBuilder b = new StringBuilder();
		
		b.append("SELECT " + LOCATION_COLUMNS + " FROM " + TABLE_LOCATIONS);
		b.append(" WHERE(");
		if(startTime != -1 && endTime != -1) {
			b.append(DataBaseHandlerConstants.KEY_LOCATION_TIME + " >= " + String.valueOf(startTime))
					.append(" AND " + DataBaseHandlerConstants.KEY_LOCATION_TIME + " <= " + String.valueOf(endTime));
		}
		if(eventId != -1) {
			if(startTime != -1 && endTime != -1) {
				b.append(" AND ");
			}
			b.append(DataBaseHandlerConstants.KEY_LOCATION_EVENT_ID + " = " + String.valueOf(eventId));
		}
		b.append(")" + "ORDER BY " + DataBaseHandlerConstants.KEY_LOCATION_TIME + " ASC");
		mQuery = b.toString();
		return this;
	}
	
	public void setMapParams(int lineStrokeWidth, int lineColor) {
		mMapLineStrokeWidth = lineStrokeWidth;
		mMapLineColor = lineColor;
	}
	
	public void setOnGetLocationInfoListener(OnGetLocationInfoListener l) {
		mListener = l;
	}
	
	@Override
	protected RouteInfo doInBackground(Void... params) {
		final PolylineOptions mMapPolyline = getNewLine();
		final Line mSpeedGraphLine = new Line();
		final RouteInfo info = new RouteInfo();
	    final Cursor cursor = mDb.rawQuery(mQuery, null);
	    
	    double totalDistance = 0;
	    double lastLatitude = -1;
	    double lastLongitude = -1;
	    
	    long startTime = -1;
	    long endTime = -1;
	    if (cursor != null && cursor.moveToFirst()){
	    	do {
	    		
	    		double latitude = cursor.getDouble(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_LOCATION_LATITUDE));
	    		double longitude = cursor.getDouble(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_LOCATION_LONGITUDE));
	    		
	    		float speed = (float) cursor.getDouble(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_LOCATION_SPEED));
	    		long time = cursor.getLong(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_LOCATION_TIME));

	            if(lastLatitude != -1 && lastLongitude != -1) {
	            	double distance = Utils.calculateDistance(latitude, longitude, lastLatitude, lastLongitude);
	            	totalDistance += distance; //In meters
	            }
	            
	            lastLatitude = latitude;
	            lastLongitude = longitude;
	            
	            if(cursor.isFirst()) {
	            	startTime = time;
	            	info.setEndLocation(latitude, longitude);
	            } else if(cursor.isLast()) {
	            	endTime = time;
	            	info.setEndLocation(latitude, longitude);
	            }
				
				mSpeedGraphLine.addPoint(new LinePoint(time, speed * 3.6f));
	    		mMapPolyline.add(new LatLng(latitude, longitude));
	    	} while(cursor.moveToNext());
	    } else {
	    	return null;
	    }
	    
	    final int duration = DateCalculations.convertDatesToMinutes(startTime, endTime);
		final float averageSpeed = (totalDistance == 0 || duration == 0) ? 0: ((float)(totalDistance / (duration * 60)) * 3.6f);
		
		
		info.setDistance((float)totalDistance / 1000);
		info.setAverageSpeed(averageSpeed);
		info.setDuration(duration);
		info.setSpeedGraphLine(mSpeedGraphLine);
		info.setMapPolyline(mMapPolyline);
		
	    if(cursor != null){
	    	cursor.close();
	    }
		return info;
	}
	
	@Override
	protected void onPostExecute(RouteInfo result){	
		if(mListener != null) {
			mListener.onGetLocationInfo(result);
		}
		
		mListener = null;
		mDb = null;
	}
	
	private PolylineOptions getNewLine(){
		PolylineOptions opt = new PolylineOptions();
		opt.width(mMapLineStrokeWidth);
		opt.color(mMapLineColor);
		opt.visible(true);
		
		return opt;
	}

}
