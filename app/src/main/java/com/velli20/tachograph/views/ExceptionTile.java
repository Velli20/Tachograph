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

import com.velli20.tachograph.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class ExceptionTile extends View {
	private TextPaint mExceptionTextPaint;
	private Paint mTilePaint;

	
	private final float mScale;
	private final float mStrokeWidth;

	private String mExceptionNumber = "3";

	public ExceptionTile(Context context) {
		this(context, null, 0);
	}
	
	public ExceptionTile(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public ExceptionTile(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		final Resources res = context.getResources();
		
		mScale = res.getDisplayMetrics().density;
		mStrokeWidth = convertFromPxToDip(1);
	
		mExceptionTextPaint = new TextPaint();
		mExceptionTextPaint.setColor(res.getColor(R.color.black));
		mExceptionTextPaint.setTextSize(convertFromPxToDip(12));
		mExceptionTextPaint.setAntiAlias(true);
		mExceptionTextPaint.setTextAlign(Align.CENTER);
		
		mTilePaint = new Paint();
		mTilePaint.setStyle(Style.STROKE);
		mTilePaint.setStrokeWidth(mStrokeWidth);
		mTilePaint.setColor(res.getColor(R.color.black));
		mTilePaint.setAntiAlias(true);
		
		if(!isInEditMode()){

			Typeface font = RobotoLightTextView.getTypeface(res, 1);
			if(font != null){
				mExceptionTextPaint.setTypeface(font);
			}
		}
		
		setWillNotDraw(false);
		
		
	}
	
	public void setException(String exception){
		mExceptionNumber = exception;
		invalidate();
	}
	
	private float convertFromPxToDip(float value){
		return mScale * value;
	}
	

	public int getTileHeight(){
		return (int)convertFromPxToDip(8);
	}
	
	@Override
	public void onDraw(Canvas canvas){
		final int xPos = (getWidth() / 2);
		final int yPos = (int) ((getHeight() / 2) - ((mExceptionTextPaint.descent() + mExceptionTextPaint.ascent()) / 2)) ; 
		 
		canvas.drawCircle(xPos, (getHeight() / 2), convertFromPxToDip(8), mTilePaint);
		canvas.drawText(mExceptionNumber,  xPos, yPos , mExceptionTextPaint);
	}


}
