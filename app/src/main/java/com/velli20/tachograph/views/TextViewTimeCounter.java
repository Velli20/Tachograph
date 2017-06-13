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
    private boolean mCountDown;

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

	public void countDown(long dateEnd) {
        mCountDown = true;
        if(mHandler != null && isTimerRunning()){
            mHandler.removeCallbacks(r);
            stopTimer();
        }

        mStartTime = dateEnd;

        mHandler = new Handler();
        mHandler.postDelayed(r, 1000);

        long now = System.currentTimeMillis();

        setTimerText(now, Math.max(now, dateEnd));
    }


	public void countUp(long dateStart){
        mCountDown = false;
		if(mHandler != null && isTimerRunning()){
			mHandler.removeCallbacks(r);
			stopTimer();
		}
		
		mStartTime = dateStart;

		mHandler = new Handler();
		mHandler.postDelayed(r, 1000);
		setTimerText(dateStart, System.currentTimeMillis());
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
            long now = System.currentTimeMillis();
            if(mCountDown) {
                setTimerText(now, Math.max(now, mStartTime));
            } else {
                setTimerText(mStartTime, now);
            }
			if(mHandler != null){
				mHandler.postDelayed(r, 1000);
			}
	    }
	};
	

	
	private void setTimerText(long start, long end){
		long secs = (end - start);
		long second = (secs / 1000) % 60;
		long minute = (secs / (1000 * 60)) % 60;
		long hour = (secs / (1000 * 60 * 60)) % 24;
		long days = (secs / (1000 * 60 * 60 )) / 24;
        String time;

        if(days > 0 || hour > 0) {
            /* Concat seconds */
            time = String.format(Locale.getDefault(), "%d h %d min", hour, minute);
        } else if(minute == 0 && hour == 0 && days == 0){
            time = String.format(Locale.getDefault(), "%d s", second);
        } else {
            time = String.format(Locale.getDefault(), "%d min %d s", minute, second);
        }

		setText((mTitle != null) ? String.format(Locale.getDefault(), mTitle, time) : time);

	}

}
