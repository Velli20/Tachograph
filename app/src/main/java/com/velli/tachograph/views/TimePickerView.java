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

import java.util.GregorianCalendar;

import com.afollestad.materialdialogs.Theme;
import com.velli.tachograph.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.view.View;
import android.view.View.OnClickListener;

public class TimePickerView extends RelativeLayout implements OnClickListener {
	
	private RobotoLightTextView mTextHourTens;
	private RobotoLightTextView mTextHourOnes;
	private RobotoLightTextView mTextMinuteTens;
	private RobotoLightTextView mTextMinuteOnes;
	private RobotoLightTextView mTextHoursSeparator;
	
	private ImageButton mBackspace;
	
	private View mPositiveButton;
	private Button mNumberButton[] = {null, null, null, null, null, null, null, null, null, null};
	
	private Typeface mAndroidClockMonoThin;
	private Typeface mOriginalHoursTypeface;
	
	private int mInputPointer = -1;
	private int mInput[] = new int[4];
	
	private boolean mInflated = false;
	private GregorianCalendar c = new GregorianCalendar();

	private int mButtonResources[] = { R.id.button10, R.id.button1,
			R.id.button2, R.id.button3, R.id.button4, R.id.button5,
			R.id.button6, R.id.button7, R.id.button8, R.id.button9 };
	
	
	private long mLastVibrate;
	private Vibrator mVibrator;
	
	private ColorStateList mTextColorDark;
	private ColorStateList mTextColorLight;

    private Theme mTheme = Theme.LIGHT;
	
	public TimePickerView(Context context) {
		super(context);
		init(context);
	}
	
