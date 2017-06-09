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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import com.velli20.tachograph.R;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class ChooseValueView extends RelativeLayout implements OnClickListener {
	private static final int BUTTON_ERASE = -2;
	
	public static final String UNIT_MILEAGE_KM = " km";
	public static final String UNIT_MILEAGE_MILES = " miles";

	private Button mButtons[] = new Button[10];

	private TextView mTextValue;

	private Vibrator mVibrator;

	private int mResources[] = { R.id.button10, R.id.button1, R.id.button2,
			R.id.button3, R.id.button4, R.id.button5, R.id.button6,
			R.id.button7, R.id.button8, R.id.button9 };

	private long mLastVibrate;

	private String mUnit = " km";
	private String mMileAgeValue = "0";

	public ChooseValueView(Context context) {
		super(context);
	}
	
	public ChooseValueView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ChooseValueView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public ChooseValueView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	
	@Override
	public void onFinishInflate(){
		super.onFinishInflate();
	    mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

	    mTextValue = (TextView)findViewById(R.id.choose_value_dialog_current_value);
		mTextValue.setText(formatValueMileage(Integer.valueOf(mMileAgeValue)) + mUnit);

		for(int i = 0; i <= 9; i++){
			mButtons[i] = (Button)findViewById(mResources[i]);
			mButtons[i].setOnClickListener(this);
			mButtons[i].setText(String.valueOf(i));
			mButtons[i].setTag(i);
		}
		ImageButton mErase = (ImageButton) findViewById(R.id.choose_value_dialog_button_erase);
		mErase.setOnClickListener(this);
		mErase.setTag(BUTTON_ERASE);
		
	}

	@Override
	public void onClick(View v) {
		tryVibrate();
		
		Integer val = (Integer)v.getTag();
		
		if(val != null){
			setMileageValue(val);
		} 

	}
	
	public int getValue(){
		return Integer.valueOf(mMileAgeValue);
	}
	
	public String getUnit(){
		return mUnit;
	}
	
	public void setValue(int value){
		mMileAgeValue = String.valueOf(value);
		if(mTextValue != null){
			mTextValue.setText(formatValueMileage(Integer.valueOf(mMileAgeValue)) + mUnit);
		}
	}
	
	private String formatValueMileage(int value){
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
		symbols.setGroupingSeparator(' ');

		DecimalFormat formatter = new DecimalFormat("###,###.##", symbols);
		return formatter.format(value);
	}
	
	
	private void setMileageValue(int addValue){
		int currentvalue = Integer.valueOf(mMileAgeValue);
		
		if(currentvalue == 0 && addValue == 0 || mMileAgeValue.length() == 9 && addValue != -2){
			return;
		} else if(addValue != -1){
			
			if(currentvalue == 0) {
				mMileAgeValue = String.valueOf(addValue == - 2 ? 0 : addValue);
				mTextValue.setText(mMileAgeValue + mUnit); 
			} else if(addValue == -2){
				if(mMileAgeValue.length() > 1){
					mMileAgeValue = mMileAgeValue.substring(0, mMileAgeValue.length() -1);
					mTextValue.setText(formatValueMileage(Integer.valueOf(mMileAgeValue)) + mUnit);
				} else if(mMileAgeValue.length() == 1){
					mMileAgeValue = "0";
					mTextValue.setText(mMileAgeValue + mUnit);
				} 
			} else {
				mMileAgeValue += String.valueOf(addValue);
				
				mTextValue.setText(formatValueMileage(Integer.valueOf(mMileAgeValue)) + mUnit); 
				
			}
		}
	}
	
    private void tryVibrate() {
        if (mVibrator != null) {
            long now = SystemClock.uptimeMillis();
            // We want to try to vibrate each individual tick discretely.
            if (now - mLastVibrate >= 125) {
                mVibrator.vibrate(5);
                mLastVibrate = now;
            }
        }
    }

}
