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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;

public class ActivityProgressPieChart extends ActivityProgress {
	
	private static final int STROKE_WIDTH = 14;
	private static final int TITLE_SIZE = 16;
	private static final int SUB_TITLE_SIZE = 12;
	private static final int SUB_TITLE_PADDING = 16;

	private float mHeight;
	private float mWidth;
	
	private RectF mArcRect;


    public ActivityProgressPieChart(Context context) {
        super(context, null, -1);
    }

    public ActivityProgressPieChart(Context context, AttributeSet attrs) {
        super(context, attrs, -1);
    }

	public ActivityProgressPieChart(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
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
	public void drawProgress(Canvas canvas, float progress, float max){
		
		float width = mWidth;
		float height = mHeight;

        Paint progressPaint = getChartPaint();
        Paint backgroundPaint = getChartBackgroundPaint();

        TextPaint titlePaint = getTitlePaint();
        TextPaint subTitlePaint = getSubTitlePaint();

        String title = getTitle();
        String subTitle = getSubTitle();
		
		if(width > height){
			height = width;
		} else if(height > width){
			width = height;
		}
		
		float cx = width / 2;
		float cy = height / 2;
		
		float percentage = Math.max(0, progress) / max;
		float arc = percentage * 360f;
		float stroke = getDpValue(STROKE_WIDTH) / 2;
		
		if (arc < 360) {
			// Draw background circle
			canvas.drawCircle(cx, cy, (cx) - stroke, backgroundPaint);
		}

		// Draw progress
		if(arc != 0) {
			canvas.drawArc(mArcRect, 270, arc, false, progressPaint);
		}
		
		// Draw title
		float subTitlePos = cy + getDpValue(SUB_TITLE_SIZE) + (getDpValue(SUB_TITLE_PADDING) / 2);
		
		canvas.drawText(title, cx, cy, titlePaint);
		canvas.drawText(subTitle, cx, subTitlePos, subTitlePaint);

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

