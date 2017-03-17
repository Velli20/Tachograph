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

import java.util.List;

import com.google.android.gms.location.DetectedActivity;

import android.content.res.Resources;
import android.location.Location;

public class Utils {

	
	public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 6371000; //meters
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

	    return earthRadius * c;
	}
	
	public static String[] getRestTimeTypeArray(Resources res, int currentAction){
		return new String[]{
				currentAction == Event.EVENT_TYPE_WEEKLY_REST ? res.getString(R.string.menu_stop_weekly_rest)
						: res.getString(R.string.menu_start_weekly_rest),
				currentAction == Event.EVENT_TYPE_DAILY_REST ? res.getString(R.string.menu_stop_daily_rest)
						: res.getString(R.string.menu_start_daily_rest),
				(currentAction == Event.EVENT_TYPE_NORMAL_BREAK || currentAction == Event.EVENT_TYPE_SPLIT_BREAK) ? res.getString(R.string.menu_stop_break)
						: res.getString(R.string.menu_start_break)};
	}
	
	public static int getRestingSelectedOption(int currentAction){
		if(currentAction == Event.EVENT_TYPE_WEEKLY_REST){
			return 0;
		} else if(currentAction == Event.EVENT_TYPE_DAILY_REST){
			return 1;
		} else {
			return 2;
		}
	}
	
	public static int[] convertIntegers(List<Integer> integers) {
		int[] ret = new int[integers.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = integers.get(i);
		}
		return ret;
	}
	


	
	public static float getSpeedFromLocations(Location start, Location end){
		final long millis = (end.getTime() - start.getTime());
		long secs = millis <= 0 ? 0 : (millis / 1000);
		
		
		final float dist = start.distanceTo(end);
		
		if(dist <= 0 || secs <= 0){
			return 0;
		} else {
			return (dist / secs) * 3.6f;
		}
	}

	
	

	public static DetectedActivity walkingOrRunning(List<DetectedActivity> probableActivities) {
		DetectedActivity myActivity = null;
		int confidence = 0;
		for (DetectedActivity activity : probableActivities) {
			if (activity.getType() != DetectedActivity.RUNNING && activity.getType() != DetectedActivity.WALKING)
				continue;

			if (activity.getConfidence() > confidence)
				myActivity = activity;
		}

		return myActivity;
	}

	/**
	 * Map detected activity types to strings
	 * 
	 * @param activityType
	 *            The detected activity type
	 * @return A user-readable name for the type
	 */
	public static String getNameFromType(int activityType) {
		switch (activityType) {
		case DetectedActivity.IN_VEHICLE:
			return "driving";
		case DetectedActivity.ON_BICYCLE:
			return "on bicycle";
		case DetectedActivity.RUNNING:
			return "running";
		case DetectedActivity.WALKING:
			return "walking";
		case DetectedActivity.ON_FOOT:
			return "on foot";
		case DetectedActivity.STILL:
		case DetectedActivity.TILTING:
			return "still";
		default:
			return "unknown";
		}
}
}

