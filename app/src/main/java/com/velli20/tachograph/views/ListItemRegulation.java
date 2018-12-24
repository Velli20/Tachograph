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
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.velli20.tachograph.DateUtils;
import com.velli20.tachograph.R;
import com.velli20.tachograph.restingtimes.OnRegulationsChangedListener;
import com.velli20.tachograph.restingtimes.Regulations;

import java.lang.ref.WeakReference;

public class ListItemRegulation extends CardView implements OnClickListener, OnRegulationsChangedListener {
    public static final int TYPE_CHART = -1;
    public static final int TYPE_DAILY_DRIVE = 0;
    public static final int TYPE_DAILY_REST = 1;
    public static final int TYPE_FORTNIGHTLY_DRIVE = 2;
    public static final int TYPE_WEEKLY_REST = 3;
    public static final int TYPE_WEEKLY_DRIVE = 4;
    public static final int TYPE_WTD_WEEKLY_WORK_TIME = 5;

    public static final int TYPE_BREAK = 6;
    public static final int TYPE_CONTINUOUS_DRIVING = 7;


    private int mWorkLimitType = -1;

    private TextView mTextWorkLimitTitle;
    private ActivityProgress mProgress;


    private ExceptionTile mExceptionTile;

    private String mTooltipTitle;
    private String mTooltipSubTitle;

    private WeakReference<Context> mContext;

    private boolean mShowTimesCountDown;
    private boolean mShowPieChart = true;

    public ListItemRegulation(Context context) {
        super(context);
        mContext = new WeakReference<>(context);
    }

    public ListItemRegulation(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = new WeakReference<>(context);
    }

