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

public class LogListItemLocation {
	private double mDuration;
	private double mDistance;
	
	private final long mStartDate;
	private final long mEndDate;
	
	
	public LogListItemLocation(double distance, double duration, long start, long end){
		mDuration = duration;
		mDistance = distance;
		mStartDate = start;
		mEndDate = end;
	}


	public void setDistanceInMeters(double dist){
		mDistance = dist;
	}
	
	public double getDistanceInMeters(){
		return mDistance;
	}
	
	public double getDuration(){
		return mDuration;
	}
	
	public long getStartDate(){
		return mStartDate;
	}
	
	public long getEndDate(){
		return mEndDate;
	}
	
}
