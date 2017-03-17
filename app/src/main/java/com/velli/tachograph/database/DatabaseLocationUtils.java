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

import com.velli.tachograph.Utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;

import java.util.List;
import java.util.Locale;

public class DatabaseLocationUtils {
	
	
	public static double getDistanceInKmBetweenTimes(SQLiteDatabase db, long start, long end) {
		if(db == null || !db.isOpen()) {
			return 0;
		}
		
		double distance = 0.0;
		
		final String query = "SELECT " + LOCATION_COLUMNS + " FROM " + TABLE_LOCATIONS 
				+ " WHERE(" + DataBaseHandlerConstants.KEY_LOCATION_TIME + " >= " + String.valueOf(start) + " AND " + DataBaseHandlerConstants.KEY_LOCATION_TIME + " <= " + String.valueOf(end)
				+") ORDER BY " + DataBaseHandlerConstants.KEY_LOCATION_TIME + " ASC";
		
		
	    final Cursor cursor = db.rawQuery(query, null);
	    
	    double lastLatitude = -1;
	    double lastLongitude = -1;
	    
	    if (cursor != null && cursor.moveToFirst()){
	    	do {
	    		double latitude = cursor.getDouble(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_LOCATION_LATITUDE));
	    		double longitude = cursor.getDouble(cursor.getColumnIndex(DataBaseHandlerConstants.KEY_LOCATION_LONGITUDE));
	    		
	    		if(lastLatitude != -1 && lastLongitude != -1) {
	    			distance += Utils.calculateDistance(latitude, longitude, lastLatitude, lastLongitude);
	    		}
	    		
	    		lastLatitude = latitude;
	    		lastLongitude = longitude;
	    	} while(cursor.moveToNext());
	    }
	    
	    if(cursor != null){
	    	cursor.close();
	    }
	    
	    if(distance != 0) {
	    	return distance / 1000;
	    }
		return distance;
	}
	
	
	
	public static String getAdressForLocation(Context context, double latitude, double longitude) {
		if(context == null){
			return null;
		}

		try {

			Geocoder geo = new Geocoder(context, Locale.getDefault());
			List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
			if (addresses.isEmpty()) {
				return null;
			}
			else {
				if(addresses.size() > 0) {
					return addresses.get(0).getAddressLine(1);
				}
			}
		}
		catch (Exception e) {}

		return null;
	}
	
	
	

}
