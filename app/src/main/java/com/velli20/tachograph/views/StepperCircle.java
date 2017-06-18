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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class StepperCircle extends View {
    private static final boolean DEBUG = true;
    private static final String TAG = "OverviewCircle ";

    public static final int START_CIRCLE_SIZE = 13;
    public static final int NORMAL_CIRCLE_SIZE = 10;

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_START = 1;
    public static final int TYPE_END = 2;
    public static final int TYPE_NO_BARS = 3;

    private float scale;
    private int mType = TYPE_NORMAL;

    private final Paint mPaint = new Paint();
    private final Paint mCirclePaintNormal = new Paint();
    private final Paint mCirclePaintStart = new Paint();


    public StepperCircle(Context context) {
        super(context);
        init(context, null);
    }

    public StepperCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StepperCircle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StepperCircle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }


    private void init(Context c, AttributeSet attrs) {
        final Resources res = getResources();
        scale = res.getDisplayMetrics().density;

        if (attrs != null) {
            TypedArray a = c.getTheme().obtainStyledAttributes(attrs, R.styleable.StepperCircle, 0, 0);

            int color = a.getColor(R.styleable.StepperCircle_circleColor, getResources().getColor(R.color.event_driving));

            mPaint.setColor(color);
        }

        mPaint.setStyle(Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(getDpValue(START_CIRCLE_SIZE + 3));
        mPaint.setColor(res.getColor(R.color.color_primary));

        mCirclePaintNormal.setStyle(Style.FILL);
        mCirclePaintNormal.setAntiAlias(true);
        mCirclePaintNormal.setColor(Color.WHITE);


        mCirclePaintStart.setStyle(Style.STROKE);
        mCirclePaintStart.setAntiAlias(true);
        mCirclePaintStart.setStrokeWidth(getDpValue(3));
        mCirclePaintStart.setColor(Color.GRAY);
        setWillNotDraw(false);

    }

    public void setColor(int color) {

    }


    public void setType(int type) {
        mType = type;
        invalidate();
    }

    private float getDpValue(float value) {
        return scale * value;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        int cx = width / 2;
        int cy = height / 2;

        if (mType == TYPE_NO_BARS) {
            float circleRadius = getDpValue(START_CIRCLE_SIZE) / 2;
            canvas.drawCircle(cx, cy, circleRadius, mCirclePaintStart);
        } else if (mType == TYPE_NORMAL) {
            float circleDiameter = getDpValue(NORMAL_CIRCLE_SIZE);
            float circleRadius = circleDiameter / 2;


            canvas.drawLine(cx, 0, cx, height, mPaint);

            canvas.drawCircle(width / 2, height / 2, circleRadius, mCirclePaintNormal);
        } else if (mType == TYPE_START) {
            float circleDiameter = getDpValue(START_CIRCLE_SIZE);
            float circleRadius = circleDiameter / 2;
            float circleRadiusNormal = getDpValue(NORMAL_CIRCLE_SIZE) / 2;

            canvas.drawLine(cx, cy, cx, height, mPaint);
            canvas.drawCircle(cx, cy, circleRadius, mCirclePaintStart);
            canvas.drawCircle(cx, cy, circleRadiusNormal, mCirclePaintNormal);
        } else if (mType == TYPE_END) {
            float circleDiameter = getDpValue(START_CIRCLE_SIZE);
            float circleRadius = circleDiameter / 2;
            float circleRadiusNormal = getDpValue(NORMAL_CIRCLE_SIZE) / 2;

            canvas.drawLine(cx, 0, cx, cy, mPaint);
            canvas.drawCircle(cx, cy, circleRadius, mCirclePaintStart);
            canvas.drawCircle(cx, cy, circleRadiusNormal, mCirclePaintNormal);
        }


    }
}
