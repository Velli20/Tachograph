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
















import com.velli.tachograph.R;
import com.velli.tachograph.DateCalculations;
import com.velli.tachograph.Event;
import com.velli.tachograph.views.LockableScrollview.OnScrollListener;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;

public class DayView extends ViewGroup implements OnScrollListener {
    private Paint mTimeLinePaint = new Paint();
    private Paint mEventFillPaint = new Paint();
    private Paint mEventTimeLinePaint = new Paint();
    
    private PointF mScrollStartPoint = new PointF();
    
    private TextPaint mTimeLabelPaint = new TextPaint();
    private TextPaint mEventLabelPaint = new TextPaint();
    
    private OnEventTouchListener mListener;
    private Handler mHandler;
    private Vibrator mVibrator;
    private LockableScrollview mScrollView;
    
    private static final int DIP_PADDING = 15;
    private static final int EVENT_ALPHA = 60;
    private static final int EVENT_HEIGHT = 18;
    private static final int EVENT_LABEL_PADDING = 5;
    private static final int HOUR_LINE_HEIGHT = 30;
    private static final int MINUTE_LINE_HEIGHT = 10;
    
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private int mScreenWidth = 0;
    private int mSeparation = 15; 
    private int mEventColors[];
    private int mSwipeSlope;
    private int mSmoothScrollPosition = 0;
    
    private float scale = 0;
    private float mEventRectCornerRadius;
    private long mTodayStartInMillis = 0;
    private long mLastVibrate;
    
    private boolean mIsAnimating = false;
    private boolean mSwiping = false;
    private boolean mBottomIsReached = false;
    private boolean mWaitingForSmoothSrcollToPosition = false;
    
    private ArrayList<Event> mEvents;
    
    private String mEventsNames[] = {};
    

    public interface OnEventTouchListener {
    	void onEventTouch(Event event, boolean isTouching, ViewGroup parent);
    }

	public DayView(Context context) {
		this(context, null, 0);
	}
	
	public DayView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public DayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Resources res = getResources();
		
		scale = res.getDisplayMetrics().density;
		mTodayStartInMillis = DateCalculations.getTodayStartTime();
		mSwipeSlope = ViewConfiguration.get(context).getScaledTouchSlop();
		mEventRectCornerRadius = convertFromPxToDip(2f);
		
		mTimeLinePaint.setColor(res.getColor(R.color.gray));
		mTimeLinePaint.setStrokeWidth(convertFromPxToDip(1f));
		mTimeLinePaint.setStyle(Style.STROKE);
		mTimeLinePaint.setStrokeCap(Cap.ROUND);
		
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
		
		mEventFillPaint.setStyle(Style.FILL_AND_STROKE);
		mEventFillPaint.setAlpha(EVENT_ALPHA);
		mEventFillPaint.setAntiAlias(true);
		
		mEventsNames = context.getResources().getStringArray(R.array.event_explanations);
		mEventColors = context.getResources().getIntArray(R.array.event_colors);
		
		mHandler = new Handler();
		mHandler.postDelayed(mEventUpdater, 1000);
		
