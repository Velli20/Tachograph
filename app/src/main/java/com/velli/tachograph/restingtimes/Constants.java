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

package com.velli.tachograph.restingtimes;

public class Constants {
	public static final long THIRTEEN_HOURS_PERIOD_IN_MILLIS = 46800000;
	public static final long FIVTEEN_HOURS_PERIOD_IN_MILLIS = 54000000;
	public static final long SIX_DAYS_PERIOD_IN_MILLIS = 518400000;
	public static final long SEVEN_DAYS_PERIOD_IN_MILLIS = 604800000;
	public static final long ONE_DAY_PERIOD_IN_MILLIS = 86400000;

	public static final int ONE_DAY_PERIOD_IN_MINS = 1440;
    
	public static final int LIMIT_WEEKLY_REST_MIN= 2700;
	public static final int LIMIT_WEEKLY_REST_REDUCED_MIN = 1440;
	public static final int LIMIT_WEEKLY_DRIVE = 3360;
	public static final int LIMIT_WTD_WEEKLY_WORK_AVERAGE_MAX = 2880;
	public static final int LIMIT_WTD_WEEKLY_WORK = 3600;
	public static final int LIMIT_WTD_WEEKLY_RESTING = 1440;
	public static final int LIMIT_WTD_DAILY_RESTING = 660;
	public static final int LIMIT_WTD_NIGHT_SHIFT_WORKING = 600;
	
	public static final int LIMIT_DAILY_WORK_MIN = 780;
	public static final int LIMIT_DAILY_WORK_EXTENDED_MIN = 900;
	public static final int LIMIT_DAILY_DRIVE_MIN = 540;
	public static final int LIMIT_DAILY_DRIVE_EXTENDED_MIN = 600;
	
	public static final int LIMIT_DAILY_REST_SPLIT_1 = 180;
	public static final int LIMIT_DAILY_REST_SPLIT_2 = 660;
	public static final int LIMIT_DAILY_REST_MIN = 660;
	public static final int LIMIT_DAILY_REST_REDUCED_MIN = 540;
	public static final int LIMIT_FORTNNIGHTLY_DRIVE = 5400;
	
	public static final int LIMIT_CONTINIOUS_DRIVING = 270;
	public static final int LIMIT_BREAK = 45;
	public static final int LIMIT_BREAK_SPLIT_1 = 15;
	public static final int LIMIT_BREAK_SPLIT_2 = 30;
}
