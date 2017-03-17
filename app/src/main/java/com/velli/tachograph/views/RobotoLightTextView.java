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

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.LruCache;
import android.widget.TextView;

public class RobotoLightTextView extends TextView {
    private static LruCache<String, Typeface> sTypefaceCache = new LruCache<>(7);
    
    private static String[] mTypefacePaths = {"font/Roboto-Bold.ttf", "font/Roboto-Light.ttf", "font/Roboto-LightItalic.ttf", 
    		"font/Roboto-Medium.ttf", "font/Roboto-Regular.ttf", "font/Roboto-Thin.ttf", "font/Roboto-Italic.ttf", "AndroidClockMono-Thin.ttf"};
    
	public RobotoLightTextView(Context context) {
		this(context, null, 0);
	}
	
	public RobotoLightTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public RobotoLightTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		int typeface = 3;
		
		if(isInEditMode()){
			return;
		}
		if(attrs != null){
			TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RobotoText, 0, 0);
			
			try{
				typeface = a.getInt(R.styleable.RobotoText_style, 4);
			} finally {
				a.recycle();
			}
		}
				

		setTypeface(getTypeface(getResources(), typeface));
		setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
		
	}
	

	public static Typeface getTypeface(Resources res, int typeface){
		Typeface roboto = sTypefaceCache.get(mTypefacePaths[typeface]);
		
		if(roboto == null){
			roboto = Typeface.createFromAsset(res.getAssets(), mTypefacePaths[typeface]);
			sTypefaceCache.put(mTypefacePaths[typeface], roboto);
		}
		
		return roboto;
	}

}