		if(!isInEditMode()){
			mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		}
		
		
		getScreenSize(context);
		setWillNotDraw(false);
	}
	

	private void getScreenSize(Context context){
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		
		Point size = new Point();
		display.getSize(size);
		mScreenWidth = size.x / 2;
	}
	
	public int addNewEvent(Event event){
		if(mEvents == null){
			mEvents = new ArrayList<>();
		}
		mEvents.add(event);
		invalidate();
		return (int) (convertFromPxToDip((getStartInDp(event)) + getStringPositionInDp(event, scale)));
	}
	
	public void addAllEvents(ArrayList<Event> list){
		if(list != null){
			mEvents = list;
			Log.i("", "DayView list size " + list.size());
		    invalidate();
		} else {
			Log.i("", "DayView list null");
		}
	}

	
	public void setOnEventTouchListener(OnEventTouchListener listener){
		mListener = listener;
	}
	
	private float convertFromPxToDip(float value){
		return scale * value;
	}
	
	public float getPositionAt(int minutes){
		return convertFromPxToDip(DIP_PADDING + minutes) - mScreenWidth;
	}
	
	public float getCurrentPosition(boolean centerPosisition){
		return (((mScrollView.getScrollX() + (centerPosisition ? mScreenWidth : 0)) / scale) - DIP_PADDING);
	}
	
	private float getStartInDp(Event ev){
		long s = (ev.getStartDateInMillis() - mTodayStartInMillis) / 1000;
		long mins = (s / 60);

		return mins;
	}
	
	private float getEndInDp(Event ev){
		long secs = (ev.getEndDateInMillis() - ev.getStartDateInMillis()) / 1000;
		long mins = (secs / 60);
		return getStartInDp(ev) + mins;
	}
	
	@SuppressWarnings("unused")
	private float getStringPosition(Event ev){
		final float start = getStartInDp(ev);
		final float end = getEndInDp(ev);
		
		if(end > 1440){
			return (1440 - start) / 2;
		} else {
			return (end - (start < 0 ? 0 : start)) / 2;
		}
	}
	
	private float getStringPositionInDp(Event ev, float dip){
		final float start = getStartInDp(ev);
		final float end = getEndInDp(ev);
		
		if(end > 1440){
			return (dip * (1440 - start)) / 2;
		} else {
			return (dip * (end - (start < 0? 0 : start))) / 2;
		}
	}
	
	public void smoothScrollToPosition(int positionInMins){
		final int position = (int)getPositionAt(positionInMins);
		
		if(mScrollView != null){
			mWaitingForSmoothSrcollToPosition = false;
			mScrollView.post(new Runnable() {
			    @Override
			    public void run() {
			    	mScrollView.smoothScrollTo((int) position, 0);
			    } 
			});
		} else {
			mSmoothScrollPosition = positionInMins;
			mWaitingForSmoothSrcollToPosition = true;
		}
	}
	
	public void removeAllEvents(){
		mEvents.clear();
	}
	
	private Runnable mEventUpdater = new Runnable() {
	    @Override
	    public void run(){
	    	invalidate();
			if(mHandler != null){
				mHandler.postDelayed(mEventUpdater, 10000);
			}
	    }
	};

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		mViewWidth = (int) convertFromPxToDip(24 * 4 * mSeparation + (DIP_PADDING * 2));
		mViewHeight = (int)convertFromPxToDip(100);

		this.setMeasuredDimension((int) mViewWidth, mViewHeight);
	}
	

	
	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);
		drawTimeLine(canvas);
		drawDrivingEvents(canvas);
	}
	
	private void drawTimeLine(Canvas canvas){
		float mScreenYmiddlePoint = getHeight() / 2;
		float hourX = 0;
		float timeLabelYpos = mScreenYmiddlePoint + convertFromPxToDip((HOUR_LINE_HEIGHT / 2) ) + mTimeLabelPaint.getTextSize();
		String text;
		
		//Draw times
	    for(int i = 0; i <= 24; i++){
	    	hourX = convertFromPxToDip(DIP_PADDING + 4 * i * mSeparation);
	        text =  i < 10 ? "0" + String.valueOf(i) + ".00" : i +".00";
	        
	        
	    	canvas.drawLine(hourX, mScreenYmiddlePoint - convertFromPxToDip(HOUR_LINE_HEIGHT /2 ), hourX, mScreenYmiddlePoint + convertFromPxToDip(HOUR_LINE_HEIGHT / 2), mTimeLinePaint);	    	
	    	canvas.drawText(text, hourX, timeLabelYpos, mTimeLabelPaint);
	    	
	    	for(int m = 0; m <= 3 && i < 24; m++){
	    		float minuteX = hourX + convertFromPxToDip((m * mSeparation));
	    		canvas.drawLine(minuteX, mScreenYmiddlePoint - convertFromPxToDip(MINUTE_LINE_HEIGHT), minuteX, mScreenYmiddlePoint, mTimeLinePaint);
	    	}
	    }
	    
		//Draw horizontal line
		canvas.drawLine(convertFromPxToDip(DIP_PADDING), mScreenYmiddlePoint, (mViewWidth - convertFromPxToDip(DIP_PADDING)), mScreenYmiddlePoint, mTimeLinePaint);
		
	}
	
	
	private void drawDrivingEvents(Canvas canvas) {
		RectF rect;	
		if(mEvents == null || mEvents.size() == 0){
			return;
		}
		
		for (Event ev : mEvents) {
			if(ev.getRect() == null || ev.isRecordingEvent() && !mIsAnimating){
				ev.setRect(calculateEventRect(ev));
			}
			rect = ev.getRect();
			
			mEventFillPaint.setColor(mEventColors[ev.getEventType()]);
			mEventFillPaint.setAlpha(EVENT_ALPHA);
			mEventTimeLinePaint.setColor(mEventColors[ev.getEventType()]);
			
			canvas.drawRoundRect(rect, mEventRectCornerRadius, mEventRectCornerRadius, mEventFillPaint);
			//canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, mEventTimeLinePaint);
			drawEventLabel(ev, canvas);
			
			//Draw colored line on top of the time line
			float separationInDP = convertFromPxToDip(mSeparation);
			float x = (rect.left - convertFromPxToDip(DIP_PADDING)) % separationInDP;
			float start = 0;
			
			if(x == 0){
				start = rect.left;
			} else {
				start = (separationInDP - x) + rect.left;
			}
			
			if (rect.width() >= separationInDP) {
				for (float i = start; i <= rect.right; i += separationInDP) {
					boolean isHourLine = (i - convertFromPxToDip(DIP_PADDING)) % 60 == 0;

					if (isHourLine) {
						canvas.drawLine(i, rect.bottom - convertFromPxToDip(HOUR_LINE_HEIGHT / 2),
								i,
								rect.bottom
										+ convertFromPxToDip(HOUR_LINE_HEIGHT / 2),
								mEventTimeLinePaint);
					} else {
						canvas.drawLine(i, rect.bottom
								- convertFromPxToDip(MINUTE_LINE_HEIGHT), i,
								rect.bottom, mEventTimeLinePaint);
					}
				}
			}
			
			canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, mEventTimeLinePaint);
		}
	}
	
	private void drawEventLabel(Event ev, Canvas canvas){
		RectF rect = ev.getRect();
		float mScreenYmiddlePoint = getHeight() / 2;
		float center = rect.width() / 2;
		float stringLenght = mEventLabelPaint.measureText(mEventsNames[ev.getEventType()]);
		
		
		if(rect.width() >= stringLenght){
			canvas.drawText(mEventsNames[ev.getEventType()], rect.left + center, 
					mScreenYmiddlePoint - convertFromPxToDip(EVENT_HEIGHT + EVENT_LABEL_PADDING), mEventLabelPaint);
		} else if (rect.width() >= mEventLabelPaint.getTextSize()) {
			canvas.drawText(mEventsNames[ev.getEventType()].substring(0, 1), rect.left + center,
					mScreenYmiddlePoint - convertFromPxToDip(EVENT_HEIGHT + EVENT_LABEL_PADDING), mEventLabelPaint);
		} 
	}
	
	private RectF calculateEventRect(Event ev){
		RectF rect = new RectF();
		
		float mScreenYmiddlePoint = getHeight() / 2;
		float endDp = getEndInDp(ev);
		float startDp = getStartInDp(ev);
		
		if (convertFromPxToDip(startDp) < convertFromPxToDip(DIP_PADDING)) {
			startDp = 0;
		} 
		if (endDp > 1440) {
			endDp = 1440;
		}
		
		rect.set(convertFromPxToDip(startDp + DIP_PADDING), mScreenYmiddlePoint - convertFromPxToDip(EVENT_HEIGHT),
				convertFromPxToDip(endDp + DIP_PADDING + 1), mScreenYmiddlePoint);
		return rect;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mScrollStartPoint.x = event.getX();
			mScrollStartPoint.y = event.getY();
			return true;
		case MotionEvent.ACTION_UP:
			return isTouchingRect(event);
		case MotionEvent.ACTION_MOVE:			
			if (mBottomIsReached) {

				mScrollView.setScrollingEnabled(false);
				float deltaX = event.getX() + getTranslationX();

				if (!mSwiping) {
					if (Math.abs(deltaX - mScrollStartPoint.x) > mSwipeSlope) {
						mSwiping = true;
					}
				} else {
					setTranslationX(deltaX - mScrollStartPoint.x);
				}
			}
			return true;
		}
		return false;
	}
	
	private boolean isTouchingRect(MotionEvent event){
		boolean isTouching = false;
		for(int i = 0; i < mEvents.size(); i++){
			Event ev = mEvents.get(i);
			
			if(ev.getRect().contains(event.getX(), event.getY())){
				isTouching = true;
				if(mListener != null){
					mListener.onEventTouch(ev, true, this);
					animateEvent(ev);
					tryVibrate();
				}
				
			} 
			
		}

		return isTouching;
	}
	
	

	

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mScrollView = (LockableScrollview)getParent();
		mScrollView.setOnScrollListener(this);
		if(mWaitingForSmoothSrcollToPosition){
			smoothScrollToPosition(mSmoothScrollPosition);
		}
		// TODO Auto-generated method stub
		  final int count = getChildCount();
		  int curWidth, curHeight, curLeft, curTop, maxHeight;

		  //get the available size of child view    
		  int childLeft = this.getPaddingLeft();
		  int childTop = this.getPaddingTop();
		  int childRight = this.getMeasuredWidth() - this.getPaddingRight();
		  int childBottom = this.getMeasuredHeight() - this.getPaddingBottom();
		  int childWidth = childRight - childLeft;
		  int childHeight = childBottom - childTop;

		  maxHeight = 0;
		  curLeft = childLeft;
		  curTop = childTop;
		  //walk through each child, and arrange it from left to right
		  for (int i = 0; i < count; i++) {
		    View child = getChildAt(i);
		    if (child.getVisibility() != GONE) {
		      //Get the maximum size of the child
		      child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.AT_MOST), 
		                    MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST));
		      curWidth = child.getMeasuredWidth();
		      curHeight = child.getMeasuredHeight();
		      //wrap is reach to the end
		      if (curLeft + curWidth >= childRight) {
		        curLeft = childLeft;
		        curTop += maxHeight;
		        maxHeight = 0;
		      }
		      //do the layout
		      child.layout(curLeft, curTop, curLeft + curWidth, curTop + curHeight);
		      //store the max height
		      if (maxHeight < curHeight)
		        maxHeight = curHeight;
		      curLeft += curWidth;
		    }
		  }
		
	}
	
	public void animateProgress(Event ev){
		if (mIsAnimating)
			return;

		AnimateEventRect anim = new AnimateEventRect(ev);
		ObjectAnimator pulseAnim = AnimationUtils.getEventProgressAnimator(anim, ev.getRect().left, ev.getRect().right);
		pulseAnim.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				mIsAnimating = true;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mIsAnimating = false;

			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mIsAnimating = false;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

		});
		pulseAnim.start();
	}
	
	public void animateEvent(Event ev){
		if(mIsAnimating) return;
		
		AnimateEventRect anim = new AnimateEventRect(ev);
		ObjectAnimator pulseAnim = AnimationUtils.getPulseAnimator(anim, 0.85f, 1.3f);
		pulseAnim.addListener(new AnimatorListener(){

			@Override
			public void onAnimationStart(Animator animation) {
				mIsAnimating = true;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mIsAnimating = false;
				
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mIsAnimating = false;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}
			
		});
		pulseAnim.start();
	}
	
	public class AnimateEventRect {
		private Event event;
		private RectF originalRect;
		private float progress = 0f;
		private float scaleY = 1;
		
		public AnimateEventRect(Event ev){
			event = ev;
			originalRect = new RectF(ev.getRect());
		}
		
		public void setProgress(float evProgress){
			event.getRect().set(originalRect.left, originalRect.top, progress, originalRect.bottom);
			progress = evProgress;
			DayView.this.invalidate();
		}
		
		public void setScaleY(float scale){
			event.getRect().set(originalRect.left, originalRect.top * scale, originalRect.right, originalRect.bottom);
			scaleY = scale;
			DayView.this.invalidate();
		}
		
		public float getProgress(){
			return progress;
		}
		
		public float getScaleY(){
			return scaleY;
		}
	}
	
	
    public void tryVibrate() {
        if (mVibrator != null) {
            long now = SystemClock.uptimeMillis();
            // We want to try to vibrate each individual tick discretely.
            if (now - mLastVibrate >= 125) {
                mVibrator.vibrate(5);
                mLastVibrate = now;
            }
        }
    }

	@Override
	public void onScrollListener(float x, float y, boolean bottomReached) {
//		if(bottomReached){
//			mBottomIsReached = true;
//		}
	}

	@Override
	public void onScrollingStarted(float x, float y) {
		
	}

	@Override
	public void onScrollingStopped(float x, float y, boolean bottomReached) {
		
		
	}

}
