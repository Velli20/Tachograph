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
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public abstract class ActivityProgress extends View {
    private static final int STROKE_WIDTH = 14;
    private static final int TITLE_SIZE = 16;
    private static final int SUB_TITLE_SIZE = 12;
    private static final int TEXT_PADDING = 2;

    private float mScale;
    private int mProgress;
    private int mProgressMax;

    private String mTitle;
    private String mSubTitle;

    private final Paint mBackGroundPaint = new Paint();
    private final Paint mChartPaint = new Paint();

    private final TextPaint mTitlePaint = new TextPaint();
    private final TextPaint mSubTitlePaint = new TextPaint();


    public ActivityProgress(Context context) {
        this(context, null, -1);
    }

    public ActivityProgress(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ActivityProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final Resources res = getResources();

        mScale = res.getDisplayMetrics().density;

        mBackGroundPaint.setColor(Color.BLACK);
        mBackGroundPaint.setAntiAlias(true);
        mBackGroundPaint.setStyle(Paint.Style.STROKE);
        mBackGroundPaint.setStrokeWidth(getDpValue(STROKE_WIDTH));
        mBackGroundPaint.setStrokeCap(Paint.Cap.ROUND);
        mBackGroundPaint.setAlpha(31); // ~12 %

        mChartPaint.setColor(Color.parseColor("#03a9f4"));
        mChartPaint.setAntiAlias(true);
        mChartPaint.setStyle(Paint.Style.STROKE);
        mChartPaint.setStrokeWidth(getDpValue(STROKE_WIDTH));
        mChartPaint.setStrokeCap(Paint.Cap.ROUND);

        mTitlePaint.setColor(Color.BLACK);
        mTitlePaint.setAntiAlias(true);
        mTitlePaint.setTextSize(getDpValue(TITLE_SIZE));
        mTitlePaint.setTextAlign(Paint.Align.CENTER);
        mTitlePaint.setAlpha(222); // ~87 %


        mSubTitlePaint.setColor(Color.BLACK);
        mSubTitlePaint.setAntiAlias(true);
        mSubTitlePaint.setTextSize(getDpValue(SUB_TITLE_SIZE));
        mSubTitlePaint.setTextAlign(Paint.Align.CENTER);
        mSubTitlePaint.setAlpha(138); // ~54 %

        if(isInEditMode()) {
            mProgress = 50;
            mProgressMax = 100;
            mTitle = "30 min";
            mSubTitle = " / 1 h";

        } else {
            mTitlePaint.setTypeface(RobotoLightTextView.getTypeface(getResources(), 4));
            mSubTitlePaint.setTypeface(RobotoLightTextView.getTypeface(getResources(), 4));
        }

        setWillNotDraw(false);
    }

    float getDpValue(float value){
        return mScale * value;
    }

    public int getProgress(){
        return mProgress;
    }

    public int getMax() { return mProgressMax; }

    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

    public void setMax(int max) {
        mProgressMax = max;
        invalidate();
    }

    public void setTitle(String title) {
        mTitle = title;
        invalidate();
    }

    public void setSubTitle(String subTitle) {
        mSubTitle = subTitle;
        invalidate();
    }

    public void setChartColor(int color){
        mChartPaint.setColor(color);
        invalidate();
    }

    Paint getChartPaint() { return mChartPaint; }

    Paint getChartBackgroundPaint() { return mBackGroundPaint; }

    TextPaint getTitlePaint() { return mTitlePaint; }

    TextPaint getSubTitlePaint() { return mSubTitlePaint; }

    String getTitle() { return mTitle; }

    String getSubTitle() { return mSubTitle; }

    float getTextPadding() { return getDpValue(TEXT_PADDING); }


    @Override
    public void onDraw(Canvas canvas) {
        drawProgress(canvas, mProgress, mProgressMax);
    }

    public abstract void drawProgress(Canvas canvas, float progress, float max);
}
