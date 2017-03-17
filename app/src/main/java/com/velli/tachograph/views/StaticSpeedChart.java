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
import java.util.Locale;

import com.velli.tachograph.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class StaticSpeedChart extends View {
	private static final boolean DEBUG = true;
	private static final String TAG = "StaticSpeedChart ";
	private static final int PADDING_LEFT = 1;
	private static final int PADDING_TOP = 16;
	private static final int PADDING_RIGHT = 1;
	private static final int PADDING_BOTTOM = 16;
	
	private static final int TEXT_LABEL_SIZE = 12;
	private static final int LINE_STROKE_WIDTH = 2;
	
	private float mScale;
	private float mPaddingLeft;
	private float mPaddingRight;
	private float mPaddingTop;
	private float mPaddingBottom;
	
	private float mDotSize;
	private float mTextLabelSize;
	
	private Paint mChartRangePaint = new Paint();
	private Paint mBottomLinePaint = new Paint();
	
	private Paint mChartLinePaint = new Paint();
	private TextPaint mLabelPaint = new TextPaint();
	private Paint mLabelBackground = new Paint();
	
	private Line mLine;
	
	private int mWidth;
	private int mHeight;
	private float mMaxSpeed = 90;
	private long mDateStart = 0;
	private long mDateEnd = 7200000;
	
	public StaticSpeedChart(Context context) {
		super(context);
		init();
	}
	
	public StaticSpeedChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public StaticSpeedChart(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public StaticSpeedChart(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}
	
	private void init(){
		mScale = getResources().getDisplayMetrics().density;
		
		mPaddingLeft = getDpValue(PADDING_LEFT);
		mPaddingRight = getDpValue(PADDING_RIGHT);
		mPaddingBottom = getDpValue(PADDING_BOTTOM);
		mPaddingTop = getDpValue(PADDING_TOP);
		
		mTextLabelSize = getDpValue(TEXT_LABEL_SIZE);
		mDotSize = getDpValue(1f);
		
		mChartRangePaint.setColor(Color.BLACK);
		mChartRangePaint.setAntiAlias(true);
		mChartRangePaint.setStyle(Style.FILL);
		mChartRangePaint.setStrokeWidth(mDotSize);
		mChartRangePaint.setAlpha(40);
		mChartRangePaint.setStrokeCap(Cap.ROUND);
		
		mBottomLinePaint.setStyle(Style.STROKE);
		mBottomLinePaint.setAntiAlias(true);
		mBottomLinePaint.setColor(Color.GRAY);
		mBottomLinePaint.setStrokeWidth(mDotSize);
		
		mChartLinePaint.setColor(getResources().getColor(R.color.color_primary_400));
		mChartLinePaint.setAntiAlias(true);
		mChartLinePaint.setStyle(Style.STROKE);
		mChartLinePaint.setStrokeWidth(getDpValue(1.5f));
		mChartLinePaint.setStrokeCap(Cap.ROUND);
		
		mLabelPaint.setColor(Color.BLACK);
		mLabelPaint.setAntiAlias(true);
		mLabelPaint.setAlpha(120);
		mLabelPaint.setTextSize(mTextLabelSize);
		if(!isInEditMode()) {
			mLabelPaint.setTypeface(RobotoLightTextView.getTypeface(getResources(), 4));
		}
		
		mLabelBackground.setAntiAlias(true);
		mLabelBackground.setStyle(Style.FILL);
		mLabelBackground.setColor(Color.WHITE);
		
		if(isInEditMode()) {
			mLine = new Line();
			
			mLine.addPoint(new LinePoint(0, 10));
			mLine.addPoint(new LinePoint(mDateEnd / 4, 30));
			mLine.addPoint(new LinePoint(mDateEnd / 2, 60));
			mLine.addPoint(new LinePoint(mDateEnd / 3, 70));
			mLine.addPoint(new LinePoint(mDateEnd, 40));
		}
		
		setWillNotDraw(false);
	}
	
	private float getDpValue(float value) {
		return mScale * value;
	}
	
	private float getLeftPadding() {
		return mPaddingLeft;
	}
	
	private float getRightPadding() {
		return mPaddingRight;
	}
	
	private float getTopPadding() {
		return mPaddingTop;
	}
	
	private float getBottomPadding() {
		return mPaddingBottom;
	}
	
	private float getTextPadding(){
		return mTextLabelSize * 1.5f;
	}
	
	public void setLine(Line line) {
		if(DEBUG) {
			Log.i(TAG, TAG + "setLine() point count: " + line.getPoints().size());
		}
		
		
		if(line != null) {
			mLine = line;
			ArrayList<LinePoint> points = mLine.getPoints();
			
			if (points != null && points.size() >= 2) {
				mDateStart = points.get(0).getX();
				mDateEnd = points.get(points.size() -1).getX();
			}
		}
		invalidate();
	}
	
	public void setMax(float maxSpeed) {
		mMaxSpeed = maxSpeed;
		invalidate();
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mHeight = getHeight();
		mWidth = getWidth();
		
		drawRange(canvas);
	}
	
	
	private void drawRange(Canvas canvas) {
		float leftPadding = getLeftPadding();
		float textPadding = getTextPadding();
		float bottomPadding = getBottomPadding();
		float topPadding = getTopPadding();
		
		float cy = mHeight - (textPadding + bottomPadding);
		float cx = leftPadding;
		float cxEnd = mWidth - (getRightPadding());
		float dotRadius = mDotSize / 2;
		
		float horizontalLineWidth = cxEnd - cx;
		float verticalLineHeight = mHeight - (textPadding + bottomPadding);
		
		
			
		//Draw bottom line
		canvas.drawLine(cx - dotRadius, cy, cxEnd + dotRadius, cy, mBottomLinePaint);
		
		//Draw speed lines
		float labelInterval = (mTextLabelSize * 2);
		int labelCount = (int) (verticalLineHeight / labelInterval);
		float labelExtraPad = (verticalLineHeight % labelInterval) / (labelCount -2) ;
		float speedLabelPad = getDpValue(3);
		
		float lineCy = topPadding;
		for(int i = 0; i < labelCount; i++) {

			if(i != 0 && i != labelCount -1) {
				//drawHorizontalDashPathLine(canvas, mDotSize, cx, cxEnd, lineCy, true);
				canvas.drawLine(cx, lineCy, cxEnd, lineCy, mChartRangePaint);
			} 
			lineCy += (labelInterval + labelExtraPad);
		}
		
		// Draw line, above speed label line but below speed labels
		if(mLine != null) {
			RectF rect = new RectF(cx - dotRadius, topPadding, cxEnd + dotRadius, cy);
			drawLine(canvas, mLine, rect);
		}
		
		//Draw speed labels
		
		lineCy = topPadding;
		float labelCy;
		for(int i = 0; i < labelCount; i++) {
			if(i == labelCount -1){
				break;
			} else if(i == 0) {
				labelCy = lineCy + (mTextLabelSize / 2);
			} else {
				labelCy = lineCy - speedLabelPad;
			}
			float speed = mMaxSpeed - (((float)i / (float)labelCount) * mMaxSpeed);
			
			String label = (i == 0 ? "km/h" : String.valueOf((int)speed));
			

			canvas.drawText(label, cx + speedLabelPad, labelCy, mLabelPaint);
			
			lineCy += (labelInterval + labelExtraPad);
		}
		
		// Draw time labels
		
		
		float horizontalLabelInterval = ((mLabelPaint.measureText("00.00") * 2)) ;
		
		int requiredLabelCount = (int) (((mDateEnd - mDateStart) / (1000 * 60)) % 60);
		int horizontalLabelCount = (int)((horizontalLineWidth - mLabelPaint.measureText("00.00")) / horizontalLabelInterval);
		
		float horizontalLabelExtraPad = (((horizontalLineWidth - mLabelPaint.measureText("00.00"))% (horizontalLabelInterval)) / (horizontalLabelCount));
		
		float previousLabelCx = cx;
		
		// Avoid drawing same label
		if(requiredLabelCount < horizontalLabelCount) {
			horizontalLabelCount = requiredLabelCount;
			horizontalLabelInterval = ((horizontalLineWidth - mLabelPaint.measureText("00.00")) / requiredLabelCount);
			horizontalLabelExtraPad = ((horizontalLineWidth - mLabelPaint.measureText("00.00")) % requiredLabelCount) / requiredLabelCount;
		}

		
		for(int i = 0; i <= horizontalLabelCount; i++) {
			final long time = (long)(((float)i / (float)labelCount) * (mDateEnd - mDateStart));
			String timeLabel = getTimeLabel(mDateStart + time);
			
			float labelCx;
			if(i == 0){
				labelCx = previousLabelCx;
			} else {
				labelCx = previousLabelCx + horizontalLabelInterval + horizontalLabelExtraPad;
			}
			 
			previousLabelCx = labelCx;
			
			canvas.drawText(timeLabel, labelCx, mHeight - getBottomPadding(), mLabelPaint);
			
		}
		
		//Draw vertical speed line
				canvas.drawLine(cx, topPadding, cx, verticalLineHeight, mBottomLinePaint);
				
				//Draw vertical end line
				canvas.drawLine(cxEnd, topPadding, cxEnd, verticalLineHeight, mBottomLinePaint);
			
		
	
	}
	
	private void drawLine(Canvas canvas, Line line, RectF rect) {
		if(DEBUG) {
			Log.i(TAG, TAG + "drawLine()");
		}
		Path path = new Path();
		
		if(line != null && line.getPoints() != null && !line.getPoints().isEmpty()) {
			
			boolean moveToFirst = false;
			for(LinePoint point : line.getPoints()) {
				float xPercent = (((point.getX() - mDateStart) * 1.0f) / ((mDateEnd - mDateStart) * 1.0f));
				
				float cy = point.getY() * (rect.top - rect.bottom) / mMaxSpeed + rect.bottom;
				float cx = (xPercent * (rect.right - rect.left)) + rect.left;
				
				if(!moveToFirst) {
					path.moveTo(cx, cy);
					moveToFirst = true;
				}
				
				path.lineTo(cx, cy);
				path.moveTo(cx, cy);
			}

			
			canvas.drawPath(path, mChartLinePaint);
			

		}
	}

	private void drawHorizontalDashPathLine(Canvas canvas, float dotSize, float startX, float endX, float cy, boolean skipFirstAndLast) {
		float width = endX - startX;
		
		float interval = (dotSize * 3);
		int dotCount = (int)(width / interval);
		
		
		float extraPadding = (width % (interval)) / dotCount;
		float cx;
		
		if(skipFirstAndLast) {
			cx = startX + (interval + extraPadding);
		} else {
			cx = startX;
		}
		for(int i = 1; i <= dotCount; i++) {
				
			if(skipFirstAndLast && i == dotCount){
				break;
			}

			canvas.drawPoint(cx, cy, mChartRangePaint);
			cx += (interval + extraPadding);
		}
	}
	
	private void drawVerticalDashPathLine(Canvas canvas, float dotSize, float startY, float endY, float cx) {
		float height = endY - startY;
		
		float interval = (dotSize * 3);
		int dotCount = (int)(height / interval);
		
		float extraPadding = (height % interval) / dotCount;
		float cy = startY;
		
		for(int i = 0; i <= dotCount; i++) {
					
			canvas.drawPoint(cx, cy, mChartRangePaint);
			cy += interval + extraPadding;
		}
	}
	
	public static String getTimeLabel(long millis){
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(millis);
		
		return String.format(Locale.getDefault(), "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
	}

}
