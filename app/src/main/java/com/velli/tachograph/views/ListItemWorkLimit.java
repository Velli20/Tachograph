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
import com.velli.tachograph.DateCalculations;
import com.velli.tachograph.restingtimes.RegulationTimesSummary;
import com.velli.tachograph.restingtimes.RegulationTimesSummary.OnTotalTimesChanged;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class ListItemWorkLimit extends CardView implements OnTotalTimesChanged, OnClickListener {
	public static final int TYPE_CHART = -1;
	public static final int TYPE_FORTNIGHTLY_DRIVE = 0;
	
	public static final int TYPE_WEEKLY_REST = 1;
	public static final int TYPE_DAILY_REST= 2;
	public static final int TYPE_DAILY_DRIVE= 3;
	public static final int TYPE_WEEKLY_DRIVE= 4;
	
	public static final int TYPE_WTD_WEEKLY_WORK_TIME = 5;
	public static final int TYPE_WTD_DAILY_REST = 6;
	public static final int TYPE_WTD_WEEKLY_REST = 7;
	public static final int TYPE_WTD_NIGHT_SHIFT = 8;
	
	public static final int TYPE_BREAK = 9;
	public static final int TYPE_SPLIT_BREAK = 10;
	public static final int TYPE_CONTINIOUS_DRIVING = 11;

		
	private int mWorkLimitType = -1;
	
	private TextView mTextWorkLimitTitle;
	private PieChart mProgress;
	private ExceptionTile mExceptionTile;
	private RobotoSwitchCompat mSplitbreakSwitch;
	
	private String mTooltipTitle;
	private String mTooltipSubTitle;
	
	private WeakReference<Context> mContext;
	private boolean mShowTimesCountDown;
	
	public ListItemWorkLimit(Context context) {
		super(context);
		mContext = new WeakReference<>(context);
	}
	
	public ListItemWorkLimit(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = new WeakReference<>(context);
	}
	
	public ListItemWorkLimit(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = new WeakReference<>(context);
	}
	

	
	@Override
	protected void onAttachedToWindow() {
	    super.onAttachedToWindow();
	    // View is now attached
	    RegulationTimesSummary.getInstance().registerForOnTotalTimesChanged(this);
	}
	
	@Override
	protected void onDetachedFromWindow() {
	    super.onDetachedFromWindow();
	    // View is now detached, and about to be destroyed.
	    RegulationTimesSummary.getInstance().unregisterForOnTotalTimesChanged(this);
	}
	
	public void setType(int type){
		mWorkLimitType = type;
		update();
	}

	@Override
	public void onFinishInflate(){
		super.onFinishInflate();
		mTextWorkLimitTitle = (TextView)findViewById(R.id.text_work_limit_title);
		mProgress = (PieChart)findViewById(R.id.work_limit_chartView);
		mExceptionTile = (ExceptionTile)findViewById(R.id.work_limit_expetion_tile);
		mSplitbreakSwitch = (RobotoSwitchCompat) findViewById(R.id.work_limit_switch_split_break);
		
		if(mExceptionTile != null){
			mExceptionTile.setOnClickListener(this);
		}
	}

	public SwitchCompat getSwitch(){
		return mSplitbreakSwitch;
	}
	
	public void setShowCountDown(boolean countDown) {
		mShowTimesCountDown = countDown;
	}
	
	public void setExceptionContent(int count, String title, String text){
		
		if(count == -1 && mExceptionTile.getVisibility() == View.VISIBLE){
			mExceptionTile.setVisibility(View.GONE);
		} else if(count == -1){
			return;
		} else if(mExceptionTile.getVisibility() == View.GONE){
			mExceptionTile.setVisibility(View.VISIBLE);
		}
		mTooltipTitle = title;
		mTooltipSubTitle = text;
		
		mExceptionTile.setException(String.valueOf(count));
	}
	
	public void setProgress(int progress, int limit, boolean descending){
		if(descending){
			if(progress >= limit){
				progress = 0;
			} else {
				progress = (limit - progress);
			}
		} 
		
		mProgress.setTitle(DateCalculations.convertMinutesToTimeString(progress >= 0 ? progress : 0));
		mProgress.setSubtitle("/ " + DateCalculations.convertMinutesToTimeString(limit));
		mProgress.setMax(limit);
		mProgress.setProgress(progress);
	}
	
	public void update(){
		if(mWorkLimitType == TYPE_WTD_DAILY_REST
				|| mWorkLimitType == TYPE_WTD_WEEKLY_REST
				|| mWorkLimitType == TYPE_WTD_WEEKLY_WORK_TIME
				|| mWorkLimitType == TYPE_WTD_NIGHT_SHIFT) {
			mProgress.setChartColor(getResources().getColor(R.color.color_accent_orange_600));
		} else {
			mProgress.setChartColor(getResources().getColor(R.color.event_drving));
		}
		
		if(mWorkLimitType == TYPE_WEEKLY_REST){
			limitWeeklyResting();
		} else if(mWorkLimitType ==TYPE_DAILY_REST){
			limitDailyResting();
		} else if(mWorkLimitType == TYPE_DAILY_DRIVE){
			limitDailyDrivingTime();
		} else if(mWorkLimitType == TYPE_WEEKLY_DRIVE) {
			limitWeeklyDrivingTime();
		} else if(mWorkLimitType == TYPE_FORTNIGHTLY_DRIVE){
			limitFortnightlyDrivingTime();
		} else if(mWorkLimitType == TYPE_CONTINIOUS_DRIVING){
			limitContinuousDrivingTime();
		} else if(mWorkLimitType == TYPE_WTD_DAILY_REST){
			limitWtdDailyRest();
		} else if(mWorkLimitType == TYPE_WTD_WEEKLY_REST){
			limitWtdWeeklyRest();
		} else if(mWorkLimitType == TYPE_WTD_WEEKLY_WORK_TIME){
			limitWtdWeeklyWorkingTime();
		} else if(mWorkLimitType == TYPE_WTD_NIGHT_SHIFT){
			limitWtdWeeklyNightShiftWorkingTime();
		} else if(mWorkLimitType == TYPE_BREAK || mWorkLimitType == TYPE_SPLIT_BREAK){
			limitBreakTime();
		}

		
		
	}

	@Override
	public void timesChanged() {
		update();
	}

	@Override
	public void onClick(View v) {
		if(mContext == null || mContext.get() == null) {
			return;
		}
		final TooltipView tooltip = (TooltipView) View.inflate(mContext.get(), R.layout.view_tooltip, null);
		final PopupWindow popup = new PopupWindow(tooltip);
		final int pos[] = new int[2];
		
		v.getLocationOnScreen(pos);
		
		tooltip.setAnchorPos(((getScreenSize().x - pos[0]) ), (getScreenSize().y - pos[1]) -(((ExceptionTile)v).getTileHeight() ), v.getWidth());
		tooltip.setTitle(mTooltipTitle);
		tooltip.setSubtitle(mTooltipSubTitle);
		
		popup.setOutsideTouchable(true);
		popup.setFocusable(true);
	    popup.setBackgroundDrawable(new BitmapDrawable(getResources(), ""));
		popup.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popup.showAtLocation(v, Gravity.RIGHT|Gravity.BOTTOM, 0, 0);
		popup.setTouchInterceptor(new OnTouchListener() {
			
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				popup.dismiss();
				return false;
			}
		});
	}
	
	private Point getScreenSize(){
		final Point point = new Point();
		final WindowManager wm = (WindowManager) mContext.get().getSystemService(Context.WINDOW_SERVICE);
		final Display display = wm.getDefaultDisplay();
		
		display.getSize(point);
		return point;
	}
	

	private void limitWeeklyResting(){
		final RegulationTimesSummary rt = RegulationTimesSummary.getInstance();
		
		int progress = rt.getWeeklyRest();
		int limit =  rt.getWeeklyRestLimit();
		
		setProgress(progress, limit, mShowTimesCountDown);
		setExceptionContent(rt.getReducedWeekRestsLeft(), getString(R.string.exeption_title_reduced_weekly_rest), getString(R.string.exeption_subtitle_reduced_weekly_rest));

		mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_weekly_rest_remaining) : getString(R.string.work_limit_weekly_rest));
	}
	
	private void limitDailyResting(){
		final RegulationTimesSummary rt = RegulationTimesSummary.getInstance();
		
		int progress = rt.getDailyRest();
		int limit = rt.getDailyRestLimit();
		
		setProgress(progress, limit, mShowTimesCountDown);
		setExceptionContent(rt.getReducedDailyRestsLeft(), getString(R.string.exeption_title_reduced_daily_rest), getString(R.string.exeption_subtitle_reduced_daily_rest));
	
		mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_daily_rest_remaining) : getString(R.string.work_limit_daily_rest));
		

	}
	
	private void limitDailyDrivingTime(){
		final RegulationTimesSummary rt = RegulationTimesSummary.getInstance();
		
		int progress = rt.getDailyDrivingTime();
		int limit =  rt.getDailyDriveLimit();
		
		setProgress(progress, limit, mShowTimesCountDown);	
		setExceptionContent(rt.getExtendedDailyDrivesLeft(), getString(R.string.exeption_title_extended_drive_time), getString(R.string.exeption_subtitle_extended_drive_time));
	
		mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_daily_driving_time_remaining) : getString(R.string.work_limit_daily_driving_time));
	}
	
	private void limitWeeklyDrivingTime(){
		final RegulationTimesSummary rt = RegulationTimesSummary.getInstance();
		
		int progress = rt.getWeeklyDrivetime();
		int limit =  rt.getWeeklyDriveLimit();
		
		setProgress(progress, limit, mShowTimesCountDown);	
		setExceptionContent(-1, null, null);
	
		mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_weekly_driving_time_remaining) : getString(R.string.work_limit_weekly_driving_time));
	}
	
	private void limitFortnightlyDrivingTime(){
		final RegulationTimesSummary rt = RegulationTimesSummary.getInstance();
		
		int progress = rt.getFortnightlyDriveTime();
		int limit =  rt.getFortnightlyDriveLimit();
		
		setProgress(progress, limit, mShowTimesCountDown);	
		setExceptionContent(-1, null, null);
	
		mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_fortnightly_drivetime_remaining) : getString(R.string.work_limit_fortnightly_drivetime));
	}
	
	private void limitContinuousDrivingTime(){
		final RegulationTimesSummary rt = RegulationTimesSummary.getInstance();
		
		int progress = rt.getContiniousDriveTime();
		int limit =  rt.getContiniousDriveLimit();
		
		setProgress(progress, limit, mShowTimesCountDown);	
		setExceptionContent(-1, null, null);
	
		mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_continious_driving_time_remaining) : getString(R.string.work_limit_continious_driving_time));
	}
	
	private void limitBreakTime(){
		final RegulationTimesSummary rt = RegulationTimesSummary.getInstance();
		
		int progress = rt.getContiniousBreak();
		int limit =  rt.getContiniousBreakLimit();
		
		setProgress(progress, limit, mShowTimesCountDown);	
		setExceptionContent(-1, null, null);
	
		mSplitbreakSwitch.setChecked(mWorkLimitType == TYPE_SPLIT_BREAK, false);
		mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_break_time_remaining) : getString(R.string.work_limit_break_time));
	}
	
	private void limitWtdDailyRest(){
		final RegulationTimesSummary rt = RegulationTimesSummary.getInstance();
		
		int progress = rt.getWTDDailyRestingTime();
		int limit =  rt.getWTDDailyRestingLimit();
		
		setProgress(progress, limit, mShowTimesCountDown);	
		setExceptionContent(-1, null, null);
	
		mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_wtd_daily_rest_remaining) : getString(R.string.work_limit_wtd_daily_rest));
	}
	
	private void limitWtdWeeklyRest(){
		final RegulationTimesSummary rt = RegulationTimesSummary.getInstance();
		
		int progress = rt.getWTDWeeklyRestingTime();
		int limit =  rt.getWTDWeeklyRestingLimit();
		
		setProgress(progress, limit, mShowTimesCountDown);	
		setExceptionContent(-1, null, null);
	
		mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_wtd_weekly_rest_remaining) : getString(R.string.work_limit_wtd_weekly_rest));
	}
	
	private void limitWtdWeeklyWorkingTime(){
		final RegulationTimesSummary rt = RegulationTimesSummary.getInstance();
		
		int progress = rt.getWTDWeeklyWorkTime();
		int limit =  rt.getWTDWeeklyWorkLimit();
		
		setProgress(progress, limit, mShowTimesCountDown);	
		setExceptionContent(-1, null, null);
	
		mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_wtd_weekly_working_remaining) : getString(R.string.work_limit_wtd_weekly_working));
		

	}
	
	private void limitWtdWeeklyNightShiftWorkingTime(){
		final RegulationTimesSummary rt = RegulationTimesSummary.getInstance();
		
		int progress = rt.getWtdNightShiftWorkingTime();
		int limit =  rt.getWTDNightShiftWorkingTimeLimit();

		setProgress(progress, limit, mShowTimesCountDown);	
		setExceptionContent(-1, null, null);
	
		mTextWorkLimitTitle.setText(mShowTimesCountDown ? getString(R.string.work_limit_wtd_night_shift_working_time_remaining) : getString(R.string.work_limit_wtd_night_shift_working_time));
	}
	
	public String getString(int id){
		return getResources().getString(id);
	}
}
