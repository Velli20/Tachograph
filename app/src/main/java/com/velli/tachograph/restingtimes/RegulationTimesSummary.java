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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;

import com.velli.tachograph.Event;
import com.velli.tachograph.database.DataBaseHandler;
import com.velli.tachograph.database.DataBaseHandler.OnDatabaseEditedListener;
import com.velli.tachograph.database.DataBaseHandler.OnTaskCompleted;
import com.velli.tachograph.restingtimes.BreakAndRestTimeCalculations.OnCalculatiosReadyListener;

import android.os.Handler;

public class RegulationTimesSummary implements OnTaskCompleted, OnDatabaseEditedListener {
	
	private int mDailyRest = 0;
    private int mDailyDrive = 0;
    private int mDailyWork = 0;
    private int mWeeklyRest = 0;
    private int mWeeklyDrive = 0;
    
    private int mWTDWeeklyWork = 0;
    private int mWTDWeeklyWorkAverage = 0;
    private int mWtdWeeklyWorkLimit = 0;
    private int mWTDWeeklyRest = 0;
    private int mWTDDailyRest = 0;
    private int mWTDDailyWorkingTime = 0;
    private int mWTDWeeklyWorkingAverageOfWeeks = 0;
    private int mFortnightlyDrivingTime = 0;
    
	private int mContiniousDrivetimeAfterBreak = 0; // Will be reseted after each break
	private int mContiniousBreak = 0;

	private BreakTime mLastEligibleSplitBreak;
	private BreakTime mLastBreak;
	
    private int mLastweekDrivetime = 0;
    
    private int mReducedWeekRestsLeft = 1;
    private int mReducedDailyRestsLeft = 3;
    private int mExtendedWorkDaysLeft = 3;
    private int mExtendedDailyDrivesLeft = 2;
    
    private long mNextDailyRest = -1;
    private long mNextWeeklyRest = -1;
    private long mWorkingDayStart = -1;
    
    private static RegulationTimesSummary mInstance;
    private Handler mWorkLimitListUpdaterHandler = new Handler();
    private ArrayList<WeakReference<OnTotalTimesChanged>> mCallbacks = new ArrayList<>();
    private ArrayList<WeekHolder> mWeekList;
    private boolean mIncludeAutomaticallyCalculatedRestingEvents = false;
    
    public interface OnTotalTimesChanged {
		void timesChanged();
	}
    
    
    public static RegulationTimesSummary getInstance(){
    	if(mInstance == null){
    		mInstance = new RegulationTimesSummary();
    	}
    	return mInstance;
    }
    
    private RegulationTimesSummary(){
		DataBaseHandler mDb = DataBaseHandler.getInstance();
    	mDb.getAllEvents(this, true, false, mIncludeAutomaticallyCalculatedRestingEvents);
    	mDb.registerOnDatabaseEditedListener(this);
    	mWorkLimitListUpdaterHandler.postDelayed(mWorkLimitUpdater, 60000);
    }
    
    public void syncAutoUpdater(long sync){
    	mWorkLimitListUpdaterHandler.postDelayed(mWorkLimitUpdater, +(60000 - (sync)));
    }
    
    private Runnable mWorkLimitUpdater = new Runnable(){

		@Override
		public void run() {
			if(mWorkLimitListUpdaterHandler != null){
				mWorkLimitListUpdaterHandler.postDelayed(mWorkLimitUpdater, 60000);
				DataBaseHandler.getInstance().getAllEvents(RegulationTimesSummary.this, true, false, mIncludeAutomaticallyCalculatedRestingEvents);
			}
		}
		
	};
    
    public void registerForOnTotalTimesChanged(OnTotalTimesChanged l){
    	if(!mCallbacks.contains(l)){
    		mCallbacks.add(new WeakReference<>(l));
    	}
    }
    
    public void unregisterForOnTotalTimesChanged(OnTotalTimesChanged l){
    	mCallbacks.remove(l);
    }
    
	@Override
	public void onDatabaseEdited(int action, int rowId) {
		DataBaseHandler.getInstance().getAllEvents(this, true, false, mIncludeAutomaticallyCalculatedRestingEvents);
	}
    
    @Override
	public void onTaskCompleted(ArrayList<Event> list) {
		if(list != null){
			setEventsList(list);
		}
		
	}
    
    public void setIncludeAutomaticallyCalculatedRestingEvents(boolean include) {
    	mIncludeAutomaticallyCalculatedRestingEvents = include;
    }
    
