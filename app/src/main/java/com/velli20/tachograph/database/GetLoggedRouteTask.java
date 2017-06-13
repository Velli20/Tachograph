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

package com.velli20.tachograph.database;



import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.velli20.tachograph.DateUtils;
import com.velli20.tachograph.location.LoggedRoute;

import graph.velli.com.librarytimebasedgraph.Line;
import graph.velli.com.librarytimebasedgraph.LinePoint;

public class GetLoggedRouteTask extends AsyncTask<Void, Void, LoggedRoute> {
	private SQLiteDatabase mDb;
	private OnGetLoggedRouteListener mListener;
	
	private String mQuery;

	public interface OnGetLoggedRouteListener {
		void onGetLoggedRoute(LoggedRoute info);
	}
	
	public GetLoggedRouteTask(SQLiteDatabase db, int eventId) {
		mDb = db;

		mQuery = new DatabaseLocationQueryBuilder()
				.fromTable(DataBaseHandlerConstants.TABLE_LOCATIONS)
                .selectAllColumns()
				.whereLocationEventIdIs(eventId)
				.orderByKey(DataBaseHandlerConstants.KEY_LOCATION_TIME, true)
				.buildQuery();
	}

	
	public void setOnGetLocationInfoListener(OnGetLoggedRouteListener l) {
		mListener = l;
	}
	
	@Override
	protected LoggedRoute doInBackground(Void... params) {
        if(mDb == null || !mDb.isOpen() || mQuery == null){
            return null;
        }

        mDb.beginTransaction();

		final PolylineOptions mapPolyline = new PolylineOptions();
		final Line speedGraphLine = new Line();
		final LoggedRoute route = new LoggedRoute();
	    final Cursor cursor = mDb.rawQuery(mQuery, null);
	    
	    double totalDistance = 0;
	    double lastLatitude = -1;
	    double lastLongitude = -1;
	    
	    long startTime = -1;
	    long endTime = -1;
	    if (cursor != null && !cursor.isClosed() && cursor.moveToFirst()){
	    	do {
	    		
	    		double latitude = cursor.getDouble(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_LOCATION_LATITUDE));
	    		double longitude = cursor.getDouble(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_LOCATION_LONGITUDE));
	    		
	    		float speed = (float) cursor.getDouble(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_LOCATION_SPEED));
	    		long time = cursor.getLong(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_LOCATION_TIME));

	            if(lastLatitude != -1 && lastLongitude != -1) {
	            	totalDistance += DatabaseLocationUtils.calculateDistance(latitude, longitude, lastLatitude, lastLongitude);
	            }
	            
	            lastLatitude = latitude;
	            lastLongitude = longitude;
	            
	            if(cursor.isFirst()) {
	            	startTime = time;
	            	route.setStartLocation(latitude, longitude);
	            } else if(cursor.isLast()) {
	            	endTime = time;
	            	route.setEndLocation(latitude, longitude);
	            }
				
				speedGraphLine.addPoint(new LinePoint(time, speed));
	    		mapPolyline.add(new LatLng(latitude, longitude));
	    	} while(cursor.moveToNext());
	    } else {
            mDb.endTransaction();
	    	return null;
	    }

        cursor.close();
	    
	    final int duration = DateUtils.convertDatesToMinutes(startTime, endTime);
		final float averageSpeed = (totalDistance == 0 || duration == 0) ? 0: ((float)(totalDistance / (duration * 60)) * 3.6f);

		mapPolyline.visible(true);

		route.setDistance((float)totalDistance / 1000);
		route.setAverageSpeed(averageSpeed);
		route.setDuration(duration);
		route.setSpeedGraphLine(speedGraphLine);
		route.setMapPolyline(mapPolyline);


        mDb.setTransactionSuccessful();
        mDb.endTransaction();

		return route;
	}
	
	@Override
	protected void onPostExecute(LoggedRoute result){
		if(mListener != null) {
			mListener.onGetLoggedRoute(result);
		}
		
		mListener = null;
		mDb = null;
	}
	


}
