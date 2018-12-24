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
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class ActivityProgress extends View {
    private static final int STROKE_WIDTH = 14;
    private static final int TITLE_SIZE = 14;
    private static final int SUB_TITLE_SIZE = 12;
    private static final int TEXT_PADDING = 2;
    private static final int PROGRESS_BAR_PADDING = 16;
    private static final int SUB_TITLE_PADDING = 16;
    private static final int PROGRESS_PIE_MAX_DIAMETER = 140;
    private final Paint mBackGroundPaint = new Paint();
    private final Paint mChartPaint = new Paint();
    private final TextPaint mTitlePaint = new TextPaint();
    private final TextPaint mSubTitlePaint = new TextPaint();
    private float mScale;
    private float mHeight;
    private float mWidth;
    private int mProgress;
    private int mProgressMax;
    private boolean mDisplayBigProgress = false;
    private String mTitle;
    private String mSubTitle;
    private RectF mArcRect;


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

        if (isInEditMode()) {
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

    float getDpValue(float value) {
        return mScale * value;
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

    public int getMax() {
        return mProgressMax;
    }

    public void setMax(int max) {
        mProgressMax = max;
        invalidate();
    }

    public void setChartColor(int color) {
        mChartPaint.setColor(color);
        invalidate();
    }

    public void displayBigProgress(boolean big) {
        if (big != mDisplayBigProgress) {
            mDisplayBigProgress = big;

            requestLayout();
        }
    }

    Paint getChartPaint() {
        return mChartPaint;
    }

    Paint getChartBackgroundPaint() {
        return mBackGroundPaint;
    }

    TextPaint getTitlePaint() {
        return mTitlePaint;
    }

    TextPaint getSubTitlePaint() {
        return mSubTitlePaint;
    }

    String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
        invalidate();
    }

    String getSubTitle() {
        return mSubTitle;
    }

    public void setSubTitle(String subTitle) {
        mSubTitle = subTitle;
        invalidate();
    }

    float getTextPadding() {
        return getDpValue(TEXT_PADDING);
    }


    @Override
    public void onDraw(Canvas canvas) {
        getTitlePaint().setTextAlign(mDisplayBigProgress ? Paint.Align.CENTER : Paint.Align.LEFT);
        getSubTitlePaint().setTextAlign(mDisplayBigProgress ? Paint.Align.CENTER : Paint.Align.LEFT);

        if (mDisplayBigProgress) {
            drawProgressChart(canvas, mProgress, mProgressMax);
        } else {
            drawProgressBar(canvas, mProgress, mProgressMax);
        }

    }


    private void drawProgressBar(Canvas canvas, float progress, float max) {
        Paint progressPaint = getChartPaint();
        Paint backgroundPaint = getChartBackgroundPaint();

        TextPaint titlePaint = getTitlePaint();
        TextPaint subTitlePaint = getSubTitlePaint();

        String title = getTitle();
        String subTitle = getSubTitle();

        float progressStrokeWidth = progressPaint.getStrokeWidth();
        float capWidth = progressStrokeWidth / 2;
        float left = getPaddingLeft() + capWidth;
        float right = (getWidth() - getPaddingRight()) - capWidth;


        float cy = (getHeight() / 2);

        float titleHeight = 0;
        float titleWidth = 0;
        float titleMaxWidth = titlePaint.measureText("24 h 60 min");
        float subTitleHeight = 0;
        float subTitleWidth = 0;
        float textPadding = getTextPadding();
        float barPadding = getDpValue(PROGRESS_BAR_PADDING);

        if (title != null) {
            titleHeight = titlePaint.getTextSize();
            titleWidth = titlePaint.measureText(title);
        }
        if (subTitle != null) {
            subTitleHeight = subTitlePaint.getTextSize();
            subTitleWidth = subTitlePaint.measureText(subTitle);
        }

        left += (Math.max(titleMaxWidth, subTitleWidth) + (textPadding)) + barPadding;

        float titleCy = (subTitleHeight == 0 ? (cy - (titleHeight / 2)) : (cy - textPadding));
        float subTitleCy = (titleHeight == 0 ? cy : (cy + subTitleHeight + textPadding));

        float progressBarWidth = Math.min(progress * (right - left) / max + left, right);

        canvas.drawLine(left, cy, right, cy, backgroundPaint);
        canvas.drawLine(left, cy, progressBarWidth, cy, progressPaint);


        if (title != null) {
            float titleCx = getPaddingLeft() + ((titleMaxWidth - titleWidth) / 2);
            canvas.drawText(title, titleCx, titleCy, titlePaint);
        }
        if (subTitle != null) {
            float subTitleCx = getPaddingLeft() + ((titleMaxWidth - subTitleWidth) / 2);
            canvas.drawText(subTitle, subTitleCx, subTitleCy, subTitlePaint);
        }
    }

    private void drawProgressChart(Canvas canvas, float progress, float max) {

        float width = mWidth;
        float height = mHeight;

        Paint progressPaint = getChartPaint();
        Paint backgroundPaint = getChartBackgroundPaint();

        TextPaint titlePaint = getTitlePaint();
        TextPaint subTitlePaint = getSubTitlePaint();

        String title = getTitle();
        String subTitle = getSubTitle();

        if (width > height) {
            height = width;
        } else if (height > width) {
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
        if (arc != 0) {
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

        if (mDisplayBigProgress) {
            int maxDiameter = (int) getDpValue(PROGRESS_PIE_MAX_DIAMETER);

            if (widthSize > heightSize && widthSize < maxDiameter) {
                finalWidth = widthSize;
                finalHeight = widthSize;
            } else if (heightSize > widthSize && heightSize < maxDiameter) {
                finalWidth = heightSize;
                finalHeight = heightSize;
            } else {
                finalWidth = maxDiameter;
                finalHeight = maxDiameter;
            }
        } else {
            int minHeight = (int) (mTitlePaint.getTextSize() + mSubTitlePaint.getTextSize() + (getTextPadding() * 2));
            finalWidth = widthSize;
            finalHeight = Math.max(minHeight, heightSize);
        }

        setMeasuredDimension(finalWidth, finalHeight);
    }

    private RectF getArcRect() {

        final RectF rect = new RectF();
        final float stroke = getDpValue(STROKE_WIDTH) / 2;

        rect.set(stroke, stroke, mWidth - stroke, mHeight - stroke);
        return rect;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getWidth();
        mHeight = getHeight();

        mArcRect = getArcRect();

    }
}