    public void setEventsList(ArrayList<Event> l){
    	BreakAndRestTimeCalculations task = new BreakAndRestTimeCalculations();
    	task.calculateBreakAndRestingtimes(l, new OnCalculatiosReadyListener() {
			
			@Override
			public void onCalculationsReady(ArrayList<WeekHolder> list) {
				resetRestTimes();
	            setTimes(list);
				
				mWeekList = list;
				notifyCallbacks();
				
			}
		});
    }
    
    public ArrayList<WeekHolder> getWeekList(){
    	return mWeekList;
    }
    

	public void resetRestTimes() {
		mDailyRest = 0;
		mDailyDrive = 0;
		mDailyWork = 0;
				
		mWeeklyRest = 0;
		mWTDWeeklyWork = 0;
		mWTDWeeklyWorkAverage = 0;
		mWTDWeeklyRest = 0;
		mWTDDailyRest = 0;
		mWTDDailyWorkingTime = 0;
		mWTDWeeklyWorkingAverageOfWeeks = 0;
		mWeeklyDrive = 0;

		mFortnightlyDrivingTime = 0;
		mContiniousDrivetimeAfterBreak = 0;
		mReducedWeekRestsLeft = 1;
		mReducedDailyRestsLeft = 3;
		mExtendedWorkDaysLeft = 3;
		mExtendedDailyDrivesLeft = 2;

	}
	
	private void setTimes(ArrayList<WeekHolder> list){
		if(list != null){
			final int weekCount = list.size();
			
			WeekHolder currentWeek = null;
			WeekHolder lastWeek = null;
			
			if(weekCount >= 1){
				if(weekCount >= 2){
					currentWeek = list.get(weekCount -1);
					lastWeek = list.get(weekCount -2);
				} else {
					currentWeek = list.get(weekCount -1);
				}
			}

			if(lastWeek != null){
				mLastweekDrivetime = lastWeek.getWeeklyDrivetime();
				mFortnightlyDrivingTime += lastWeek.getWeeklyDrivetime();
			}
			
			mWTDWeeklyWorkingAverageOfWeeks = 0;
			int workingTime;
			for(int i = 0; i < (weekCount > 17 ? 17 : weekCount); i++){
				mWTDWeeklyWorkingAverageOfWeeks = i +1;
				
				workingTime = list.get((weekCount-1) - i).geWtdtWeeklyWorkingTime();
				mWTDWeeklyWorkAverage += (workingTime < 0 ? 0 : workingTime);
			}
			
			if(mWTDWeeklyWorkingAverageOfWeeks > 0){
				mWTDWeeklyWorkAverage = mWTDWeeklyWorkAverage / mWTDWeeklyWorkingAverageOfWeeks;
			}
			
			
			

			if (currentWeek != null) {
				ArrayList<WorkDayHolder> days = currentWeek.getWorkDays();
				
				if (days != null && !days.isEmpty()) {
					int lenght = days.size();

					WorkDayHolder day = days.get(lenght - 1);

					if (!day.isWeeklyRest()) {
						mDailyRest = day.getDailyRest();
						mDailyDrive = day.getDailyDrivetime();
						mDailyWork = day.getTotalWorktime();
						mWTDDailyWorkingTime = day.getWtdDailyWorkingTime();
						mWTDDailyRest = day.getWtdDailyRestingTime();

						mLastBreak = day.getLastBreak();
						
						final ArrayList<BreakTime> breaks = day.getBreakTimes();
						
						for(BreakTime breaktime : breaks){
							if(RestingTimesUtils.isLastSplitBreakEligible(breaktime) && !breaktime.isRecording()){
								mLastEligibleSplitBreak = breaktime;
								break;
							}
						}
						if (day.getLastBreak() != null) {
							mContiniousBreak = mLastBreak.getDuration();
						} 
						
						
						if(day.isDriving()){
							mContiniousDrivetimeAfterBreak = day.getContiniousDriveTimeAfterBreak();
						}
						
						mWorkingDayStart = day.getWorkdayStart();
						if (currentWeek.hasReducedDailyrestLeft()) {
							mNextDailyRest = day.getWorkdayStart() + Constants.FIVTEEN_HOURS_PERIOD_IN_MILLIS;
						} else {
							mNextDailyRest = day.getWorkdayStart() + Constants.THIRTEEN_HOURS_PERIOD_IN_MILLIS;
						}
					}

				}
		
				//Get this weeks times
				if(lastWeek != null){
					mReducedWeekRestsLeft = (lastWeek.isReducedWeeklyRest() || lastWeek.isInvalidWeeklyrest()) ? 0 : 1;
				} else {
					mReducedWeekRestsLeft = currentWeek.isReducedWeeklyRest() ? 0 : 1;
				}
				mReducedDailyRestsLeft = currentWeek.getReducedDailyrestLeft();
				mExtendedWorkDaysLeft = currentWeek.getExtendedDailyWorktimesLeft();
				mExtendedDailyDrivesLeft = currentWeek.getExtendedDailyDrivetimesLeft();
				mWeeklyRest = currentWeek.getWeeklyrest();
			    mWTDWeeklyWork = currentWeek.geWtdtWeeklyWorkingTime();
			    mWeeklyDrive = currentWeek.getWeeklyDrivetime();
			    
			    mFortnightlyDrivingTime += currentWeek.getWeeklyDrivetime();
			    if(currentWeek.getWeeklyrest() == 0){
			    	mNextWeeklyRest = currentWeek.getWeekStart() + Constants.SIX_DAYS_PERIOD_IN_MILLIS;
			    }
			}
			
			if(currentWeek == null){
				mNextWeeklyRest = System.currentTimeMillis() + Constants.SIX_DAYS_PERIOD_IN_MILLIS;
				mNextDailyRest = System.currentTimeMillis() + Constants.FIVTEEN_HOURS_PERIOD_IN_MILLIS;
			}
			
		}
	}
	
