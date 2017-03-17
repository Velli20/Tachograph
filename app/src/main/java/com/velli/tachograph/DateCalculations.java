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

package com.velli.tachograph;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateCalculations {
	
	
	public static final String[] SHORT_MONTHS = new DateFormatSymbols().getShortMonths();
	public static final String[] MONTHS = new DateFormatSymbols().getMonths();
	
	public static String convertDatesInHours(long start, long end){
		final int mins = convertDatesToMinutes(start, end);
		return convertMinutesToTimeString(mins);
	}
	
	public static int convertDatesToMinutes(long start, long end){
		long secs = (end - start) / 1000;
		return (int) (secs / 60);
	}
	
	public static String convertMinutesToTimeString(int minutes){
		final int fullHours = minutes / 60;
		final int fullMinutes = minutes % 60;
		
		if(fullHours == 0) {
			return String.format(Locale.getDefault(), "%2d min", fullMinutes);
		} else {
			if(fullMinutes == 0){
				return String.format(Locale.getDefault(), "%1d h", fullHours);
			}
			return String.format(Locale.getDefault(), "%1d h %2d min", fullHours, fullMinutes);
		}
	}
	
	public static String createTimeString(int hour, int minute){
		return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
	}
	
	public static String createTimeString(long millis){
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(millis);
		
		return c.get(Calendar.HOUR_OF_DAY) + ":" + appendTimeString(c.get(Calendar.MINUTE));
	}


	public static String dateSummary(long start, long end){
		String summary;
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();

		startCal.setTimeInMillis(start);
		endCal.setTimeInMillis(end);

		if(startCal.get(Calendar.DAY_OF_MONTH) != endCal.get(Calendar.DAY_OF_MONTH) ||
				startCal.get(Calendar.MONTH) != endCal.get(Calendar.MONTH)){

			summary = startCal.get(Calendar.DAY_OF_MONTH) + "." +
					(startCal.get(Calendar.MONTH) + 1) + " - " + endCal.get(Calendar.DAY_OF_MONTH) +
					"." + (endCal.get(Calendar.MONTH) + 1)  + "." +
					startCal.get(Calendar.YEAR);
		} else {
			summary = startCal.get(Calendar.DAY_OF_MONTH) +"." +
					(startCal.get(Calendar.MONTH) + 1)  + "." +
					startCal.get(Calendar.YEAR);
		}
		return summary;
	}
	
	
	public static String createDateString(long date){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date);
		
		return cal.get(Calendar.DAY_OF_MONTH) +"." + 
		cal.get(Calendar.MONTH) + "." +
		cal.get(Calendar.YEAR);
	}
	
	public static long formatDateMillisAndSeconds(long timeToFormat){
		Calendar c = Calendar.getInstance();
    	c.setTimeInMillis(timeToFormat);
    	c.set(Calendar.SECOND, 0);
    	c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis(); 
	}
	
	public static String createShortDateTimeString(long date){
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date);
		
		return cal.get(Calendar.DAY_OF_MONTH) + ". " 
		+ SHORT_MONTHS[cal.get(Calendar.MONTH)] + " " +  
		+ cal.get(Calendar.HOUR_OF_DAY) + ":" + appendTimeString(cal.get(Calendar.MINUTE));
		
	}
	
	// 24.00
	public static String createShortTimeString(long date){
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date);
		
		return String.format(Locale.getDefault(), "%02d.%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
		
	}

    
    public static int getWeekOfYear(long date){
    	final Calendar cal = Calendar.getInstance();
    	cal.setTimeInMillis(date);
    	
    	return cal.get(Calendar.WEEK_OF_YEAR);
    }
    

    
    public static String createDateTimeString(long start, long end){
		return createDateTimeString(start) + " - \n" + createDateTimeString(end);
    }
    
    public static String createDateTimeString(long date){
    	final StringBuilder dateString = new StringBuilder();
    	final GregorianCalendar calStart = new GregorianCalendar(Locale.getDefault());
    	final String[] weekdays = new DateFormatSymbols().getShortWeekdays();
    	calStart.setTimeInMillis(date);
    	
    	return dateString.append(weekdays[calStart.get(GregorianCalendar.DAY_OF_WEEK)]).append(" ")
    	.append(String.valueOf(calStart.get(GregorianCalendar.DAY_OF_MONTH))).append(". ")
    	.append(MONTHS[calStart.get(GregorianCalendar.MONTH)]).append(" ")
    	.append(String.valueOf(appendTimeString(calStart.get(GregorianCalendar.HOUR_OF_DAY))))
    	.append(".").append(String.valueOf(appendTimeString(calStart.get(GregorianCalendar.MINUTE)))).toString();
    	
    }
	
	public static String appendTimeString(int time){
		String string = String.valueOf(time);
		if(string.length() == 1){
			return "0" + string;
		} else {
			return string;
		}
	}


	
	public static long getTodayStartTime(){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTimeInMillis();
	}
	
	public static long getTodayEndTime(){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 60);
		c.set(Calendar.SECOND, 60);
		return c.getTimeInMillis();
	}


	public static int getTimeDifferenceInMins(long start, long end, long minDate){
		if(start < minDate && minDate != -1){
			start = minDate;
		}
		final long secs = (end - start) / 1000;
		return (int) (secs / 60);
	}



     
     public static int getCurrentHour(long millis){
		 GregorianCalendar now = new GregorianCalendar();
 		 now.setTimeInMillis(millis);
 		 return now.get(Calendar.HOUR_OF_DAY);
     }

     
     public static int getCurrentMinute(long millis){
		 GregorianCalendar now = new GregorianCalendar();
 		 now.setTimeInMillis(millis);

 		 return now.get(Calendar.MINUTE);
     }
     
     public static String getFileDateName(){
    	 DateFormat formOut = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault());
    	 return formOut.format(System.currentTimeMillis());
     }

     
     public static long setTimeToDate(long dateInMillis, int hours, int minutes){
    	final GregorianCalendar cal = new GregorianCalendar(Locale.getDefault());
      	
      	cal.setTimeInMillis(dateInMillis);
      	cal.set(GregorianCalendar.HOUR_OF_DAY, hours);
      	cal.set(GregorianCalendar.MINUTE, minutes);
      	cal.set(GregorianCalendar.SECOND, 0);
      	cal.set(GregorianCalendar.MILLISECOND, 0);
      	return cal.getTimeInMillis();
     }
     
     public static String formatDate(Date timeToFormat, int hours, int minutes){
 		Date date = timeToFormat;
 		if(hours != -1 && minutes != -1){
         	Calendar c = Calendar.getInstance();
         	c.setTime(timeToFormat);
         	c.set(Calendar.HOUR_OF_DAY, hours);
         	c.set(Calendar.MINUTE, minutes);
         	c.set(Calendar.SECOND, 0);
         	c.set(Calendar.MILLISECOND, 0);
         	date = c.getTime();
         } 
 		
 	    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
 	}
 	
 	public static long formatDateToMillis(long timeToFormat, int hours, int minutes){
 		Calendar c = Calendar.getInstance();
     	c.setTimeInMillis(timeToFormat);
     	c.set(Calendar.HOUR_OF_DAY, hours);
     	c.set(Calendar.MINUTE, minutes);
     	//TODO
     	//c.set(Calendar.SECOND, 0);
     	//c.set(Calendar.MILLISECOND, 0);
 		return c.getTimeInMillis(); 
 	}

}
