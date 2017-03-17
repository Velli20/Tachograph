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

public class WorkWeekCalculator {
	private WeekHolder mCurrentWeek;		
	private WorkDayHolder mCurrentDay;
	
	private Event mCurrentEvent;
	private Event mPreviousEvent;
	
	private int mWeeklyDriveTime = 0;
	private int mWeeklyWorkingTime = 0;
	private int mWtdWeeklyWorkTime = 0;
	private int mWeeklyRestingTime = 0;
	private long mWeekStart = -1;
	private double mWeeklyDrivenDistance = 0;
	
	private boolean mSetTimeParams = false;
	private boolean mSetWeeklyRest = false;
	private boolean mWasPreviousWeeklyRestInvalid = false;
	private boolean mSetLocationParams = false;
	
	public WorkWeekCalculator(){
		
	}
	
	public WorkWeekCalculator setParamCurrentWeek(WeekHolder previousWeek){
		mCurrentWeek = previousWeek;
		return this;
	}
	
	public WorkWeekCalculator setParamCurrentDay(WorkDayHolder currentDay){
		mCurrentDay = currentDay;
		return this;
	}
	
	public WorkWeekCalculator setParamCurrentEvent(Event currentEvent){
		mCurrentEvent = currentEvent;
		return this;
	}
	
	public WorkWeekCalculator setParamPreviousEvent(Event previousEvent){
		mPreviousEvent = previousEvent;
		return this;
	}
	
	public WorkWeekCalculator setTimeParams(int weeklyDrivingTime, int weeklyWorkingTime, int wtdWorkingTime){
		mWeeklyDriveTime = weeklyDrivingTime;
		mWeeklyWorkingTime = weeklyWorkingTime;
		mWtdWeeklyWorkTime = wtdWorkingTime;
		mSetTimeParams = true;
		
		return this;
	}
	
	public WorkWeekCalculator setWeeklyRestingTime(int weeklyRestingTime){
		mWeeklyRestingTime = weeklyRestingTime;
		mSetWeeklyRest = true;
		
		return this;
	}
	
	public WorkWeekCalculator setParamPreviousWeeklyWasInvalid(boolean invalid){
		mWasPreviousWeeklyRestInvalid = invalid;
		return this;
	}
	
	public WorkWeekCalculator setWeekStartDate(long dateInMillis){
		mWeekStart = dateInMillis;
		return this;
	}
	
	public WorkWeekCalculator setWeeklyDrivenDistance(double dist) {
		mWeeklyDrivenDistance = dist;
		mSetLocationParams = true;
		return this;
	}
	
	private void setWeeklyRest(){
		if(mWeeklyRestingTime >= Constants.LIMIT_WEEKLY_REST_MIN){
			if(mWeeklyRestingTime <= Constants.LIMIT_WEEKLY_REST_REDUCED_MIN) {
				if(!mWasPreviousWeeklyRestInvalid) {
					mCurrentWeek.setReducedWeeklyRest(true);
				} else {
					mCurrentWeek.setInvalidWeeklyrest(true);
				}
			}
		} else {
			mCurrentWeek.setInvalidWeeklyrest(true);
		}
		mCurrentWeek.setWeeklyRest(mWeeklyRestingTime);
		mCurrentWeek.setWtdWeeklyRestingTime(mWeeklyRestingTime);
	}
	
	public WeekHolder execute(){
		WeekHolder holder = new WeekHolder();
		
		holder.setStart(mWeekStart == -1? mCurrentEvent.getStartDateInMillis() : mWeekStart);
		
		if(mCurrentDay != null && !mCurrentDay.isEnded()){
			if(mCurrentWeek != null){
				mCurrentWeek.addWorkDay(mCurrentDay);
			} else {
				holder.addWorkDay(mCurrentDay);
			}
		}
		if(mCurrentWeek != null && !mCurrentWeek.isEnded()){
			long end = 0;
			
			if(mPreviousEvent != null){
				end = mPreviousEvent.getEndDateInMillis();
			} else if(mCurrentEvent != null){
				end = mCurrentEvent.getStartDateInMillis();
			}
			if(mSetTimeParams){
				mCurrentWeek.setWeeklyDrivetime(mWeeklyDriveTime);
				mCurrentWeek.setWeeklyWorktime(mWeeklyWorkingTime);
				mCurrentWeek.setWtdWeeklyWorkingTime(mWtdWeeklyWorkTime);
			}
			if(mSetWeeklyRest){
				setWeeklyRest();
				
				end = mCurrentEvent.getEndDateInMillis();
			}
			
			if(mSetLocationParams) {
				mCurrentWeek.setWeeklyDrivenDistance(mWeeklyDrivenDistance);
			}
			int weeklyRest = mCurrentWeek.getWeeklyrest();
			if(weeklyRest < Constants.LIMIT_WEEKLY_REST_REDUCED_MIN){
				mCurrentWeek.setInvalidWeeklyrest(true);
			} else if(weeklyRest>= Constants.LIMIT_WEEKLY_REST_REDUCED_MIN
					&& weeklyRest < Constants.LIMIT_WEEKLY_REST_MIN){
				mCurrentWeek.setReducedWeeklyRest(true);
			}
			mCurrentWeek.setEnd(end);
		}
		return holder;
	}
}