	private void notifyCallbacks(){
		for(WeakReference<OnTotalTimesChanged> callback : mCallbacks){
			if(callback != null && callback.get() != null){
				callback.get().timesChanged();
			}
		}
	}
	
	public int getNextEvent(int eventType) {
		if (eventType == Event.EVENT_TYPE_DRIVING) {
			boolean hasDailyDriveTimeLeft = mDailyDrive < getDailyDriveLimit();
			boolean hasDailyWorkingTimeLeft = mDailyWork < getDailyWorkLimit();
			
			if(hasDailyWorkingTimeLeftAfterBreakAndDrive()){
				return Event.EVENT_TYPE_NORMAL_BREAK;
			}
			if(!hasDailyDriveTimeLeft || !hasDailyWorkingTimeLeft){
				if (mNextWeeklyRest != -1 && (mNextWeeklyRest - Constants.ONE_DAY_PERIOD_IN_MILLIS) <= System.currentTimeMillis()) {
					return Event.EVENT_TYPE_WEEKLY_REST;
				} else {
					return Event.EVENT_TYPE_DAILY_REST;
				}
			} else {
				return Event.EVENT_TYPE_NORMAL_BREAK;
			}
			
			
		} else if (eventType == Event.EVENT_TYPE_NORMAL_BREAK) {
			if(getDailyDriveLimit() - mDailyDrive > 0){
				return Event.EVENT_TYPE_DRIVING;
			}
		}

		return -1;
	}
	

	
	private boolean hasDailyWorkingTimeLeftAfterBreakAndDrive(){
		boolean hasDailyDriveTimeLeft = mDailyDrive < getDailyDriveLimit();
		boolean hasDailyWorkingTimeLeft = mDailyWork < getDailyWorkLimit();
		
		if(hasDailyDriveTimeLeft && hasDailyWorkingTimeLeft) {
			boolean hasDrivingTimeLeftAfterBreak = ((mDailyDrive - mContiniousDrivetimeAfterBreak) + Constants.LIMIT_CONTINIOUS_DRIVING < getDailyDriveLimit());
			boolean hasWorkingTimeLeftAfterBreak = ((mDailyWork - mContiniousDrivetimeAfterBreak) + Constants.LIMIT_CONTINIOUS_DRIVING + getContiniousBreakLimit() < getDailyWorkLimit());
			
			return hasDrivingTimeLeftAfterBreak && hasWorkingTimeLeftAfterBreak;
		}
		return false;
	}
	
	public long getNextEventStartTime(int eventType){
		switch(getNextEvent(eventType)){
		
		case Event.EVENT_TYPE_NORMAL_BREAK:
			return System.currentTimeMillis() + ((getContiniousDriveLimit() - getContiniousDriveTime()) * 60 * 1000);
		case Event.EVENT_TYPE_DAILY_REST:
			return mNextDailyRest;
		case Event.EVENT_TYPE_WEEKLY_REST:
			return mNextWeeklyRest;
		case Event.EVENT_TYPE_DRIVING:
			return System.currentTimeMillis() + ((getContiniousBreakLimit() - getContiniousBreak()) * 60 * 1000);
			
		
		}
		return -1;
	}

	
	public int getDailyRest(){
		return mDailyRest;
	}
	
