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

package com.velli20.tachograph.location;

import com.google.android.gms.maps.model.PolylineOptions;

import graph.velli.com.librarytimebasedgraph.Line;

public class LoggedRoute {
	private PolylineOptions mMapPolyLine;
	private Line mSpeedGraphLine;
	
	private int mDuration;
	private float mAverageSpeed;
	private float mDistance;
	
	private double mStartLatitude;
	private double mStartLongitude;

	private double mEndLatitude;
	private double mEndLongitude;
	
	public LoggedRoute(){ }
	
	public void setMapPolyline(PolylineOptions opt){
		mMapPolyLine = opt;
	}
	
	public void setSpeedGraphLine(Line line){
		mSpeedGraphLine = line;
	}
	
	public void setDuration(int duration){
		mDuration = duration;
	}
	
	public void setAverageSpeed(float averageSpeed){
		mAverageSpeed = averageSpeed;
	}
	
	public void setDistance(float distance){
		mDistance = distance;
	}
	
	public void setStartLocation(double latitude, double longitude){
		mStartLatitude = latitude;
		mStartLongitude = longitude;
	}
	
	public void setEndLocation(double latitude, double longitude){
		mEndLatitude = latitude;
		mEndLongitude = longitude;
	}
	
	public PolylineOptions getMapPolyline(){
		return mMapPolyLine;
	}
	
	public Line getSpeedGraphLine(){
		return mSpeedGraphLine;
	}
	
	public int getDuration(){
		return mDuration;
	}
	
	public float getAverageSpeed(){
		return mAverageSpeed;
	}
	
	public float getDistance(){
		return mDistance;
	}
	
	public double getStartLatitude(){
		return mStartLatitude;
	}
	
	public double getStartLongitude(){
		return mStartLongitude;
	}
	
	public double getEndLatitude(){
		return mEndLatitude;
	}
	
	public double getEndLongitude(){
		return mEndLongitude;
	}
	
}
