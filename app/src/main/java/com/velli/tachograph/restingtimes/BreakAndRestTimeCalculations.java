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

import android.os.AsyncTask;

import com.velli.tachograph.Event;


public class BreakAndRestTimeCalculations {
	private static final boolean DEBUG = false;
	private static final String TAG = "BreakAndRestTimeCalculations ";
	
	public interface OnCalculatiosReadyListener {
		void onCalculationsReady(ArrayList<WeekHolder> list);
	}
	
	
	public void calculateBreakAndRestingtimes(ArrayList<Event> list, OnCalculatiosReadyListener l){
		new BackgroundTask(list, l).execute();
	}
	
	
	private class BackgroundTask extends AsyncTask<Void, Void, ArrayList<WeekHolder>> {
		private ArrayList<Event> mList;
		private WeakReference<OnCalculatiosReadyListener> mListener;
		
		private int mWeeklyDriveTime = 0;
    	private int mWeeklyWorkTime = 0;
    	private int mWtdWeeklyWorkTime = 0;
    	
    	private int mDailyDriveTime = 0;
    	private int mDailyWorkTime = 0;
    	private int mDailyRestTime = 0;
    	private int mDailyWtdWorkTime = 0;
    	
    	private double mWeeklyDrivenDistance = 0;
    	private double mDailyDrivenDistance;
    	
    	private long mLastBreak = -1;
    	private Event ev = null;
    	private Event previousEvent = null;
    	private WeekHolder currentWeek = null;
    	private WorkDayHolder currentDay = null;
    	private boolean mCalculateRestingAutomatically = false;
    	
		public BackgroundTask(ArrayList<Event> l, OnCalculatiosReadyListener listener){
    		mList = l;
    		mListener = new WeakReference<>(listener);
    	}
		
