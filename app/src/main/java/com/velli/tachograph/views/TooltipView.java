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
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TooltipView extends LinearLayout {
	private ImageView mArrowUp;
    private ImageView mArrowDown;
    private ImageView mSelectedArrow;
    
    private TextView mTitle;
    private TextView mSubtitle;
    
    private LinearLayout mBackground;
    
    private int mArrowWidth;
    
    private int mAnchorX;
    private int mAnchorY;
    private int mAnchorWidth;
    
	public TooltipView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.setOrientation(VERTICAL);
    }
	
	public enum ArrowType {
		ArrowUp, ArrowDown
	}
	
	protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i("", "Tooltip onFinishInflate()");
        mArrowUp = (ImageView)this.findViewById(R.id.tooltip_arrow_top);
        mArrowDown = (ImageView)this.findViewById(R.id.tooltip_arrow_bottom);
        
        mBackground = (LinearLayout) findViewById(R.id.tooltip_background);
        
        mArrowWidth = mArrowDown.getDrawable().getIntrinsicWidth() / 2;
        
        mTitle = (TextView)this.findViewById(R.id.tooltip_title);
        mSubtitle = (TextView)this.findViewById(R.id.tooltip_text);
        
        setArrowType(ArrowType.ArrowDown);
	}

	
	public void setArrowType(ArrowType al) {
		Log.i("", "Tooltip setArrowType()");
		
        switch (al) {
            case ArrowUp: {
            	this.mArrowUp.setVisibility(View.VISIBLE);
                this.mArrowDown.setVisibility(View.GONE);
                this.mSelectedArrow = mArrowUp;
                break;
            }
            case ArrowDown: {
                this.mArrowDown.setVisibility(View.VISIBLE);
                this.mArrowUp.setVisibility(View.GONE);
                this.mSelectedArrow = mArrowDown;
                break;
            }
        }
        
        setParams();
    }
	
	private void setParams() {
		Log.i("", "Tooltip setParams()");
        if (mArrowWidth > 0) {
            ViewGroup.MarginLayoutParams arrowParams = (ViewGroup.MarginLayoutParams)mSelectedArrow.getLayoutParams();
            arrowParams.rightMargin = (mAnchorX - mAnchorWidth) + (mArrowWidth * 2);
            arrowParams.bottomMargin = mAnchorY;
            
            mSelectedArrow.requestLayout();
            
            ViewGroup.MarginLayoutParams mBackgroundParams = (ViewGroup.MarginLayoutParams)mBackground.getLayoutParams();
            mBackgroundParams.rightMargin = mAnchorX - mAnchorWidth;
            mBackground.requestLayout();
            
        }
    }
	
	public void setAnchorPos(int x, int y, int anchorWidth){
		mAnchorX = x;
		mAnchorY = y;
		mAnchorWidth = anchorWidth;
		setParams();
	}
	
	

	public void setTitle(String title){
		mTitle.setText(title);
	}
	
	public void setSubtitle(String subtitle){
		mSubtitle.setText(subtitle);
	}

}