	public int getDailyRestLimit(){
		if(mReducedDailyRestsLeft > 0){
			return Constants.LIMIT_DAILY_REST_REDUCED_MIN;
		} else {
			return Constants.LIMIT_DAILY_REST_MIN;
		}
	}
	
	public int getWeeklyRest(){
		return mWeeklyRest;
	}
	
	public int getWeeklyRestLimit(){
		if(mReducedWeekRestsLeft > 0){
			return Constants.LIMIT_WEEKLY_REST_REDUCED_MIN;
		} else {
			return Constants.LIMIT_WEEKLY_REST_MIN;
		}
	}

	
	public int getDailyWorkLimit() {
		if(mExtendedWorkDaysLeft > 0){
			return Constants.LIMIT_DAILY_WORK_EXTENDED_MIN;
		} else {
			return Constants.LIMIT_DAILY_WORK_MIN;
		}
	}
	
	public int getContiniousDriveTime(){
		return mContiniousDrivetimeAfterBreak;
	}
	
	public int getWTDWeeklyWorkTime(){
		return mWTDWeeklyWork;
	}
	
	public int getWTDWeeklyWorkTime17weekAverage(){
		return mWTDWeeklyWorkAverage;
	}
	
	public int getWTDWeeklyWorkTimeAverageWeekCount(){
		return mWTDWeeklyWorkingAverageOfWeeks;
	}
	
	public int getWTDDailyRestingTime(){
		return mWTDDailyRest;
	}
	
	public int getWTDWeeklyRestingTime(){
		return mWTDWeeklyRest;
	}
	
	public int getWtdNightShiftWorkingTime(){
		if(mWorkingDayStart == -1){
			return -1;
		}
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(mWorkingDayStart);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		if(hour >= 0 && hour <= 4){
			return mWTDDailyWorkingTime;
		}
		return -1;
	}
	public int getFortnightlyDriveTime(){
		return mFortnightlyDrivingTime;
	}
	
	public int getFortnightlyDriveLimit() {
		return Constants.LIMIT_FORTNNIGHTLY_DRIVE;
	}
	
	public int getContiniousDriveLimit(){
		int left = (getDailyDriveLimit() - (mDailyDrive - mContiniousDrivetimeAfterBreak));
		
		if(left >= Constants.LIMIT_CONTINIOUS_DRIVING){
			return Constants.LIMIT_CONTINIOUS_DRIVING;
		} else {
			return left;
		} 
	}
	
	public int getContiniousBreak(){
		return mContiniousBreak;
	}
   
	public int getDailyDrivingTime(){
		return mDailyDrive;
	}
	
	public int getDailyDriveLimit(){
		if(mExtendedDailyDrivesLeft > 0){
			return Constants.LIMIT_DAILY_DRIVE_EXTENDED_MIN;
		} else {
			return Constants.LIMIT_DAILY_DRIVE_MIN;
		}
	}
	
	public int getContiniousBreakLimit(){
		if(mLastBreak != null){
			if(mLastBreak.isSplitBreak()){
				if(mLastEligibleSplitBreak != null){
					return Constants.LIMIT_BREAK_SPLIT_2;
				} else {
					return Constants.LIMIT_BREAK_SPLIT_1;
				}
				
			}
			
		}
		return Constants.LIMIT_BREAK;
	}
	
	public BreakTime getLastSpiltbreak(){
		return mLastEligibleSplitBreak;
	}
	
	public int getWeeklyDrivetime(){
		return mWeeklyDrive;
	}
	
	public int getWeeklyDriveLimit() {
		return Constants.LIMIT_WEEKLY_DRIVE;
	}
	
	public int getWTDWeeklyWorkLimit(){
		return Constants.LIMIT_WTD_WEEKLY_WORK;
	}
	
	
	public int getWTDWeeklyRestingLimit(){
		return Constants.LIMIT_WTD_WEEKLY_RESTING;
	}
	
	public int getWTDDailyRestingLimit(){
		return Constants.LIMIT_WTD_DAILY_RESTING;
	}
	
	public int getWTDNightShiftWorkingTimeLimit(){
		return Constants.LIMIT_WTD_NIGHT_SHIFT_WORKING;
	}
	
	public int getLastweekDrivingTime(){
		return mLastweekDrivetime;
	}
	
	public int getReducedWeekRestsLeft(){
		return mReducedWeekRestsLeft;
	}
	
	public int getReducedDailyRestsLeft(){
		return mReducedDailyRestsLeft;
	}
	
	public int getExtendedWorkDaysLeft(){
		return mExtendedWorkDaysLeft;
	}
	
	public int getExtendedDailyDrivesLeft(){
		return mExtendedDailyDrivesLeft;
	}


}
