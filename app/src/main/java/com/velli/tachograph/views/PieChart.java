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

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class PieChart extends View {
	
	/** DP sizes */
	private static final int STROKE_WIDTH = 14;
	private static final int TITLE_SIZE = 16;
	private static final int SUB_TITLE_SIZE = 12;
	private static final int SUB_TITLE_PADDING = 16;
	private static final int ANIM_DURATION = 700;
	
	private final Paint mBackGroundPaint = new Paint();
	private final Paint mChartPaint = new Paint();
	
	private final TextPaint mTitlePaint = new TextPaint();
	private final TextPaint mSubTitlePaint = new TextPaint();

	private float scale;
	private float mMax = 1200;
	private float mHeight;
	private float mWidth;
	private float mCurrentPrecentage = 0;
	private int mProgress = 0;
	
	private String mTitle = "00:21:37";
	private String mSubtitle = "";
	
	private RectF mArcRect;

	public PieChart(Context context) {
		super(context);
		init();
	}
	
	public PieChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public PieChart(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public PieChart(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}
	
	private void init(){
		final Resources res = getResources();
		scale = res.getDisplayMetrics().density;
		
		mBackGroundPaint.setColor(Color.BLACK);
		mBackGroundPaint.setAntiAlias(true);
		mBackGroundPaint.setStyle(Paint.Style.STROKE);
		mBackGroundPaint.setStrokeWidth(getDpValue(STROKE_WIDTH));
		mBackGroundPaint.setStrokeCap(Cap.ROUND);
		mBackGroundPaint.setAlpha(31); // ~12 %
		
		mChartPaint.setColor(Color.parseColor("#03a9f4"));
		mChartPaint.setAntiAlias(true);
		mChartPaint.setStyle(Paint.Style.STROKE);
		mChartPaint.setStrokeWidth(getDpValue(STROKE_WIDTH));
		mChartPaint.setStrokeCap(Cap.ROUND);
		
		if(!isInEditMode()){
			final Typeface robotoLight = RobotoLightTextView.getTypeface(getResources(), 1);
		    mTitlePaint.setTypeface(RobotoLightTextView.getTypeface(getResources(), 4));
		    mSubTitlePaint.setTypeface(robotoLight);
		}
		mTitlePaint.setColor(Color.BLACK);
		mTitlePaint.setAntiAlias(true);
		mTitlePaint.setTextSize(getDpValue(TITLE_SIZE));
		mTitlePaint.setTextAlign(Align.CENTER);
		mTitlePaint.setAlpha(222); // ~87 %
		
		
		mSubTitlePaint.setColor(Color.BLACK);
		mSubTitlePaint.setAntiAlias(true);
		mSubTitlePaint.setTextSize(getDpValue(SUB_TITLE_SIZE));
		mSubTitlePaint.setTextAlign(Align.CENTER);
		mSubTitlePaint.setAlpha(138); // ~54 %
		
		
		setWillNotDraw(false);
	}
	
	public void setProgress(int progress){
		this.mProgress = progress;
		if(progress == -1){
			mTitlePaint.setAlpha(97); // ~38 %
			mSubTitlePaint.setAlpha(97); // ~54 %
			mChartPaint.setAlpha(97); // ~38%
			mBackGroundPaint.setAlpha(12);
		} else {
			mTitlePaint.setAlpha(222); // ~87 %
			mSubTitlePaint.setAlpha(138); // ~54 %
			mChartPaint.setAlpha(255); 
			mBackGroundPaint.setAlpha(31);
		}
		invalidate();
	}
	
	public void setChartColor(int color){
		mChartPaint.setColor(color);
	}
	
	public void setTitle(String title){
		mTitle = title;
	}
	
	public void setSubtitle(String subtitle){
		mSubtitle = subtitle;
	}
	
	public void setMax(int max){
		mMax = max;
	}
	
	public int getProgress(){
		return mProgress;
	}
	
	public void animProgress(int progress){
		this.mProgress = progress;
		int startProgress = (int)(mCurrentPrecentage * mMax);
		final PropertyValuesHolder progAnimHolder = PropertyValuesHolder.ofInt("progress", startProgress, progress);
   	    final ObjectAnimator progAnim = ObjectAnimator.ofPropertyValuesHolder(this, progAnimHolder);  
   	   
   	    progAnim.setInterpolator(new AccelerateDecelerateInterpolator());
   	    progAnim.setDuration(ANIM_DURATION);
   	    progAnim.start();
	}
	
	private float getDpValue(float value){
		return scale * value;
	}
	
	private RectF getArcRect(){
		
		final RectF rect = new RectF();
		final float stroke = getDpValue(STROKE_WIDTH) / 2;
		
		rect.set(stroke, stroke, mWidth - stroke, mHeight - stroke);
		return rect;
	}
	
	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = getWidth();
		mHeight = getHeight();
		mArcRect = getArcRect();
	}
	
	@Override
	public void onDraw(Canvas canvas){
		
		float width = mWidth;
		float height = mHeight;
		
		
		if(width > height){
			height = width;
		} else if(height > width){
			width = height;
		}
		
		float cx = width / 2;
		float cy = height / 2;
		
		float precentage = (mProgress >= 0 ? mProgress : 0) / mMax;
		float prog = precentage * 360f;
		float stroke = getDpValue(STROKE_WIDTH) / 2;
		
		if (prog < 360) {
			// Draw background circle
			canvas.drawCircle(cx, cy, (cx) - stroke, mBackGroundPaint);
		}
		// Draw progress
		mCurrentPrecentage = prog;
		
		if(prog != 0) {
			canvas.drawArc(mArcRect, 270, prog, false, mChartPaint);
		}
		
		// Draw title
		float subTitlePos = cy + getDpValue(SUB_TITLE_SIZE) + (getDpValue(SUB_TITLE_PADDING) / 2);
		
		canvas.drawText(mTitle, cx, cy, mTitlePaint);
		canvas.drawText(mSubtitle, cx, subTitlePos, mSubTitlePaint);

	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		int finalWidth;
		int finalHeight;
		
		if(widthSize > heightSize){
			finalWidth = widthSize;
			finalHeight = widthSize;
		} else if(heightSize > widthSize){
			finalWidth = heightSize;
			finalHeight = heightSize;
		} else {
			finalWidth = widthSize;
			finalHeight = heightSize;
		}
		 
	    setMeasuredDimension(finalWidth, finalHeight);
	}

}

