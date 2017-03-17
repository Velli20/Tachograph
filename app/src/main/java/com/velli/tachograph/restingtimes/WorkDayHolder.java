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

public class WorkDayHolder {
	private long start = -1;
	private long end = -1;
	private long mRecordStartTime = -1;
	
	private int totalDriveTime = 0;
	private int dailyRest = 0;
	private int totalWorkingTime = 0;
	private int dayType;
	private int continiousDriveTimeAfterBreak = 0;
	private int wtdDailyResting = 0;
	private int wtdDailyWorkingTime = 0;
	private double mDrivenDistance = 0;
	
	private boolean invalidDailyrest = false;
	private boolean reducedDailyrest = false;
	private boolean extendedDailyDrive = false;
	private boolean extendedDailyWork = false;
	private boolean dailyWorktimeExceeded = false;
    private boolean dailyDrivetimeExceeded = false;
    private boolean isEnded = false;
    private boolean isRescording = false;
    private boolean isDriving = false;
    
    public static final int TYPE_WEEK_REST = 10;
	public static final int TYPE_WORK_DAY = 11;
	
	
	private ArrayList<BreakTime> mBreaks = new ArrayList<>();
	
	public WorkDayHolder(){
	
	}
	
	public void setStart(long starttime){
		start = starttime;
	}
	
	public void setEnd(long endtime){
		end = endtime;
		isEnded = true;
	}
	
	public void setDailyDrivetime(int mins){
		this.totalDriveTime = mins;
	}
	
	public void setDailyRestTime(int mins){
		this.dailyRest = mins;
	}
	
	public void setDailyWorktime(int mins){
		this.totalWorkingTime = mins;
	}
	
	public void setInvalidDailyrest(boolean invalid){
		invalidDailyrest = invalid;
	}
	
	public void setReducedDailyrest(boolean reduced){
		reducedDailyrest = reduced;
	}
	
	public void setExtendedDailyWork(boolean extended){
		extendedDailyWork = extended;
	}
	
	public void setDailyDrivetimeExtended(boolean extended){
		extendedDailyDrive = extended;
	}
	
	public void setDailyDrivetimeExceeded(boolean exceeded){
		dailyDrivetimeExceeded = exceeded;
	}
	
	public void setDailyWorktimeExceeded(boolean exceeded){
		dailyWorktimeExceeded = exceeded;
	}
	
	public void setContiniousDriveTimeAfterBreak(int minutes){
		continiousDriveTimeAfterBreak = minutes;
		isDriving = minutes > 0;
	}
	
	public void setDayType(int which){
		dayType = which;
	}
	
	public void setRecording(boolean rec){
		isRescording = rec;
	}
	
	public void setWtdDailyRestingTime(int time){
		wtdDailyResting = time;
	}
	
	public void setWtdWorkingTime(int time){
		wtdDailyWorkingTime = time;
	}
	
	public void setRecordStartTime(long start){
		mRecordStartTime = start;
	}
	
	public void setDailyDrivenDistance(double distance) {
		mDrivenDistance = distance;
	}
	
	public void addNewBreakTime(BreakTime breaktime){
		if(breaktime != null){
			mBreaks.add(breaktime);
		}
	}
	
	public int getDailyDrivetime(){
		return totalDriveTime;
	}
	
	public int getDailyRest(){
		return dailyRest;
	}
	
	public int getTotalWorktime(){
		return totalWorkingTime;
	}
	
	public int getContiniousDriveTimeAfterBreak(){
		return continiousDriveTimeAfterBreak;
	}
	
	public int getWtdDailyRestingTime(){
		return wtdDailyResting;
	}
	
	public int getWtdDailyWorkingTime(){
		return wtdDailyWorkingTime;
	}
	
	public long getWorkdayStart(){
		return start;
	}
	
	public long getWorkdayEnd(){
		return end;
	}
	
	public long getRecordStartTime(){
		return mRecordStartTime;
	}
	
	public double getDailyDrivenDistance() {
		return mDrivenDistance;
	}
	
	public boolean isEmpty(){
		return (totalDriveTime == 0 && dailyRest == 0 && totalWorkingTime == 0 && !isRescording);
	}
	
	public boolean isInvalidDailyrest(){
		return invalidDailyrest;
	}
	
	public boolean isReducedDailyrest(){
		return reducedDailyrest;
	}
	
	public boolean isDailyDrivetimeExceeded(){
		return dailyDrivetimeExceeded;
	}
	
	public boolean isDailyworktimeExceeded(){
		return dailyWorktimeExceeded;
	}
	
	public boolean isExtendedDailyDrive() {
		return extendedDailyDrive;
	}
	
	public boolean isExtendedDailyWork(){
		return extendedDailyWork;
	}
	
	public boolean isWeeklyRest(){
		return dayType == TYPE_WEEK_REST;
	}
	
	public boolean isEnded(){
		return isEnded;
	}
	
	public boolean isRecording(){
		return isRescording;
	}
	
	public boolean isDriving(){
		return isDriving;
	}
	
	public BreakTime getLastBreak(){
		if(!mBreaks.isEmpty()){
			return mBreaks.get(mBreaks.size()-1);
		}
		return null;
	}
	
	public ArrayList<BreakTime> getBreakTimes(){
		return mBreaks;
	}
	
	
}
