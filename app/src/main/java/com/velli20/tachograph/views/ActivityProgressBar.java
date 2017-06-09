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
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;


public class ActivityProgressBar extends ActivityProgress {
    private static final int PROGRESS_BAR_PADDING = 16;

    public ActivityProgressBar(Context context) {
        this(context, null, -1);
    }

    public ActivityProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ActivityProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        getTitlePaint().setTextAlign(Paint.Align.LEFT);
        getSubTitlePaint().setTextAlign(Paint.Align.LEFT);
    }


    @Override
    public void drawProgress(Canvas canvas, float progress, float max) {
        Paint progressPaint = getChartPaint();
        Paint backgroundPaint = getChartBackgroundPaint();

        TextPaint titlePaint = getTitlePaint();
        TextPaint subTitlePaint = getSubTitlePaint();

        String title = getTitle();
        String subTitle = getSubTitle();

        float progressStrokeWidth = progressPaint.getStrokeWidth();
        float capWidth = progressStrokeWidth / 2;
        float left = getPaddingLeft() + capWidth;
        float right = (getWidth() - getPaddingRight())- capWidth;


        float cy = (getHeight() / 2);

        float titleHeight = 0;
        float titleWidth = 0;
        float subTitleHeight = 0;
        float subTitleWidth = 0;
        float textPadding = getTextPadding();
        float barPadding = getDpValue(PROGRESS_BAR_PADDING);

        if(title != null) {
            titleHeight = titlePaint.getTextSize();
            titleWidth = titlePaint.measureText("24 h 60 min");
        }
        if(subTitle != null) {
            subTitleHeight = subTitlePaint.getTextSize();
            subTitleWidth = subTitlePaint.measureText(subTitle);
        }

        left += (Math.max(titleWidth, subTitleWidth) + (textPadding)) + barPadding;

        float titleCy = (subTitleHeight == 0 ? (cy - (titleHeight / 2)) : (cy - textPadding));
        float subTitleCy = (titleHeight == 0 ? cy : (cy + subTitleHeight + textPadding));

        float progressBarWidth = Math.min(progress * (right - left) / max + left, right);

        canvas.drawLine(left, cy, right, cy, backgroundPaint);
        canvas.drawLine(left, cy, progressBarWidth, cy, progressPaint);


        if(title != null) {
            titleWidth = titlePaint.measureText(title);
            canvas.drawText(title, getPaddingLeft(), titleCy, titlePaint);
        }
        if(subTitle != null) {
            float subTitleCx = Math.max(getPaddingLeft() + ((titleWidth - subTitleWidth) / 2), getPaddingLeft());
            canvas.drawText(subTitle, subTitleCx, subTitleCy, subTitlePaint);
        }
    }


}
