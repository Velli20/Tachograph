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

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class ListCircle extends View {
    private final Paint mBackgroundPaint = new Paint();
    private final TextPaint mLetterPaint = new TextPaint();

    private float scale = 0;
    private int mBackgroundColor;
    private int mSelectedColor;

    private String mText = "";
    private boolean mIsSelected = false;
    private boolean mAnimated = true;
    private boolean mIsSelectable = true;
    private Drawable mDrawableSelected;
    private Drawable mDrawableNormal;
    private OnCircleSelectedListener mListener;


    public interface OnCircleSelectedListener {
        void onCircleSelected(ListCircle v, boolean selected);
    }

    public ListCircle(Context context) {
        this(context, null, 0);
    }

    public ListCircle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListCircle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final Resources res = getResources();

        int textColor = Color.WHITE;
        int textSize = 0;

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ListCircle, 0, 0);
            try {
                String color = a.getString(R.styleable.ListCircle_background_color);
                String colorSelected = a.getString(R.styleable.ListCircle_background_color_selected);
                textSize = a.getDimensionPixelSize(R.styleable.ListCircle_letter_size, 0);
                mDrawableNormal = a.getDrawable(R.styleable.ListCircle_drawable_normal);
                if (color != null) {
                    mBackgroundColor = Color.parseColor(color);
                }
                if (colorSelected != null) {
                    mSelectedColor = Color.parseColor(colorSelected);
                }


            } finally {
                a.recycle();
            }
        } else {
            mBackgroundColor = Color.BLUE;
            mSelectedColor = Color.GRAY;
        }

        mDrawableSelected = getResources().getDrawable(R.drawable.ic_action_done);
        scale = res.getDisplayMetrics().density;


        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setAntiAlias(true);

        mLetterPaint.setColor(textColor);
        mLetterPaint.setTextSize(textSize == 0 ? convertFromPxToDip(24) : textSize);
        mLetterPaint.setTextAlign(Align.CENTER);
        mLetterPaint.setAntiAlias(true);

        if (!isInEditMode()) {
            Typeface font = RobotoLightTextView.getTypeface(res, 1);
            if (font != null) {
                mLetterPaint.setTypeface(font);
            }
        }
    }

    private float convertFromPxToDip(float value) {
        return scale * value;
    }

    public void setCircleBackgroundColor(int color) {
        mBackgroundColor = color;
        invalidate();
    }

    public void setCircleSelectedColor(int color) {
        mSelectedColor = color;
        invalidate();
    }

    public void setOnCircleSelectedListener(OnCircleSelectedListener l) {
        mListener = l;
    }

    public void setCircleText(String text) {
        mText = text;
        invalidate();
    }

    public void setCircleDrawable(Drawable d) {
        mDrawableSelected = d;
    }

    public void setCircleSelectedDrawable(Drawable d) {

    }

    public void setSelected(final boolean selected) {
        mBackgroundPaint.setColor(mIsSelected ? mSelectedColor : mBackgroundColor);
        mIsSelected = selected;
        invalidate();
    }

    public void setDrawable(Drawable d) {
        mDrawableNormal = d;
        invalidate();
    }

    @Override
    public boolean performClick() {
        performSelect();
        return super.performClick();
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public boolean isSelectable() {
        return mIsSelectable;
    }

    public void setSelectable(boolean selectable) {
        mIsSelectable = selectable;
        invalidate();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsSelectable) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                performSelect();
                return true;

        }
        return false;
    }

    private void performSelect() {
        if (mListener != null) {
            mListener.onCircleSelected(this, !mIsSelected);
        }
        mAnimated = false;
        final float rot = getRotationY();
        animate().rotationY(90).setListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mAnimated) {
                    mIsSelected = !mIsSelected;
                    mAnimated = true;
                    invalidate();
                    animate().rotationY(mIsSelected ? 0 : rot).setDuration(150).start();
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        }).setDuration(150).start();
    }

    @Override
    public void onDraw(Canvas canvas) {
        final int width = getWidth();
        final int height = getHeight();

        final int textX = getWidth() / 2;
        final int textY = (int) ((height / 2) - ((mLetterPaint.descent() + mLetterPaint.ascent()) / 2));
        mBackgroundPaint.setColor(mIsSelected ? mSelectedColor : mBackgroundColor);

        canvas.drawCircle(width / 2, height / 2, width / 2, mBackgroundPaint);

        if (mIsSelectable && mIsSelected && mDrawableSelected != null) {
            int left = (width / 2) - (mDrawableSelected.getIntrinsicWidth() / 2);
            int right = (width / 2) + (mDrawableSelected.getIntrinsicWidth() / 2);
            int top = (height / 2) - (mDrawableSelected.getIntrinsicHeight() / 2);
            int bottom = (height / 2) + (mDrawableSelected.getIntrinsicHeight() / 2);

            mDrawableSelected.setBounds(+left, +top, +right, +bottom);
            mDrawableSelected.draw(canvas);
        } else if (mDrawableNormal != null) {
            int left = (width / 2) - (mDrawableNormal.getIntrinsicWidth() / 2);
            int right = (width / 2) + (mDrawableNormal.getIntrinsicWidth() / 2);
            int top = (height / 2) - (mDrawableNormal.getIntrinsicHeight() / 2);
            int bottom = (height / 2) + (mDrawableNormal.getIntrinsicHeight() / 2);

            mDrawableNormal.setBounds(+left, +top, +right, +bottom);
            mDrawableNormal.draw(canvas);
        } else if (!mText.isEmpty()) {
            canvas.drawText(mText, textX, textY, mLetterPaint);
        }
    }

}