    public ListItemRegulation(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = new WeakReference<>(context);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            Regulations.getInstance().registerOnRegulationsChangedListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            Regulations.getInstance().unregisterOnRegulationsChangedListener(this);
        }
    }

    public void setType(int type) {
        mWorkLimitType = type;
        update();
    }

    public void showProgressInPieChart(boolean show) {
        mShowPieChart = show;
        mProgress.displayBigProgress(show);
        update();
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mTextWorkLimitTitle = (TextView) findViewById(R.id.text_work_limit_title);
        mProgress = (ActivityProgress) findViewById(R.id.work_limit_progress);

        mExceptionTile = (ExceptionTile) findViewById(R.id.work_limit_exception_tile);

        if (mExceptionTile != null) {
            mExceptionTile.setOnClickListener(this);
        }
    }

    public void setShowCountDown(boolean countDown) {
        mShowTimesCountDown = countDown;
    }

    public void setExceptionContent(int count, String title, String text) {

        if (count == -1 && mExceptionTile.getVisibility() == View.VISIBLE) {
            mExceptionTile.setVisibility(View.GONE);
        } else if (count == -1) {
            return;
        } else if (mExceptionTile.getVisibility() == View.GONE) {
            mExceptionTile.setVisibility(View.VISIBLE);
        }
        mTooltipTitle = title;
        mTooltipSubTitle = text;

        mExceptionTile.setException(String.valueOf(count));
    }

    public void setProgress(int progress, int limit) {

        mProgress.setTitle(DateUtils.convertMinutesToTimeString(Math.max(progress, 0)));
        mProgress.setSubTitle("/ " + DateUtils.convertMinutesToTimeString(limit));
        mProgress.setMax(limit);
        mProgress.setProgress(progress);
    }

    public void update() {
        if (isInEditMode()) {
            return;
        }
        mProgress.setChartColor(getResources().getColor(R.color.color_accent));


        if (mWorkLimitType == TYPE_WEEKLY_REST) {
            limitWeeklyResting();
        } else if (mWorkLimitType == TYPE_DAILY_REST) {
            limitDailyResting();
        } else if (mWorkLimitType == TYPE_DAILY_DRIVE) {
            limitDailyDrivingTime();
        } else if (mWorkLimitType == TYPE_WEEKLY_DRIVE) {
            limitWeeklyDrivingTime();
        } else if (mWorkLimitType == TYPE_FORTNIGHTLY_DRIVE) {
            limitFortnightlyDrivingTime();
        } else if (mWorkLimitType == TYPE_CONTINUOUS_DRIVING) {
            limitContinuousDrivingTime();
        } else if (mWorkLimitType == TYPE_WTD_WEEKLY_WORK_TIME) {
            limitWtdWeeklyWorkingTime();
        }
        if (mWorkLimitType == TYPE_BREAK) {
            limitBreakTime();
        }

    }


    @Override
    public void onClick(View v) {
        if (mContext == null || mContext.get() == null) {
            return;
        }
        final TooltipView tooltip = (TooltipView) View.inflate(mContext.get(), R.layout.view_tooltip, null);
        final PopupWindow popup = new PopupWindow(tooltip);
        final int pos[] = new int[2];

        v.getLocationOnScreen(pos);

        tooltip.setAnchorPos(((getScreenSize().x - pos[0])), (getScreenSize().y - pos[1]) - (((ExceptionTile) v).getTileHeight()), v.getWidth());
        tooltip.setTitle(mTooltipTitle);
        tooltip.setSubtitle(mTooltipSubTitle);

        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(new BitmapDrawable(getResources(), ""));
        popup.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.showAtLocation(v, Gravity.RIGHT | Gravity.BOTTOM, 0, 0);
        popup.setTouchInterceptor(new OnTouchListener() {


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popup.dismiss();
                return false;
            }
        });
    }

    private Point getScreenSize() {
        final Point point = new Point();
        final WindowManager wm = (WindowManager) mContext.get().getSystemService(Context.WINDOW_SERVICE);
        final Display display = wm.getDefaultDisplay();

        display.getSize(point);
        return point;
    }


    private void limitWeeklyResting() {
        final Regulations rt = Regulations.getInstance();

        int progress = mShowTimesCountDown ? rt.getRemainingWeeklyRest() : rt.getWeeklyRest();
        int limit = rt.getWeeklyRestLimit();

        setProgress(progress, limit);
        setExceptionContent(rt.getRemainingReducedWeeklyRestsLeft(), getString(R.string.exception_title_reduced_weekly_rest), getString(R.string.exception_subtitle_reduced_weekly_rest));

        mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_weekly_rest_remaining) : getString(R.string.work_limit_weekly_rest));
    }

    private void limitDailyResting() {
        final Regulations rt = Regulations.getInstance();

        int progress = mShowTimesCountDown ? rt.getRemainingDailyRest() : rt.getDailyRest();
        int limit = rt.getDailyRestLimit();

        setProgress(progress, limit);
        setExceptionContent(rt.getRemainingReducedDailyRestsLeft(), getString(R.string.exception_title_reduced_daily_rest), getString(R.string.exception_subtitle_reduced_daily_rest));

        mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_daily_rest_remaining) : getString(R.string.work_limit_daily_rest));


    }

    private void limitDailyDrivingTime() {
        final Regulations rt = Regulations.getInstance();

        int progress = mShowTimesCountDown ? rt.getRemainingDailyDrivingTime() : rt.getDailyDrivingTime();
        int limit = rt.getDailyDrivingTimeLimit();

        setProgress(progress, limit);
        setExceptionContent(rt.getRemainingExtendedDrivingDaysLeft(), getString(R.string.exception_title_extended_drive_time), getString(R.string.exception_subtitle_extended_drive_time));

        mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_daily_driving_time_remaining) : getString(R.string.work_limit_daily_driving_time));
    }

    private void limitWeeklyDrivingTime() {
        final Regulations rt = Regulations.getInstance();

        int progress = mShowTimesCountDown ? rt.getRemainingWeeklyDrivingTime() : rt.getWeeklyDrivingTime();
        int limit = rt.getWeeklyDrivingTimeLimit();

        setProgress(progress, limit);
        setExceptionContent(-1, null, null);

        mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_weekly_driving_time_remaining) : getString(R.string.work_limit_weekly_driving_time));
    }

    private void limitFortnightlyDrivingTime() {
        final Regulations rt = Regulations.getInstance();

        int progress = mShowTimesCountDown ? rt.getRemainingFortnightlyDrivingTime() : rt.getFortnightlyDrivingTime();
        int limit = rt.getFortnightlyDrivingTimeLimit();

        setProgress(progress, limit);
        setExceptionContent(-1, null, null);

        mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_fortnightly_driving_time_remaining) : getString(R.string.work_limit_fortnightly_driving_time));
    }

    private void limitContinuousDrivingTime() {
        final Regulations rt = Regulations.getInstance();

        int progress = mShowTimesCountDown ? rt.getRemainingContinuousDrivingTime() : rt.getContinuousDrivingTime();
        int limit = rt.getContinuousDrivingTimeLimit();

        setProgress(progress, limit);
        setExceptionContent(-1, null, null);

        mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_continuous_driving_time_remaining) : getString(R.string.work_limit_continuous_driving_time));
    }

    private void limitBreakTime() {
        final Regulations rt = Regulations.getInstance();

        int progress = mShowTimesCountDown ? rt.getRemainingBreakTime() : rt.getBreakTime();
        int limit = rt.getBreakTimeLimit();

        setProgress(progress, limit);
        setExceptionContent(-1, null, null);

        mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_break_time_remaining) : getString(R.string.work_limit_break_time));
    }

    private void limitWtdWeeklyWorkingTime() {
        final Regulations rt = Regulations.getInstance();

        int progress = mShowTimesCountDown ? rt.getRemainingWtdWeeklyWorkingTime() : rt.getWtdWeeklyWorkingTime();
        int limit = rt.getWtdWeeklyWorkingTimeLimit();

        setProgress(progress, limit);
        setExceptionContent(-1, null, null);

        mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_wtd_weekly_working_remaining) : getString(R.string.work_limit_wtd_weekly_working));


    }

    public String getString(int id) {
        return getResources().getString(id);
    }

    @Override
    public void onRegulationsChanged() {
        update();
    }
}
