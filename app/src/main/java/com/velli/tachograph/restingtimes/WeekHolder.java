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

import java.util.ArrayList;

public class WeekHolder {
	private long weekStart;
	private long weekEnd;
	
	private int weeklyDrivetime = 0;
	private int weeklyRest = 0;
	private int weeklyWorktime = 0;
	
	private int wtdWeeklyRestingTime = 0;
	private int wtdWeeklyWorkingTime = 0;
	
	private int reducedDailyRestsLeft = 3;
	private int extendedDailyWorktimesLeft = 3;
	private int extendedDailyDrivetimesLeft = 2;
	
	private double mWeeklyDrivenDistance = 0;
	
	private boolean invalidWeeklyRest = false;
	private boolean reducedWeeklyRest = false;
	private boolean isEnded = false;


	private final ArrayList<WorkDayHolder> workdays = new ArrayList<>();
	
	public WeekHolder(){

	}
	
	public void setStart(long start){
		weekStart = start;
	}
	
	public void setEnd(long end){
		weekEnd = end;
		isEnded = true;
	}
	
	public void setWeeklyDrivetime(int mins){
		weeklyDrivetime = mins;
	}
	
	public void setWeeklyWorktime(int mins){
		weeklyWorktime = mins;
	}
	
	public void setInvalidWeeklyrest(boolean invalid){
		invalidWeeklyRest = invalid;
	}
	
	public void setReducedWeeklyRest(boolean reduced){
		reducedWeeklyRest = reduced;
	}
	
	public void setWeeklyRest(int mins){
		weeklyRest = mins;
	}
	
	public void setWtdWeeklyRestingTime(int mins){
		wtdWeeklyRestingTime = mins;
	}
	
	public void setWtdWeeklyWorkingTime(int mins){
		wtdWeeklyWorkingTime = mins;
	}
	
	public void setWeeklyDrivenDistance(double distance) {
		mWeeklyDrivenDistance = distance;
	}
	
	public long getWeekStart(){
		return weekStart;
	}
	
	public long getWeekEnd(){
		return weekEnd;
	}
	
	public int getWeeklyDrivetime(){
		return weeklyDrivetime;
	}
	
	public int getWeeklyWorktime(){
		return weeklyWorktime;
	}
	
	public int getWeeklyrest(){
		return weeklyRest;
	}
	
	public int getReducedDailyrestLeft(){
		return reducedDailyRestsLeft;
	}
	
	public int getWtdWeeklyRestingTime(){
		return wtdWeeklyRestingTime;
	}
	
	public int geWtdtWeeklyWorkingTime(){
		return wtdWeeklyWorkingTime;
	}
	
	public void addWorkDay(WorkDayHolder day){
		if(day == null) {
			return;
		}
		if(day.isReducedDailyrest()){
			reducedDailyRestsLeft -= 1;
		}
		if(day.isExtendedDailyDrive()){
			extendedDailyDrivetimesLeft -= 1;
		}
		if(day.isExtendedDailyWork()){
			extendedDailyWorktimesLeft -= 1;
		}
		workdays.add(day);
	}
	
	public ArrayList<WorkDayHolder> getWorkdaysList(){
		return workdays;
	}
	
	public boolean isInvalidWeeklyrest(){
		return invalidWeeklyRest;
	}

	public boolean isReducedWeeklyRest(){
		return reducedWeeklyRest;
	}
	
	public boolean isEnded(){
		return isEnded;
	}
	
	public boolean hasExtendedDailyWorktimesLeft(){
		return extendedDailyWorktimesLeft > 0;
	}
	
	public boolean hasExtendedDailyDrivetimesLeft(){
		return extendedDailyDrivetimesLeft > 0;
	}
	
	public int getExtendedDailyWorktimesLeft(){
		return extendedDailyWorktimesLeft;
	}
	
	public int getExtendedDailyDrivetimesLeft(){
		return extendedDailyDrivetimesLeft;
	}
	
	public double getWeeklyDrivenDistance(){
		return mWeeklyDrivenDistance;
	}
	
	public boolean hasReducedDailyrestLeft(){
		return reducedDailyRestsLeft > 0;
	}
	
	public ArrayList<WorkDayHolder> getWorkDays(){
		return workdays;
	}
	
	

}
