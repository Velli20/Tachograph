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

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

public class LockableScrollview extends HorizontalScrollView {
	private OnScrollListener mListener;
	
    public interface OnScrollListener{
    	void onScrollListener(float x, float y, boolean bottomReached);
    	void onScrollingStarted(float x, float y);
    	void onScrollingStopped(float x, float y, boolean bottomReached);
    }
    
	public LockableScrollview(Context context) {
		this(context, null, 0);
	}
	
	public LockableScrollview(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public LockableScrollview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	private boolean mScrollable = true;

    public void setScrollingEnabled(boolean enabled) {
        mScrollable = enabled;
    }

    public boolean isScrollable() {
        return mScrollable;
    }
    
    public void setOnScrollListener(OnScrollListener listener){
    	mListener = listener;
    	
    }

    @SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	if(mListener != null){
            		mListener.onScrollingStarted(ev.getRawX(), ev.getRawY());
            	}
                if (mScrollable) return super.onTouchEvent(ev);
                return mScrollable; // mScrollable is always false at this point
            case MotionEvent.ACTION_UP:
            	if(mListener != null){
            		mListener.onScrollingStopped(ev.getRawX(), ev.getRawY(), isBottomReached());
            	}
            	return super.onTouchEvent(ev);
            case MotionEvent.ACTION_MOVE:
            	if(mListener != null){
            		mListener.onScrollListener(ev.getRawX(), ev.getRawY(), isBottomReached());
            	}
                return super.onTouchEvent(ev);
            default:
            	return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if 
        // we are not scrollable
        if (!mScrollable) return false;
        else return super.onInterceptTouchEvent(ev);
    }
    
  
    
    public boolean isBottomReached(){
    	View view = getChildAt(getChildCount() - 1);
        int diff = (view.getRight() - (getWidth() + getScrollX()));

        // if diff is zero, then the bottom has been reached
        return diff == 0;
    }



}
