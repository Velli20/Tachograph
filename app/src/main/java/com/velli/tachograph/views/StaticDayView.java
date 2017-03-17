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


import java.util.ArrayList;




import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import com.velli.tachograph.R;
import com.velli.tachograph.DateCalculations;
import com.velli.tachograph.Event;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class StaticDayView extends View {
	private static final String TAG = "StaticDayView ";
	private static final boolean DEBUG = true;
	
    private Paint mTimeLinePaint = new Paint();
    private Paint mEventFillPaint = new Paint();
    private Paint mEventTimeLinePaint = new Paint();
    private Paint mNowLinePaint = new Paint();
    
    private TextPaint mTimeLabelPaint = new TextPaint();
    private TextPaint mEventLabelPaint = new TextPaint();
    
    private Handler mHandler;
    
    private static final int HOUR_CIRCLE_SIZE = 3;
    private static final int MINUTE_CIRCLE_SIZE = 2;
    private static final int NOW_CIRCLE_SIZE = 6;
    private static final int BOTTOM_PADDING = 16;
    private static final int TOP_PADDING = 16;
    private static final int EVENT_LINE_HEIGHT = 8;
    
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private int mEventColors[];
    
    private float scale = 0;
    private float mPointerX;
    private float m24hLineStart;
    private float m24hLineEnd;
    private float mEventLineStrokeWidth;
    
    private long mTodayStartInMillis = 0;
    private long mTodayEndInMillis = 0;
    
    private boolean mSizeChanged = false;
    private boolean mTouching = false;
    
    private ArrayList<Event> mEvents;
    private HashMap<Integer, RectF> mRects = new HashMap<>();
    private String[] mEventExplanations;
    private RectF mNowRect = new RectF();
    private Event mRecordingEvent;


	public StaticDayView(Context context) {
		this(context, null, 0);
	}
	
	public StaticDayView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public StaticDayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Resources res = getResources();
		
		mEventExplanations = res.getStringArray(R.array.event_explanations);
		scale = res.getDisplayMetrics().density;
		mTodayStartInMillis = DateCalculations.getTodayStartTime();
		mTodayEndInMillis = DateCalculations.getTodayEndTime();
		
		mTimeLinePaint.setColor(Color.BLACK);
		mTimeLinePaint.setStyle(Style.FILL);
		mTimeLinePaint.setAlpha(31);
		
		mEventTimeLinePaint.setStrokeWidth(convertFromPxToDip(1f));
		mEventTimeLinePaint.setStyle(Style.STROKE);
		
		mTimeLabelPaint.setColor(res.getColor(R.color.gray));
		mTimeLabelPaint.setTextSize(convertFromPxToDip(12));
		mTimeLabelPaint.setTextAlign(Align.CENTER);
		mTimeLabelPaint.setAntiAlias(true);
		
		mEventLabelPaint.setColor(res.getColor(R.color.gray));
		mEventLabelPaint.setTextSize(convertFromPxToDip(14));
		mEventLabelPaint.setTextAlign(Align.CENTER);
		mEventLabelPaint.setAntiAlias(true);
		
		mEventLineStrokeWidth = convertFromPxToDip(EVENT_LINE_HEIGHT);
		
		mEventFillPaint.setStyle(Style.STROKE);
		mEventFillPaint.setAntiAlias(true);
		mEventFillPaint.setStrokeWidth(mEventLineStrokeWidth);
		mEventFillPaint.setStrokeCap(Cap.ROUND);
		
		mNowLinePaint.setStyle(Style.STROKE);
		mNowLinePaint.setAntiAlias(true);
		mNowLinePaint.setStrokeWidth(convertFromPxToDip(2));
		mNowLinePaint.setColor(res.getColor(R.color.gray));
		
		mEventColors = context.getResources().getIntArray(R.array.event_colors);
		
		mHandler = new Handler();
		mHandler.postDelayed(mEventUpdater, 1000);

		
		
		setWillNotDraw(false);
	}


	
	private float convertFromPxToDip(float value){
		return scale * value;
	}


	private Runnable mEventUpdater = new Runnable() {
	    @Override
	    public void run(){
	    	invalidate();
			if(mHandler != null){
				mHandler.postDelayed(mEventUpdater, 1000);
			}
	    }
	};
	

	
	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);

		
		mViewWidth = (int) getViewWidthWithoutPadding();
		mViewHeight = getHeight();
	
		
		drawDrivingEvents(canvas);
		
		if(mTouching) {
			drawPointerLine(canvas);
		} else {
			drawNowLine(canvas);
		}
		
		drawTimeLine(canvas);
	}
	
	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		if(DEBUG){
			Log.i(TAG, TAG + "onSizeChanged()");
		}
		mSizeChanged = true;
	}
	
	private float getViewWidthWithoutPadding(){
		return getWidth() - getLeftPadding() - getRightPadding();
	}
	
	private float getLeftPadding(){
		return mTimeLabelPaint.getTextSize();
	}
	
	private float getRightPadding() {
		return mTimeLabelPaint.getTextSize();
	}
	
	private float getTopPadding(){
		return convertFromPxToDip(TOP_PADDING);
	}
	
	private void drawNowLine(Canvas canvas) {
		mNowLinePaint.setStrokeWidth(convertFromPxToDip(2));
		mNowLinePaint.setColor(getResources().getColor(R.color.gray));
		
		float leftPadding = getLeftPadding();
		float rightPadding = getRightPadding();
		float outMax = (getLeftPadding() + mViewWidth);
		float outMin = getLeftPadding();
		float timeLineBottomPadding = convertFromPxToDip(BOTTOM_PADDING) + convertFromPxToDip(5);
		
		float cx = (System.currentTimeMillis() - mTodayStartInMillis) * (outMax - outMin) / (mTodayEndInMillis - mTodayStartInMillis) + outMin;
		float cy = mViewHeight - timeLineBottomPadding;
		
		float textPadding = convertFromPxToDip(4);
		float circleRadius = convertFromPxToDip(NOW_CIRCLE_SIZE) / 2;
		
		StringBuilder b = new StringBuilder();
		
		if(mRecordingEvent != null && mRecordingEvent.isRecordingEvent()) {
			b.append(getResources().getString(R.string.title_now) + ": ");
			b.append(mEventExplanations[mRecordingEvent.getEventType()]);
			b.append(", ");
			b.append(convertDatesToMinutes(mRecordingEvent.getStartDateInMillis(), mRecordingEvent.getEndDateInMillis()));
		} else {
			b.append(getResources().getString(R.string.title_now));
		}
		
		float labelLenght = mTimeLabelPaint.measureText(b.toString());
		float textLeftPadding = 0;
		float textRightPadding = 0;
		
		if(cx - leftPadding < (labelLenght / 2)) {
			textLeftPadding =  +((labelLenght / 2) - (cx - leftPadding));
		} else if((mViewWidth + rightPadding) - cx  < (labelLenght / 2)) {
			textRightPadding = (labelLenght / 2) - ((mViewWidth + rightPadding) - cx);
		}
		
		canvas.drawLine(cx, getTopPadding() + textPadding, cx, cy - circleRadius, mNowLinePaint);
		canvas.drawCircle(cx, cy, circleRadius, mNowLinePaint);
		canvas.drawText(b.toString(), (textLeftPadding + cx) - textRightPadding, getTopPadding(), mTimeLabelPaint);
		
		mNowRect.set(cx - (circleRadius * 2), getTopPadding(), cx + (circleRadius * 2), cy);
	}
	
	
	private void drawPointerLine(Canvas canvas) {
		mNowLinePaint.setStrokeWidth(convertFromPxToDip(1));
		mNowLinePaint.setColor(getResources().getColor(R.color.gray));
		float leftPadding = getLeftPadding();
		float rightPadding = getRightPadding();
		float timeLineBottomPadding = convertFromPxToDip(BOTTOM_PADDING) + convertFromPxToDip(5);
		float cy = mViewHeight - timeLineBottomPadding;
		float textPadding = convertFromPxToDip(4);
		float circleRadius = convertFromPxToDip(NOW_CIRCLE_SIZE) / 2;
		
		long millis = (long) ((mPointerX - m24hLineStart) * (mTodayEndInMillis - mTodayStartInMillis) / (m24hLineEnd - m24hLineStart)) + mTodayStartInMillis;
		
		if(millis < mTodayStartInMillis) {
			millis = mTodayStartInMillis;
		}
		StringBuilder label = new StringBuilder();
		for(Event ev : mEvents) {
			if(millis >= ev.getStartDateInMillis() && millis <= ev.getEndDateInMillis()) {
				label.append(mEventExplanations[ev.getEventType()]);
				label.append(" (" + DateCalculations.convertDatesInHours(ev.getStartDateInMillis(), ev.getEndDateInMillis()));
				label.append("), ");
				mNowLinePaint.setColor(mEventColors[ev.getEventType()]);
				mNowLinePaint.setStrokeWidth(convertFromPxToDip(2));
				break;
			}
		}
		label.append(getTimeLabel(millis));
		
		float labelLenght = mTimeLabelPaint.measureText(label.toString());
		float textLeftPadding = 0;
		float textRightPadding = 0;
		
		if(mPointerX - leftPadding < (labelLenght / 2)) {
			textLeftPadding =  +((labelLenght / 2) - (mPointerX - leftPadding));
		} else if((mViewWidth + rightPadding) - mPointerX  < (labelLenght / 2)) {
			textRightPadding = (labelLenght / 2) - ((mViewWidth + rightPadding) - mPointerX);
		}
		canvas.drawLine(mPointerX, getTopPadding() + textPadding, mPointerX, cy - circleRadius, mNowLinePaint);
		canvas.drawCircle(mPointerX, cy, circleRadius, mNowLinePaint);
		canvas.drawText(label.toString(), (textLeftPadding + mPointerX) - textRightPadding, getTopPadding(), mTimeLabelPaint);
	}
	
	private void drawTimeLine(Canvas canvas){
		float leftPadding = getLeftPadding();
		
		float textBottomPadding = convertFromPxToDip(BOTTOM_PADDING);
		float timeLineBottomPadding = convertFromPxToDip(BOTTOM_PADDING) + convertFromPxToDip(5);
		float hourCircleSize = convertFromPxToDip(HOUR_CIRCLE_SIZE);
		float minuteCircleSize = convertFromPxToDip(MINUTE_CIRCLE_SIZE);
		
		int hourCircleInterval = mViewWidth / 4;
		int hourCircleIntervalRem = mViewWidth % 4;
		
		hourCircleInterval += hourCircleIntervalRem / 4;
		
		float hourCy = mViewHeight - timeLineBottomPadding;
		float textCy = mViewHeight -  textBottomPadding + mTimeLabelPaint.getTextSize();
		
		for(int i = 0; i <= 4; i++){
			float hourCx = (i * hourCircleInterval) + leftPadding;
			
			if(i == 0) {
				m24hLineStart = hourCx;
			} else if(i == 4) {
				m24hLineEnd = hourCx;
			}
			canvas.drawCircle(hourCx, hourCy, hourCircleSize / 2, mTimeLinePaint);
			canvas.drawText(String.valueOf(i * 6), hourCx, textCy, mTimeLabelPaint);

			int minuteCircleCount = (int) (((float) hourCircleInterval) / (minuteCircleSize * 2));
			float remaining = ((((float) hourCircleInterval) % (minuteCircleSize * 2)) / minuteCircleCount);
			
			if (i < 4) {
				for (int m = 0; m < minuteCircleCount; m++) {
					float minuteCx = (m * remaining) + hourCx + (m * (minuteCircleSize * 2 ));
					
					canvas.drawCircle(minuteCx, hourCy, minuteCircleSize / 2, mTimeLinePaint);
				}
			}
		}
		
		
		
	}
	

	
	private void drawDrivingEvents(Canvas canvas) {
		RectF rect;	
		if(mEvents == null || mEvents.size() == 0){
			return;
		}
		
		mRecordingEvent = null;
		for (Event ev : mEvents) {
			Integer id = Integer.valueOf(ev.getRowId());
			rect = mRects.get(id);
			
			if(rect == null || ev.isRecordingEvent() || mSizeChanged){
				rect = calculateEventRect(ev);
				if(ev.isRecordingEvent()) {
					mRecordingEvent = ev;
				}
				mRects.put(id, rect);
			}
			
			mSizeChanged = false;
			
			mEventFillPaint.setColor(mEventColors[ev.getEventType()]);
			
			canvas.drawLine(rect.left, rect.top, rect.right, rect.top, mEventFillPaint);
		}
	}
	
	
	
	private RectF calculateEventRect(Event ev){
		RectF rect = new RectF();
		
		float bottomPadding = mViewHeight - (convertFromPxToDip(BOTTOM_PADDING) + convertFromPxToDip(14));
		
		float outMax = (getLeftPadding() + mViewWidth);
		float outMin = getLeftPadding();
		
		float left = (ev.getStartDateInMillis() - mTodayStartInMillis) * (outMax - outMin) / (mTodayEndInMillis - mTodayStartInMillis) + outMin;
		float right = (ev.getEndDateInMillis() - mTodayStartInMillis) * (outMax - outMin) / (mTodayEndInMillis - mTodayStartInMillis) + outMin;

		if(getLeftPadding() > left) {
			left = getLeftPadding();
		}
		
		rect.set(left + (mEventLineStrokeWidth / 2), bottomPadding, right - (mEventLineStrokeWidth / 2), bottomPadding);
		return rect;
	}



	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		
		if(DEBUG) {
			Log.i(TAG, TAG + "onTouchEvent() x= " + String.valueOf(x));
		}
		
		getParent().requestDisallowInterceptTouchEvent(true);
		((ViewGroup)getParent()).onInterceptTouchEvent(event);
		switch(event.getAction()) {
		 
		case MotionEvent.ACTION_DOWN:
			if(x >= mNowRect.left && x <= mNowRect.right && x >= getLeftPadding() && x <= getViewWidthWithoutPadding() +getRightPadding()) {
				mNowLinePaint.setStrokeWidth(convertFromPxToDip(1));
				mTouching = true;
				mPointerX = x;
				StaticDayView.this.invalidate();
				return true;
			}
			
			break;
		case MotionEvent.ACTION_MOVE:
			if(x <= getLeftPadding()) {
				x = getLeftPadding();
			} else if(x >= getViewWidthWithoutPadding() + getRightPadding()) {
				x = getViewWidthWithoutPadding() + getRightPadding();
			}
			mTouching = true;
			mPointerX = x;
			StaticDayView.this.invalidate();
			
			return true;
		case MotionEvent.ACTION_UP:
			if(mTouching) {
				mNowLinePaint.setStrokeWidth(convertFromPxToDip(2));
				mTouching = false;
				StaticDayView.this.invalidate();
				return true;
			}
			break;
		}
		
		return false;
	}
	

	public static String getTimeLabel(long millis){
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(millis);
		
		return String.format(Locale.getDefault(), "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
	}

	
	public static String convertDatesToMinutes(long start, long end){
		long secs = (end - start);
		long second = (secs / 1000) % 60;
		long minute = (secs / (1000 * 60)) % 60;
		long hour = (secs / (1000 * 60 * 60)) % 24;
		long days = (secs / (1000 * 60 * 60 )) / 24;
		String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
		if(days > 0){
			return days + "d " + time;
		} else {
			return time;
		}
	}



}
