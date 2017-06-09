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

package com.velli20.tachograph.restingtimes;

import java.util.ArrayList;

public class WeekHolder {
	private long weekStart;
	private long weekEnd;
	
	private int mWeeklyDrivingTimeInMinutes = 0;
	private int mWeeklyRestInMinutes = 0;
	private int mWeeklyWorkingTimeInMinutes = 0;
    private int mWeeklyOtherWorkingTimeInMinutes = 0;
    private int mWeeklyPoaTime = 0;
	private int mWtdWeeklyWorkingTime = 0;
	private int mReducedDailyRests = 0;
	private int mExtendedDrivingDaysUsed = 0;

	private double mWeeklyDrivenDistance = 0;

	private final ArrayList<WorkDayHolder> workdays = new ArrayList<>();
	private ArrayList<Integer> mEventIds;
	
	public WeekHolder(){ }
	
	public void setStartDate(long start){ weekStart = start; }
	
	public void setEndDate(long end){ weekEnd = end; }
	
	public void setWeeklyDrivingTime(int minutes){ mWeeklyDrivingTimeInMinutes = minutes; }

    public void setWeeklyOtherWorkingTime(int minutes) { mWeeklyOtherWorkingTimeInMinutes = minutes; }

	public void setWeeklyWorkingTime(int minutes){ mWeeklyWorkingTimeInMinutes = minutes; }

    public void setWeeklyPoaTime(int minutes) { mWeeklyPoaTime = minutes; }

	public void setWeeklyRest(int minutes){
		mWeeklyRestInMinutes = minutes;
	}

	public void setWtdWeeklyWorkingTime(int minutes){
		mWtdWeeklyWorkingTime = minutes;
	}

	public void setWeeklyDrivenDistance(double distance) { mWeeklyDrivenDistance = distance; }

    public void setReducedDailyRest(int reducedDailyRests){ mReducedDailyRests = reducedDailyRests; }

    public void setExtendedDrivingDaysUsed(int extendedDaysUsed) { mExtendedDrivingDaysUsed = extendedDaysUsed; }

    public void setEventIds(ArrayList<Integer> ids) { mEventIds = ids; }

	public long getStartDate(){
		return weekStart;
	}
	
	public long getEndDate(){
		return weekEnd;
	}
	
	public int getWeeklyDrivingTime(){
		return mWeeklyDrivingTimeInMinutes;
	}

    public int getWeeklyOtherWorkingTime() { return mWeeklyOtherWorkingTimeInMinutes; }

	public int getWeeklyWorkingTime(){
		return mWeeklyWorkingTimeInMinutes;
	}

    public int getWeeklyPoaTime() { return mWeeklyPoaTime; }
	
	public int getWeeklyRest(){
		return mWeeklyRestInMinutes;
	}

	public int getWtdWeeklyWorkingTime(){ return mWtdWeeklyWorkingTime; }

    public int getReducedDailyRests() {return mReducedDailyRests;}

    public int getExtendedDrivingDaysUsed(){
        return mExtendedDrivingDaysUsed;
    }

    public double getWeeklyDrivenDistance(){
        return mWeeklyDrivenDistance;
    }

    public void addWorkDay(WorkDayHolder day){
		if(day == null) {
			return;
		}
		workdays.add(day);
	}
	
	public ArrayList<WorkDayHolder> getWorkdaysList(){
		return workdays;
	}

	public ArrayList<Integer> getEventIds() { return mEventIds; }
	
	public ArrayList<WorkDayHolder> getWorkDays(){
		return workdays;
	}
	
	

}
