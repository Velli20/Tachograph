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

package com.velli.tachograph.views;

import java.util.Locale;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

public class TextViewTimeCounter extends RobotoLightTextView {
    private long mStartTime = 0;
    private long mTimeNow = 0;
    private String mTitle;
    private Handler mHandler;
    
	public TextViewTimeCounter(Context context) {
		super(context, null, 0);
	}
	
	public TextViewTimeCounter(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public TextViewTimeCounter(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void startTimer(long start){
		if(mStartTime == start){
			return;
		} else if(mHandler != null && isTimerRunning()){
			mHandler.removeCallbacks(r);
			stopTimer();
		}
		
		mStartTime = start;
		mTimeNow = System.currentTimeMillis();
		
		mHandler = new Handler();
		mHandler.postDelayed(r, 1000);
		convertDatesToMinutes(start, mTimeNow);
	}
	
	public void stopTimer(){
		if(mHandler != null){
			mHandler = null;
			mStartTime = 0;
		    mTimeNow = 0;
		}
		setText("");
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public boolean isTimerRunning(){
		return mHandler == null? false : true;
	}
	
	
	Runnable r = new Runnable() {
	    @Override
	    public void run(){
	    	mTimeNow += 1000;
			convertDatesToMinutes(mStartTime, mTimeNow);
			if(mHandler != null){
				mHandler.postDelayed(r, 1000);
			}
	    }
	};
	

	
	public void convertDatesToMinutes(long start, long end){
		long secs = (end - start);
		long second = (secs / 1000) % 60;
		long minute = (secs / (1000 * 60)) % 60;
		long hour = (secs / (1000 * 60 * 60)) % 24;
		long days = (secs / (1000 * 60 * 60 )) / 24;
		String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
		if(days > 0){
			setText(mTitle != null ? mTitle + " " + days + "d " +time : days + "d " +time);
		} else {
			setText(mTitle != null ? mTitle + " " + time : time);
		}
	}

}
