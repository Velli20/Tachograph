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


import com.velli.tachograph.DateCalculations;
import com.velli.tachograph.Event;

public class RestingTimesUtils {

	
	/** Check that last split resting period occurred in last 4,5 hour time span */
	public static boolean isLastSplitBreakEligible(BreakTime breaktime){
		final long current = System.currentTimeMillis();
		final long lastBreak = breaktime.getEndTime();
		
		final int a = DateCalculations.getTimeDifferenceInMins(lastBreak, current, -1);
		
		if(a <= Constants.LIMIT_CONTINIOUS_DRIVING){
			if(breaktime.getDuration() >= Constants.LIMIT_BREAK_SPLIT_1){
				return true;
			} 
		}
		return false;
	}
	
	/** Get Event duration in minutes */
	public static int getEventDurationInMin(Event ev){
		return DateCalculations.getTimeDifferenceInMins(ev.getStartDateInMillis(), ev.getEndDateInMillis(), -1);
	}
	
	/** Check if working day started over 24 hours ago */
	public static boolean needToStartNewDay(WorkDayHolder currentDay, Event ev){
		if(currentDay == null || (currentDay != null && currentDay.isEnded())){
			return true;
		}
		return currentDay.getWorkdayStart() != -1 
				&& (ev.getStartDateInMillis() - currentDay.getWorkdayStart() >= Constants.ONE_DAY_PERIOD_IN_MILLIS);
	}
	
	/** Check if week started over 7 days ago */
	public static boolean needToStartNewWeek(Event ev, WeekHolder currentweek){
		if(currentweek == null || currentweek.isEnded()){
			return true;
		} else {
			return currentweek.getWeekStart() != -1 && ev.getStartDateInMillis() - currentweek.getWeekStart() >= Constants.SEVEN_DAYS_PERIOD_IN_MILLIS;
		}
	}

	
	/** Create a new weekly rest 
	 * @param restingEvent event
	 * @return New WorkDayHolder based on resting Event*/
	public static WorkDayHolder getWeekRest(Event restingEvent){
		final WorkDayHolder weeklyRest = new WorkDayHolder();
		
		weeklyRest.setDayType(WorkDayHolder.TYPE_WEEK_REST);
		weeklyRest.setStart(restingEvent.getStartDateInMillis());
		weeklyRest.setEnd(restingEvent.getEndDateInMillis());
		weeklyRest.setDailyRestTime(getEventDurationInMin(restingEvent));
		return weeklyRest;
	}
	
	/** Create a new break 
	 * 
	 * @param ev a break Event
	 * @return New BreakTime based on break Event
	 */
	public static BreakTime getBreakTime(Event ev){
		final BreakTime breaktime = new BreakTime();
		final int duration = getEventDurationInMin(ev);
		
		breaktime.setStart(ev.getStartDateInMillis());
		breaktime.setEnd(ev.getEndDateInMillis());
		breaktime.setDuration(getEventDurationInMin(ev));
		breaktime.setRecording(ev.isRecordingEvent());
		
		if(duration < Constants.LIMIT_BREAK && !ev.isRecordingEvent()){
			if(duration >= Constants.LIMIT_BREAK_SPLIT_2){
				breaktime.setAsSplitBreak(true);
			} else if(duration >= Constants.LIMIT_BREAK_SPLIT_1 && isLastSplitBreakEligible(breaktime)){
				breaktime.setAsSplitBreak(true);
			}
		} else if(ev.isSplitBreak()){
			breaktime.setAsSplitBreak(true);
		}
		
		return breaktime;
	}
}
