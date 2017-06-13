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
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;


public class RobotoButton extends AppCompatButton {

	public RobotoButton(Context context) {
		super(context);
		init(context, null);
	}
	
	public RobotoButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}
	
	public RobotoButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}
	
	private void init(Context context, AttributeSet attrs) {
		int typeface = 3;

		if (isInEditMode()) {
			return;
		}
		if (attrs != null) {
			TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RobotoText, 0, 0);
			try {
				typeface = a.getInt(com.velli20.tachograph.R.styleable.RobotoText_style, 3);
			} finally {
				a.recycle();
			}
		}

		Typeface roboto = RobotoLightTextView.getTypeface(getResources(), typeface);
		setTypeface(roboto);
	}

}

