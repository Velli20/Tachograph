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

import com.velli.tachograph.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class DashpathLine extends View {
	private Paint mLinePaint = new Paint();
	private float mScale;
	private float mDotsize;
	private boolean mSizeChanged = true;
	
	public DashpathLine(Context context) {
		super(context);
		init(context, null);
	}
	
	public DashpathLine(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	
	public DashpathLine(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public DashpathLine(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attrs){
		final Resources res = getResources();
		mScale = res.getDisplayMetrics().density;

		int mLineColor;
		if(attrs != null) {
			TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DashpathLine, 0, 0);
			
			mDotsize = a.getDimensionPixelSize(R.styleable.DashpathLine_dotSize, (int)getDpValue(2));
			mLineColor = a.getColor(R.styleable.DashpathLine_dotSize, Color.BLACK);
		} else {
			mDotsize = getDpValue(2);
			mLineColor = Color.BLACK;
		}
		
		mLinePaint.setColor(mLineColor);
		mLinePaint.setAlpha(31);
		
		
		setWillNotDraw(false);
	}
	
	private float getDpValue(float value){
		return mScale * value;
	}
	
	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		mSizeChanged = true;
	}

	
	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		if(!mSizeChanged){
			return;
		}
		mSizeChanged = false;
		
		int width = getWidth();
		int cy = getHeight() / 2;
		
		float interval = (mDotsize * 2);
		int count = (int)(width / interval);
		float rem = ((width % interval) / count);
		
		for(int i = 0; i < count; i++) {
			canvas.drawCircle(rem + (i * interval), cy, mDotsize / 2, mLinePaint);
		}
	}
}
