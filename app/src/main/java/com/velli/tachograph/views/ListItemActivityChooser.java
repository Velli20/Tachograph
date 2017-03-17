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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;

import com.velli.tachograph.DateCalculations;
import com.velli.tachograph.Event;
import com.velli.tachograph.R;
import com.velli.tachograph.restingtimes.RegulationTimesSummary;

public class ListItemActivityChooser extends CardView implements View.OnClickListener {
	private TextViewTimeCounter mCounter;
	private RobotoLightTextView mNextEvent;

    private RobotoButton mToggleDriving;
    private RobotoButton mToggleResting;
    private RobotoButton mToggleOtherWork;
    private RobotoButton mTogglePoa;

    private View mDivider;
    private OnClickListener mListener;

	public ListItemActivityChooser(Context context) {
		super(context);
	}
	
	public ListItemActivityChooser(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ListItemActivityChooser(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

        mCounter = (TextViewTimeCounter) findViewById(R.id.list_item_activity_event_chooser_counter);
        mNextEvent = (RobotoLightTextView) findViewById(R.id.list_item_activity_event_chooser_next_event);

        mToggleDriving = (RobotoButton) findViewById(R.id.list_item_activity_event_chooser_driving);
        mToggleResting = (RobotoButton) findViewById(R.id.list_item_activity_event_chooser_resting);
        mToggleOtherWork = (RobotoButton) findViewById(R.id.list_item_activity_event_chooser_other_work);
        mTogglePoa = (RobotoButton) findViewById(R.id.list_item_activity_event_chooser_poa);

        mDivider = findViewById(R.id.list_item_activity_event_chooser_divider);

        mToggleDriving.setOnClickListener(this);
        mToggleResting.setOnClickListener(this);
        mToggleOtherWork.setOnClickListener(this);
        mTogglePoa.setOnClickListener(this);
	}

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        if(mListener != null) {
            mListener.onClick(v);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mListener = l;
    }

    public void setEvent(Event ev) {
        if(ev != null) {
            setToggle(ev.getEventType());
            mCounter.setVisibility(View.VISIBLE);
            mDivider.setVisibility(View.VISIBLE);
            mCounter.startTimer(ev.getStartDateInMillis());
            mCounter.setTitle(getResources().getStringArray(R.array.event_explanations)[ev.getEventType()]);
            setNextEventInfo(ev);
        } else {
            setToggle(-1);
            mCounter.stopTimer();
            mCounter.setVisibility(View.GONE);
            mNextEvent.setVisibility(View.GONE);
            mDivider.setVisibility(View.GONE);
        }
    }

    private void setNextEventInfo(Event ev) {
        final Resources res = getResources();
        final RegulationTimesSummary summary = RegulationTimesSummary.getInstance();
        final int nextEventType = summary.getNextEvent(ev.getEventType());

        final StringBuilder nextEvent = new StringBuilder();


        if(nextEventType == Event.EVENT_TYPE_NORMAL_BREAK) {
            nextEvent.append(res.getString(R.string.notification_next_event_break)).append(", ").append(DateCalculations.convertMinutesToTimeString(summary.getContiniousBreakLimit()));
        } else if(nextEventType == Event.EVENT_TYPE_DAILY_REST){
            nextEvent.append(res.getString(R.string.notification_next_event_daily_rest)).append(", ").append(DateCalculations.convertMinutesToTimeString(summary.getDailyRestLimit()));
        } else if(nextEventType == Event.EVENT_TYPE_WEEKLY_REST){
            nextEvent.append(res.getString(R.string.notification_next_event_weekly_rest)).append(", ").append(DateCalculations.convertMinutesToTimeString(summary.getWeeklyRestLimit()));
        } else if(nextEventType == Event.EVENT_TYPE_DRIVING){
            nextEvent.append(res.getString(R.string.notification_next_event_driving)).append(", ").append(DateCalculations.convertMinutesToTimeString(summary.getContiniousDriveLimit()));
        }

        if(!nextEvent.toString().isEmpty()) {
            nextEvent.append("\n").append(DateCalculations.createDateTimeString(summary.getNextEventStartTime(ev.getEventType())));
        } else if(mNextEvent.getVisibility() == View.VISIBLE) {
            mNextEvent.setVisibility(View.GONE);
            return;
        }

        mNextEvent.setText(nextEvent.toString());

        if(mNextEvent.getVisibility() == View.GONE) {
            mNextEvent.setVisibility(View.VISIBLE);
        }
    }

    private void setToggle(int toggle) {
        if(toggle == Event.EVENT_TYPE_DRIVING) {
            Drawable d = getResources().getDrawable(R.drawable.ic_driving_large_on);
            d.setBounds(0, 0, d != null ? d.getIntrinsicWidth() : 0, d.getIntrinsicHeight());
            mToggleDriving.setCompoundDrawables(null, d, null, null);
        } else {
            Drawable d = getResources().getDrawable(R.drawable.ic_driving_large_off);
            d.setBounds(0, 0, d != null ? d.getIntrinsicWidth() : 0, d.getIntrinsicHeight());
            mToggleDriving.setCompoundDrawables(null, d, null, null);
        }
        if(toggle == Event.EVENT_TYPE_DAILY_REST
                || toggle == Event.EVENT_TYPE_NORMAL_BREAK
                || toggle == Event.EVENT_TYPE_WEEKLY_REST) {
            Drawable d = getResources().getDrawable(R.drawable.ic_rest_large_on);
            d.setBounds(0, 0, d != null ? d.getIntrinsicWidth() : 0, d.getIntrinsicHeight());
            mToggleResting.setCompoundDrawables(null, d, null, null);

        } else {
            Drawable d = getResources().getDrawable(R.drawable.ic_rest_large_off);
            d.setBounds(0, 0, d != null ? d.getIntrinsicWidth() : 0, d.getIntrinsicHeight());
            mToggleResting.setCompoundDrawables(null, d, null, null);
        }
        if(toggle == Event.EVENT_TYPE_OTHER_WORK) {
            Drawable d = getResources().getDrawable(R.drawable.ic_other_work_large_on);
            d.setBounds(0, 0, d != null ? d.getIntrinsicWidth() : 0, d.getIntrinsicHeight());
            mToggleOtherWork.setCompoundDrawables(null, d, null, null);
        } else {
            Drawable d = getResources().getDrawable(R.drawable.ic_other_work_large_off);
            d.setBounds(0, 0, d != null ? d.getIntrinsicWidth() : 0, d.getIntrinsicHeight());
            mToggleOtherWork.setCompoundDrawables(null, d, null, null);
        }
        if(toggle == Event.EVENT_TYPE_POA) {
            Drawable d = getResources().getDrawable(R.drawable.ic_poa_large_on);
            d.setBounds(0, 0, d != null ? d.getIntrinsicWidth() : 0, d.getIntrinsicHeight());
            mTogglePoa.setCompoundDrawables(null, d, null, null);
        } else {
            Drawable d = getResources().getDrawable(R.drawable.ic_poa_large_off);
            d.setBounds(0, 0, d != null ? d.getIntrinsicWidth() : 0, d.getIntrinsicHeight());
            mTogglePoa.setCompoundDrawables(null, d, null, null);
        }
    }
}
