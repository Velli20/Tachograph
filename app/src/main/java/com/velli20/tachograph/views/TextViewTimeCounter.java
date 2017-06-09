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

package com.velli20.tachograph.views;

import java.util.Locale;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

public class TextViewTimeCounter extends RobotoLightTextView {
    private long mStartTime = 0;
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
		if(mHandler != null && isTimerRunning()){
			mHandler.removeCallbacks(r);
			stopTimer();
		}
		
		mStartTime = start;

		mHandler = new Handler();
		mHandler.postDelayed(r, 1000);
		setTimerText(start, System.currentTimeMillis());
	}
	
	public void stopTimer(){
		if(mHandler != null){
			mHandler = null;
			mStartTime = 0;
		}
	}

	public void setTitle(String title) {
		mTitle = title;
		setTimerText(mStartTime == 0? System.currentTimeMillis() : mStartTime, System.currentTimeMillis());
	}

	public boolean isTimerRunning(){
		return mHandler != null;
	}
	
	
	Runnable r = new Runnable() {
	    @Override
	    public void run(){
			setTimerText(mStartTime, System.currentTimeMillis());
			if(mHandler != null){
				mHandler.postDelayed(r, 1000);
			}
	    }
	};
	

	
	public void setTimerText(long start, long end){
		long secs = (end - start);
		long second = (secs / 1000) % 60;
		long minute = (secs / (1000 * 60)) % 60;
		long hour = (secs / (1000 * 60 * 60)) % 24;
		long days = (secs / (1000 * 60 * 60 )) / 24;
		String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
		if(days > 0){
			setText((mTitle != null) ? String.format(Locale.getDefault(), "%s %d d %s", mTitle, days, time) : (days + "d " + time));
		} else {
			setText((mTitle != null) ? String.format(Locale.getDefault(), "%s %s", mTitle, time) : time);
		}
	}

}
