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
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;

public class RobotoSwitchCompat extends SwitchCompat{
	private OnCheckedChangeListener mListener;
	
	public RobotoSwitchCompat(Context context) {
		super(context);
		init(context, null);
	}
	
	public RobotoSwitchCompat(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	
	public RobotoSwitchCompat(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
        init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attrs){
		int typeface = 3;
		
		if(isInEditMode()){
			return;
		}
		
		if(attrs != null){
			TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RobotoText, 0, 0);
			
			try{
				typeface = a.getInt(R.styleable.RobotoText_style, 3);
			} finally {
				a.recycle();
			}
		}

		setTypeface(RobotoLightTextView.getTypeface(getResources(), typeface));
		setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
	}
	
	@Override
	public void setOnCheckedChangeListener(OnCheckedChangeListener l){
		super.setOnCheckedChangeListener(l);
		mListener = l;
	}
	
	public void setChecked(final boolean checked, final boolean alsoNotify) {
		if (!alsoNotify) {
			super.setOnCheckedChangeListener(null);
			super.setChecked(checked);
			super.setOnCheckedChangeListener(mListener);
			return;
		}
		super.setChecked(checked);
	}

}