	public TimePickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public TimePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public TimePickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}
	
	private void init(Context context){
		mAndroidClockMonoThin = Typeface.createFromAsset(context.getAssets(), "AndroidClockMono-Thin.ttf");
		mTextColorDark = getResources().getColorStateList(R.color.dialog_timepicker_dark);
		mTextColorLight =  getResources().getColorStateList(R.color.dialog_timepicker_light);
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	public void setPositiveButton(View v){
		mPositiveButton = v;
	}
	
	@Override
	public void onFinishInflate(){
		super.onFinishInflate();
		
		mTextHourTens = (RobotoLightTextView)findViewById(R.id.time_picker_hour_tens);
		mTextHourOnes = (RobotoLightTextView)findViewById(R.id.time_picker_hour_ones);
		mTextMinuteTens = (RobotoLightTextView)findViewById(R.id.time_picker_minute_tens);
		mTextMinuteOnes = (RobotoLightTextView)findViewById(R.id.time_picker_minute_ones);
		mTextHoursSeparator = (RobotoLightTextView)findViewById(R.id.time_picker_hour_separator);
		mInflated = true;
		
		mTextMinuteTens.setTypeface(mAndroidClockMonoThin);
		
		mTextMinuteOnes.setTypeface(mAndroidClockMonoThin);
		
		mOriginalHoursTypeface = mTextHourOnes.getTypeface();
		
		mBackspace = (ImageButton)findViewById(R.id.time_picker_delete);
		mBackspace.setOnClickListener(this);
			
		restyleViews();
		
		setTheme(mTheme);
		
		if(mInput != null){
			updateNumericKeys();
			updateTime();
		} else {
			mInput = new int[4];
			setTime(-1, -1, -1, -1);
		}
	}

    public void setTheme(Theme theme) {
        mTheme = theme;
        if(!mInflated) {
            return;
        }

        mTextHourOnes.setTextColor(mTheme == Theme.DARK ?  mTextColorLight : mTextColorDark);
        mTextMinuteOnes.setTextColor(mTheme == Theme.DARK ?  mTextColorLight : mTextColorDark);
        mTextHourTens.setTextColor(mTheme == Theme.DARK ?  mTextColorLight : mTextColorDark);
        mTextMinuteTens.setTextColor(mTheme == Theme.DARK ?  mTextColorLight : mTextColorDark);
        mTextHoursSeparator.setTextColor(mTheme == Theme.DARK ?  mTextColorLight : mTextColorDark);

        for(int i = 0; i <= 9; i++){
            mNumberButton[i] = (Button)findViewById(mButtonResources[i]);
            mNumberButton[i].setOnClickListener(this);
            mNumberButton[i].setText(String.valueOf(i));
            mNumberButton[i].setTag(i);
            mNumberButton[i].setTextColor(mTheme == Theme.DARK ?  mTextColorLight : mTextColorDark);

        }

        final Drawable d = mBackspace.getDrawable();

        if(d != null) {
            d.mutate();
            d.setColorFilter(mTheme == Theme.DARK ? mTextColorLight.getDefaultColor() : mTextColorDark.getDefaultColor(), PorterDuff.Mode.SRC_IN);
            mBackspace.setImageDrawable(d);
        }
    }

	public void setTime(int hour, int minute){
		mInput = new int[4];
		
		mInputPointer = 3;
		mInput[3] = hour / 10;
		mInput[2] = hour % 10;
		mInput[1] = minute / 10;
		mInput[0] = minute % 10;
		
		if(mInflated){
			updateNumericKeys();
		    updateTime();
		}
	}
	
	public void setTimeInMillis(long millis){
		c.setTimeInMillis(millis);
		int hours = c.get(GregorianCalendar.HOUR_OF_DAY);
		int minute = c.get(GregorianCalendar.MINUTE);
		setTime(hours, minute);
	}
	
	@Override
	public void onClick(View v) {
		Integer value = (Integer)v.getTag();
		if(value != null){
			addClickedNumber(value);
		} else if(v.getId() == R.id.time_picker_delete){
			if (mInputPointer >= 0) {
				System.arraycopy(mInput, 1, mInput, 0, mInputPointer);
                mInput[mInputPointer] = 0;
                mInputPointer--;
            }
		}  
		// enable/disable numeric keys according to the numbers entered already
        updateNumericKeys();
		updateTime();
		tryVibrate();
	}
	
	public int getHours(){
		return mInput[3] * 10 + mInput[2];
	}
	
	public int getMinutes(){
		return mInput[1] * 10 + mInput[0];
	}
	
	public long getTimeInMillis(){
		c.set(GregorianCalendar.HOUR_OF_DAY, getHours());
		c.set(GregorianCalendar.MINUTE, getMinutes());
		return c.getTimeInMillis();
	}
	
	private void restyleViews() {
		if (mTextHourOnes != null) {
			mTextHourOnes.setTextColor(mTheme == Theme.DARK ?  mTextColorLight : mTextColorDark);
		}
		if (mTextMinuteOnes != null) {
			mTextMinuteOnes.setTextColor(mTheme == Theme.DARK ?  mTextColorLight : mTextColorDark);
		}
		if (mTextHourTens != null) {
			mTextHourTens.setTextColor(mTheme == Theme.DARK ?  mTextColorLight : mTextColorDark);
		}
		if (mTextMinuteTens != null) {
			mTextMinuteTens.setTextColor(mTheme == Theme.DARK ?  mTextColorLight : mTextColorDark);
		}
		if (mTextHoursSeparator != null) {
			mTextHoursSeparator.setTextColor(mTheme == Theme.DARK ?  mTextColorLight : mTextColorDark);
		}
	}
	
	private void addClickedNumber(int val) {
        if (mInputPointer < 4 - 1) {
			System.arraycopy(mInput, 0, mInput, 1, mInputPointer + 1);
            mInputPointer++;
            mInput[0] = val;
        }
    }
	
	private void setKeyRange(int maxKey) {
		
		if(mPositiveButton != null){
			mPositiveButton.setEnabled(mInputPointer >= maxKey);
		}
		
        for (int i = 0; i < mNumberButton.length; i++) {
        	mNumberButton[i].setEnabled(i <= maxKey);
        }
    }
	
	private int getEnteredTime() {
        return mInput[3] * 1000 + mInput[2] * 100 + mInput[1] * 10 + mInput[0];
    }
	
	protected void updateTime() {
        // Put "-" in digits that was not entered by passing -1
        // Hide digit by passing -2 (for highest hours digit only);

        int hours1 = -1;
        // If the user entered 2 to 9 or 13 to 15 , there is no need for a 4th digit (AM/PM mode)
        // If the user entered 3 to 9 or 24 to 25 , there is no need for a 4th digit (24 hours mode)
        if (mInputPointer > -1) {
            // Test to see if the highest digit is 2 to 9 for AM/PM or 3 to 9 for 24 hours mode
            if (mInputPointer >= 0) {
                int digit = mInput[mInputPointer];
                if (digit >= 3 && digit <= 9) {
                    hours1 = -2;
                }
            }
            // Test to see if the 2 highest digits are 13 to 15 for AM/PM or 24 to 25 for 24 hours
            // mode
            if (mInputPointer > 0 && mInputPointer < 3 && hours1 != -2) {
                int digits = mInput[mInputPointer] * 10 + mInput[mInputPointer - 1];
                if (digits >= 24 && digits <= 25) {
                    hours1 = -2;
                }
            }
            // If we have a digit show it
            if (mInputPointer == 3) {
                hours1 = mInput[3];
            }
        } else {
            hours1 = -1;
        }
        int hours2 = (mInputPointer < 2) ? -1 : mInput[2];
        int minutes1 = (mInputPointer < 1) ? -1 : mInput[1];
        int minutes2 = (mInputPointer < 0) ? -1 : mInput[0];
        setTime(hours1, hours2, minutes1, minutes2);
    }
	
	private void setTime(int hoursTensDigit, int hoursOnesDigit, int minutesTensDigit, int minutesOnesDigit) {
        if (mTextHourTens != null) {
            // Hide digit
            if (hoursTensDigit == -2) {
            	mTextHourTens.setVisibility(View.INVISIBLE);
            } else if (hoursTensDigit == -1) {
            	mTextHourTens.setText("-");
            	mTextHourTens.setTypeface(mAndroidClockMonoThin);
            	mTextHourTens.setEnabled(false);
            	mTextHourTens.setVisibility(View.VISIBLE);
            } else {
            	mTextHourTens.setText(String.format("%d", hoursTensDigit));
            	mTextHourTens.setTypeface(mOriginalHoursTypeface);
                mTextHourTens.setEnabled(true);
                mTextHourTens.setVisibility(View.VISIBLE);
            }
        }
        if (mTextHourOnes != null) {
            if (hoursOnesDigit == -1) {
            	mTextHourOnes.setText("-");
            	mTextHourOnes.setTypeface(mAndroidClockMonoThin);
            	mTextHourOnes.setEnabled(false);
            } else {
            	mTextHourOnes.setText(String.format("%d", hoursOnesDigit));
            	mTextHourOnes.setTypeface(mOriginalHoursTypeface);
            	mTextHourOnes.setEnabled(true);
            }
        }
        if (mTextMinuteTens != null) {
            if (minutesTensDigit == -1) {
            	mTextMinuteTens.setText("-");
            	mTextMinuteTens.setEnabled(false);
            } else {
            	mTextMinuteTens.setEnabled(true);
            	mTextMinuteTens.setText(String.format("%d", minutesTensDigit));
            }
        }
        if (mTextMinuteOnes != null) {
            if (minutesOnesDigit == -1) {
            	mTextMinuteOnes.setText("-");
            	mTextMinuteOnes.setEnabled(false);
            } else {
            	mTextMinuteOnes.setText(String.format("%d", minutesOnesDigit));
            	mTextMinuteOnes.setEnabled(true);
            }
        }
    }
	
	private void updateNumericKeys() {
		int time = getEnteredTime();

		
		if (mInputPointer >= 3) {
			setKeyRange(-1);
		} else if (time == 0) {
			if (mInputPointer == -1 || mInputPointer == 0 || mInputPointer == 2) {
				setKeyRange(9);
			} else if (mInputPointer == 1) {
				setKeyRange(5);
			} else {
				setKeyRange(-1);
			}
		} else if (time == 1) {
			if (mInputPointer == 0 || mInputPointer == 2) {
				setKeyRange(9);
			} else if (mInputPointer == 1) {
				setKeyRange(5);
			} else {
				setKeyRange(-1);
			}
		} else if (time == 2) {
			if (mInputPointer == 2 || mInputPointer == 1) {
				setKeyRange(9);
			} else if (mInputPointer == 0) {
				setKeyRange(3);
			} else {
				setKeyRange(-1);
			}
		} else if (time <= 5) {
			setKeyRange(9);
		} else if (time <= 9) {
			setKeyRange(5);
		} else if (time >= 10 && time <= 15) {
			setKeyRange(9);
		} else if (time >= 16 && time <= 19) {
			setKeyRange(5);
		} else if (time >= 20 && time <= 25) {
			setKeyRange(9);
		} else if (time >= 26 && time <= 29) {
			setKeyRange(-1);
		} else if (time >= 30 && time <= 35) {
			setKeyRange(9);
		} else if (time >= 36 && time <= 39) {
			setKeyRange(-1);
		} else if (time >= 40 && time <= 45) {
			setKeyRange(9);
		} else if (time >= 46 && time <= 49) {
			setKeyRange(-1);
		} else if (time >= 50 && time <= 55) {
			setKeyRange(9);
		} else if (time >= 56 && time <= 59) {
			setKeyRange(-1);
		} else if (time >= 60 && time <= 65) {
			setKeyRange(9);
		} else if (time >= 70 && time <= 75) {
			setKeyRange(9);
		} else if (time >= 80 && time <= 85) {
			setKeyRange(9);
		} else if (time >= 90 && time <= 95) {
			setKeyRange(9);
		} else if (time >= 100 && time <= 105) {
			setKeyRange(9);
		} else if (time >= 106 && time <= 109) {
			setKeyRange(-1);
		} else if (time >= 110 && time <= 115) {
			setKeyRange(9);
		} else if (time >= 116 && time <= 119) {
			setKeyRange(-1);
		} else if (time >= 120 && time <= 125) {
			setKeyRange(9);
		} else if (time >= 126 && time <= 129) {
			setKeyRange(-1);
		} else if (time >= 130 && time <= 135) {
			setKeyRange(9);
		} else if (time >= 136 && time <= 139) {
			setKeyRange(-1);
		} else if (time >= 140 && time <= 145) {
			setKeyRange(9);
		} else if (time >= 146 && time <= 149) {
			setKeyRange(-1);
		} else if (time >= 150 && time <= 155) {
			setKeyRange(9);
		} else if (time >= 156 && time <= 159) {
			setKeyRange(-1);
		} else if (time >= 160 && time <= 165) {
			setKeyRange(9);
		} else if (time >= 166 && time <= 169) {
			setKeyRange(-1);
		} else if (time >= 170 && time <= 175) {
			setKeyRange(9);
		} else if (time >= 176 && time <= 179) {
			setKeyRange(-1);
		} else if (time >= 180 && time <= 185) {
			setKeyRange(9);
		} else if (time >= 186 && time <= 189) {
			setKeyRange(-1);
		} else if (time >= 190 && time <= 195) {
			setKeyRange(9);
		} else if (time >= 196 && time <= 199) {
			setKeyRange(-1);
		} else if (time >= 200 && time <= 205) {
			setKeyRange(9);
		} else if (time >= 206 && time <= 209) {
			setKeyRange(-1);
		} else if (time >= 210 && time <= 215) {
			setKeyRange(9);
		} else if (time >= 216 && time <= 219) {
			setKeyRange(-1);
		} else if (time >= 220 && time <= 225) {
			setKeyRange(9);
		} else if (time >= 226 && time <= 229) {
			setKeyRange(-1);
		} else if (time >= 230 && time <= 235) {
			setKeyRange(9);
		} else if (time >= 236) {
			setKeyRange(-1);
		}

	}
	
	public void tryVibrate() {
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
