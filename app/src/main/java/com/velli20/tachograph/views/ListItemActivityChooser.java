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
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;

import com.velli20.tachograph.Event;
import com.velli20.tachograph.R;

import java.lang.ref.WeakReference;


public class ListItemActivityChooser extends CardView implements View.OnClickListener {
	private TextViewTimeCounter mCounter;

    private RobotoButton mToggleDriving;
    private RobotoButton mToggleResting;
    private RobotoButton mToggleOtherWork;
    private RobotoButton mTogglePoa;

    private WeakReference<OnClickListener> mListener;

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

        mToggleDriving = (RobotoButton) findViewById(R.id.list_item_activity_event_chooser_driving);
        mToggleResting = (RobotoButton) findViewById(R.id.list_item_activity_event_chooser_resting);
        mToggleOtherWork = (RobotoButton) findViewById(R.id.list_item_activity_event_chooser_other_work);
        mTogglePoa = (RobotoButton) findViewById(R.id.list_item_activity_event_chooser_poa);


        mToggleDriving.setOnClickListener(this);
        mToggleResting.setOnClickListener(this);
        mToggleOtherWork.setOnClickListener(this);
        mTogglePoa.setOnClickListener(this);
	}


    @Override
    public void onClick(View v) {
        if(mListener != null && mListener.get() != null) {
            mListener.get().onClick(v);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mListener = new WeakReference<>(l);
    }

    public void setEvent(Event ev) {
        if(ev != null) {
            setToggle(ev.getEventType());
            mCounter.setVisibility(View.VISIBLE);

            mCounter.setTitle(getResources().getStringArray(R.array.event_explanations)[ev.getEventType()]);
            mCounter.startTimer(ev.getStartDateInMillis());
        } else {
            setToggle(-1);
            mCounter.stopTimer();
            mCounter.setVisibility(View.GONE);
        }
    }

    private void setToggle(int toggle) {
        final Resources resources = getResources();

        Drawable drivingEvent;
        Drawable restingEvent;
        Drawable otherWorkEvent;
        Drawable poaEvent;

        drivingEvent = resources.getDrawable(toggle == Event.EVENT_TYPE_DRIVING ? R.drawable.ic_driving_large_on : R.drawable.ic_driving_large_off);
        otherWorkEvent = resources.getDrawable(toggle == Event.EVENT_TYPE_OTHER_WORK ? R.drawable.ic_other_work_large_on : R.drawable.ic_other_work_large_off);
        poaEvent= resources.getDrawable(toggle == Event.EVENT_TYPE_POA ? R.drawable.ic_poa_large_on : R.drawable.ic_poa_large_off);


        if(toggle == Event.EVENT_TYPE_DAILY_REST
                || toggle == Event.EVENT_TYPE_NORMAL_BREAK
                || toggle == Event.EVENT_TYPE_WEEKLY_REST) {
            restingEvent = resources.getDrawable(R.drawable.ic_rest_large_on);

        } else {
            restingEvent = resources.getDrawable(R.drawable.ic_rest_large_off);
        }



        setDrawableBounds(drivingEvent);
        setDrawableBounds(restingEvent);
        setDrawableBounds(otherWorkEvent);
        setDrawableBounds(poaEvent);

        mToggleDriving.setCompoundDrawables(null, drivingEvent, null, null);
        mToggleResting.setCompoundDrawables(null, restingEvent, null, null);
        mToggleOtherWork.setCompoundDrawables(null, otherWorkEvent, null, null);
        mTogglePoa.setCompoundDrawables(null, poaEvent, null, null);
    }

    private void setDrawableBounds(Drawable d) {
        if(d != null) {
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        }
    }
}