		@Override
		protected ArrayList<WeekHolder> doInBackground(Void... params) {
			final ArrayList<WeekHolder> weeklist = new ArrayList<>();
			final WorkDayCalculator workdayCalculator = new WorkDayCalculator();
			
			final int size = mList.size();
			
		    ev = null;
		    previousEvent = null;
		    currentWeek = null;
			currentDay = null;
			
            for(int i = 0; i < size; i++){
            	ev = mList.get(i);
            	
            	if(mCalculateRestingAutomatically && (previousEvent != null || i == size -1)) {
            		
            		
            		long difference = (previousEvent == null? System.currentTimeMillis() : previousEvent.getEndDateInMillis()) - ev.getStartDateInMillis();
            		int mins = (int)((difference / 1000) / 60);
            		
            		if(mins >= Constants.LIMIT_DAILY_REST_SPLIT_1) {
            			Event autoEvent = new Event();
            			
            			autoEvent.setStartDate(previousEvent == null? System.currentTimeMillis() : previousEvent.getEndDateInMillis());
            			autoEvent.setEndDate(ev.getStartDateInMillis());
            			
            			if(mins >= Constants.LIMIT_WEEKLY_REST_MIN) {
            				autoEvent.setEventType(Event.EVENT_TYPE_WEEKLY_REST);
            			} else {
            				autoEvent.setEventType(Event.EVENT_TYPE_DAILY_REST);
            			}
            			
            			ev = autoEvent;
            			i--;
            		}
            	}
            	
            	if(RestingTimesUtils.needToStartNewWeek(ev, currentWeek)){
            		WorkDayHolder pendingDay = null;
            		

            		
            		if(currentDay != null && !currentDay.isEnded()){
            			workdayCalculator
                		.setParamsCurrentDayHolder(currentDay)
                		.setParamsCurrentEvent(ev)
                		.setParamsCurrentWeekHolder(currentWeek)
                		.setParamsPreviousEvent(previousEvent)
                		.setDrivenDistance(mDailyDrivenDistance)
                		.setTimes(mDailyDriveTime, mDailyWorkTime, mDailyWtdWorkTime);
            			
            			if(currentWeek != null){
            				currentWeek.addWorkDay(workdayCalculator.endDay());
            			} else {
            				pendingDay = workdayCalculator.endDay();
            			}
            			currentDay = workdayCalculator.getNewWorkingDayHolder();
            			workdayCalculator.resetParams();
            			
            		}
            		
            		if(currentWeek != null && !weeklist.contains(currentWeek)){
            			weeklist.add(currentWeek);
            		}
            		
            		currentWeek = new WorkWeekCalculator()
            		.setParamCurrentWeek(currentWeek)
            		.setParamCurrentEvent(ev)
            		//.setWeekStartDate(ev.getStartDateInMillis())
            		.setWeeklyDrivenDistance(mWeeklyDrivenDistance)
            		.setTimeParams(mWeeklyDriveTime, mWeeklyWorkTime, mWtdWeeklyWorkTime)
            		.setParamPreviousEvent(previousEvent).execute();
            		
             		if(pendingDay != null){
            			currentWeek.addWorkDay(pendingDay);
            		}
            		
            		resetWeeklyTimes();
            		resetDailyTimes();
            	}
            	
            	if(RestingTimesUtils.needToStartNewDay(currentDay, ev)){

            		
            		workdayCalculator.setParamsCurrentDayHolder(currentDay)
            		.setParamsCurrentEvent(ev)
            		.setParamsCurrentWeekHolder(currentWeek)
            		.setParamsPreviousEvent(previousEvent)
            		.setDrivenDistance(mDailyDrivenDistance)
            		.setTimes(mDailyDriveTime, mDailyWorkTime, mDailyWtdWorkTime);
            		
            		if(currentDay != null && !currentDay.isEnded() && currentWeek != null){
            			currentWeek.addWorkDay(workdayCalculator.endDay());
            		} 
            		
            		currentDay = workdayCalculator.getNewWorkingDayHolder();
            		workdayCalculator.resetParams();
            		resetDailyTimes();
            	}
            	
            	mDailyDrivenDistance += ev.getDrivenDistance();
            	mWeeklyDrivenDistance += ev.getDrivenDistance();
            	
            	if(ev.getEventType() == Event.EVENT_TYPE_WEEKLY_REST){
            		int weeklyrest = RestingTimesUtils.getEventDurationInMin(ev);
            		
            		workdayCalculator
            		.setParamsCurrentDayHolder(currentDay)
            		.setParamsCurrentEvent(ev)
            		.setParamsCurrentWeekHolder(currentWeek)
            		.setParamsPreviousEvent(previousEvent)
            		.setDrivenDistance(mDailyDrivenDistance)
            		.setTimes(mDailyDriveTime, mDailyWorkTime, mDailyWtdWorkTime);
            		currentWeek.addWorkDay(workdayCalculator.endDay());
        			workdayCalculator.resetParams();
        			
        			
            		currentWeek.addWorkDay(RestingTimesUtils.getWeekRest(ev));
            		currentWeek.setWeeklyRest(weeklyrest);
            		
            	
            		WeekHolder newWeek = new WorkWeekCalculator()
            		.setParamCurrentEvent(ev)
            		.setParamCurrentWeek(currentWeek)
            		.setTimeParams(mWeeklyDriveTime, mWeeklyWorkTime, mWtdWeeklyWorkTime)
            		.setWeeklyRestingTime(weeklyrest)
            		.setParamPreviousWeeklyWasInvalid(weeklist.isEmpty() ? false : weeklist.get(weeklist.size() - 1).isReducedWeeklyRest())
            		.setWeeklyDrivenDistance(mWeeklyDrivenDistance)
            		.execute();
            		  
            		
            		weeklist.add(currentWeek);
            		
            		// Create a new dummy week
            		if(i == (size -1)){
            			currentWeek = newWeek;
            			currentWeek.setStart(ev.getEndDateInMillis());
            		}
            		resetDailyTimes();
            		resetWeeklyTimes();
            	} else if(ev.getEventType() == Event.EVENT_TYPE_DAILY_REST) {
            		mDailyRestTime = RestingTimesUtils.getEventDurationInMin(ev);
            		
            		workdayCalculator
            		.setParamsCurrentDayHolder(currentDay)
            		.setParamsCurrentEvent(ev)
            		.setParamsCurrentWeekHolder(currentWeek)
            		//.setParamsPreviousEvent(previousEvent)
            		.setDrivenDistance(mDailyDrivenDistance)
            		.setTimes(mDailyDriveTime, mDailyWorkTime, mDailyWtdWorkTime)
            		.setDailyRest(mDailyRestTime);
            		currentWeek.addWorkDay(workdayCalculator.endDay());
            		
     
            		workdayCalculator.resetParams();
            		resetDailyTimes();
            		
            	} else if (ev.getEventType() == Event.EVENT_TYPE_NORMAL_BREAK
            			|| ev.getEventType() == Event.EVENT_TYPE_SPLIT_BREAK
						|| ev.getEventType() == Event.EVENT_TYPE_OTHER_WORK
						|| ev.getEventType() == Event.EVENT_TYPE_POA) {
                    final int otherWork = RestingTimesUtils.getEventDurationInMin(ev);
            		
            		mDailyWorkTime += otherWork;
					mWeeklyWorkTime += otherWork;  
					
					if(ev.getEventType() == Event.EVENT_TYPE_OTHER_WORK){
						mWtdWeeklyWorkTime += otherWork;
						mDailyWtdWorkTime += otherWork;
					}
					
					if (ev.getEventType() == Event.EVENT_TYPE_NORMAL_BREAK) {
						
						final BreakTime b = RestingTimesUtils.getBreakTime(ev);
						final BreakTime lastBreak = currentDay.getLastBreak();

						if (b.getDuration() >= Constants.LIMIT_BREAK) {
							currentDay.setContiniousDriveTimeAfterBreak(0);
							mLastBreak = ev.getEndDateInMillis();
						} else if (lastBreak != null 
								&& lastBreak.isSplitBreak() 
								&& RestingTimesUtils.isLastSplitBreakEligible(lastBreak)) {
							
							if ((lastBreak.getDuration() + b.getDuration()) >= Constants.LIMIT_BREAK) {
								currentDay.setContiniousDriveTimeAfterBreak(0);
								mLastBreak = ev.getEndDateInMillis();
								/** Set last split as used */
							} else if(lastBreak.getDuration() >= Constants.LIMIT_BREAK_SPLIT_1){
								currentDay.setContiniousDriveTimeAfterBreak(0);
								mLastBreak = ev.getEndDateInMillis();
							}
						}
						currentDay.addNewBreakTime(b);
					}
					
					if(ev.isRecordingEvent()){
            			currentDay.setRecording(true);
            			currentDay.setRecordStartTime(ev.getStartDateInMillis());
            		} 
            		
            	} else if(ev.getEventType() == Event.EVENT_TYPE_DRIVING){
            		int drive = RestingTimesUtils.getEventDurationInMin(ev);
            		int contDriveTime = currentDay.getContiniousDriveTimeAfterBreak();
            		

            		
            		if(drive >= 0) {
						mDailyWorkTime += drive;
						mDailyDriveTime += drive;
						mWeeklyDriveTime += drive;
						mWeeklyWorkTime += drive;
						mWtdWeeklyWorkTime += drive;
						mDailyWtdWorkTime += drive;
						if(ev.isRecordingEvent()){
	            			currentDay.setRecording(true);
	            			currentDay.setContiniousDriveTimeAfterBreak(drive + contDriveTime);
	            			
	            		} else if(mLastBreak == -1 || ev.getStartDateInMillis() >= mLastBreak) {
	            			currentDay.setContiniousDriveTimeAfterBreak(drive + contDriveTime);
	            		}
					}
            		if(ev.isRecordingEvent()){
            			currentDay.setRecording(true);
            			currentDay.setRecordStartTime(ev.getStartDateInMillis());
            		} 
            	}
            	
            	previousEvent = ev;
            }
            
            if(currentDay != null && !currentDay.isEnded() && ev != null){
            	workdayCalculator
        		.setParamsCurrentDayHolder(currentDay)
        		.setParamsCurrentEvent(ev)
        		.setParamsCurrentWeekHolder(currentWeek)
        		.setParamsPreviousEvent(null)
        		.setTimes(mDailyDriveTime, mDailyWorkTime, mDailyWtdWorkTime)
        		.setDrivenDistance(mDailyDrivenDistance)
        		.setDailyRest(mDailyRestTime);
        		currentWeek.addWorkDay(workdayCalculator.endDay());
    		}
            
            if(currentWeek != null && !weeklist.contains(currentWeek)){
            	new WorkWeekCalculator()
        		.setParamCurrentWeek(currentWeek)
        		.setParamCurrentDay(currentDay)
        		.setParamCurrentEvent(ev)
        		.setTimeParams(mWeeklyDriveTime, mWeeklyWorkTime, mWtdWeeklyWorkTime)
        		.setWeeklyDrivenDistance(mWeeklyDrivenDistance)
        		.setParamPreviousEvent(previousEvent)
        		.execute();
            	weeklist.add(currentWeek);
            }
			return weeklist;
		}
		
		public void resetDailyTimes(){
			mDailyDriveTime = 0;
	    	mDailyWorkTime = 0;
	    	mDailyRestTime = 0;
	    	mDailyWtdWorkTime = 0;
	    	mLastBreak = -1;
	    	mDailyDrivenDistance = 0;
		}
		
		public void resetWeeklyTimes(){
			mWeeklyDriveTime = 0;
	    	mWeeklyWorkTime = 0;
	    	mWtdWeeklyWorkTime = 0;
	    	mWeeklyDrivenDistance = 0; 
		}
		
		@Override
		protected void onPostExecute(ArrayList<WeekHolder> list){
			if(mListener != null && mListener.get() != null){
				mListener.get().onCalculationsReady(list);
			}
		}
		
	}
	
}
