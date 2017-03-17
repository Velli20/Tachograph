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

package com.velli.tachograph.restingtimes;

import com.velli.tachograph.Event;

public class WorkDayCalculator {
	private WorkDayHolder mCurrentDay;
	private WeekHolder mCurrentWeek;
	private Event mCurrentEvent;
	private Event mPreviousEvent;
	
	private int mDailyDriveTime;
	private int mDailyWorkTime;
	private int mDailyRestTime;
	private int mDailyWtdWorkTime;
	
	private boolean mSetTimeParams = false;
	private boolean mSetLocationParams = false;
	
	private double mDrivenDistance = 0;
	
	public WorkDayCalculator(){
		
	}
	
	public void resetParams(){
		mCurrentDay = null;
		mCurrentWeek = null;
		mCurrentEvent = null;
		mPreviousEvent = null;
		
		mDailyDriveTime = 0;
		mDailyWorkTime = 0;
		mDailyWtdWorkTime = 0;
		mDailyRestTime = -1;
		mDrivenDistance = 0;
		mSetTimeParams = false;
	}
	
	public WorkDayCalculator setParamsCurrentWeekHolder(WeekHolder currentWeek){
		mCurrentWeek = currentWeek;
		return this;
	}
	
	public WorkDayCalculator setParamsCurrentDayHolder(WorkDayHolder currentDay){
		mCurrentDay = currentDay;
		return this;
	}
	
	public WorkDayCalculator setParamsCurrentEvent(Event ev){
		this.mCurrentEvent = ev;
		return this;
	}
	
	public WorkDayCalculator setParamsPreviousEvent(Event previousEvent){
		this.mPreviousEvent = previousEvent;
		return this;
	}
	
	public WorkDayCalculator setTimes(int dailyDriveTime, int dailyWorkTime, int wtdDailyWorkTime){
		this.mDailyDriveTime = dailyDriveTime;
		this.mDailyWorkTime = dailyWorkTime;
		this.mDailyWtdWorkTime = wtdDailyWorkTime;
		mSetTimeParams = true;
		return this;
	}
	
	public WorkDayCalculator setDrivenDistance(double distance) {
		mDrivenDistance = distance;
		mSetLocationParams = true;
		return this;
	}
	public WorkDayCalculator setDailyRest(int dailyRestTime){
		this.mDailyRestTime = dailyRestTime;
		return this;
	}
	
	public WorkDayHolder endDay(){
		if(mDailyWorkTime > Constants.LIMIT_DAILY_WORK_MIN && mCurrentWeek != null) {
			if (mCurrentWeek.hasExtendedDailyWorktimesLeft() && mDailyWorkTime <= Constants.LIMIT_DAILY_WORK_EXTENDED_MIN) {
				mCurrentDay.setExtendedDailyWork(true);
			} else {
				mCurrentDay.setDailyWorktimeExceeded(true);
			}
		}
		if(mDailyDriveTime > Constants.LIMIT_DAILY_DRIVE_MIN && mCurrentWeek != null) {
			if (mCurrentWeek.hasExtendedDailyDrivetimesLeft() && mDailyDriveTime <= Constants.LIMIT_DAILY_DRIVE_EXTENDED_MIN) {
				mCurrentDay.setDailyDrivetimeExtended(true);
			} else {
				mCurrentDay.setDailyDrivetimeExceeded(true);
			}
		}
		
		if (mSetTimeParams) {
			mCurrentDay.setDailyDrivetime(mDailyDriveTime);
			mCurrentDay.setDailyWorktime(mDailyWorkTime);
			mCurrentDay.setWtdWorkingTime(mDailyWtdWorkTime);
		}
		if(mSetLocationParams) {
			mCurrentDay.setDailyDrivenDistance(mDrivenDistance);
		}
		if(mDailyRestTime >= 0){
			setDailyRest();
		}
		if(mCurrentEvent != null) {
			mCurrentDay.setEnd(mPreviousEvent != null? mPreviousEvent.getEndDateInMillis() : mCurrentEvent.getEndDateInMillis());
		}
		return mCurrentDay;
	}
	
	private void setDailyRest(){
		mCurrentDay.setDailyRestTime(mDailyRestTime);
		mCurrentDay.setWtdDailyRestingTime(mDailyDriveTime);
		
		if(mDailyRestTime < Constants.LIMIT_DAILY_REST_MIN) {
			if(mDailyRestTime >= Constants.LIMIT_DAILY_REST_REDUCED_MIN 
					&& (mCurrentWeek != null && mCurrentWeek.hasReducedDailyrestLeft())) {
				mCurrentDay.setReducedDailyrest(true);
				mCurrentDay.setInvalidDailyrest(false);
			} else {
				mCurrentDay.setInvalidDailyrest(true);
			}
		}
	}
	
	public WorkDayHolder getNewWorkingDayHolder(){
		WorkDayHolder holder = new WorkDayHolder();
		holder.setStart(mCurrentEvent.getStartDateInMillis());
		
		return holder;
	}
	
	public WorkDayHolder getNewWorkingDayHolder(long start){
		WorkDayHolder holder = new WorkDayHolder();
		holder.setStart(start);
		
		return holder;
	}
	
	
}
