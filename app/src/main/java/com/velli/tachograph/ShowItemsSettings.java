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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import com.velli.tachograph.collections.ListItemToShow;
import com.velli.tachograph.views.ListItemWorkLimit;

import android.content.Context;
import android.content.SharedPreferences;

public class ShowItemsSettings {
	public static final String KEY = "showItems";
	public static final String KEY_SETTINGS_SHOW_FORTNIGHTLY_DRV_TIME = "showFortnightlyDriveTime";
    public static final String KEY_SETTINGS_SHOW_WEEKLY_REST = "showWeeklyRest";
    public static final String KEY_SETTINGS_SHOW_DAILY_REST = "showDailyRest";
    public static final String KEY_SETTINGS_SHOW_DAILY_DRV_TIME= "showDailyDrivetime";
    public static final String KEY_SETTINGS_SHOW_WEEKLY_DRV_TIME = "showWeeklyDriveTime";
    public static final String KEY_SETTINGS_SHOW_BREAK_TIME = "showBreakTime";
    public static final String KEY_SETTINGS_SHOW_CONTINUOS_DRV_TIME = "showContiniousDrivingTime";
    public static final String KEY_SETTINGS_SHOW_WTD_WEEKLY_WORK_TIME = "showWtdWeeklyWorkTime";
    public static final String KEY_SETTINGS_SHOW_WTD_DAILY_REST = "showWtdDailyRest";
    public static final String KEY_SETTINGS_SHOW_WTD_WEEKLY_REST = "showWtdWeeklyRest";
    public static final String KEY_SETTINGS_SHOW_WTD_NIGHT_SHIFT= "showWtdNightShift";
    public static final String KEY_SETTINGS_POSITION = "Position";
    
	public static final int ITEM_FORTNIGHTLY_DRIVING_TIME = 0;
	public static final int ITEM_WEEKLY_RESTING = 1;
	public static final int ITEM_DAILY_REST = 2;
	public static final int ITEM_DAILY_DRIVING_TIME = 3;
	public static final int ITEM_WEEKLY_DRIVING_TIME = 4;
	public static final int ITEM_WTD_WEEKLY_WORK_TIME = 5;
	public static final int ITEM_WTD_DAILY_REST = 6;
	public static final int ITEM_WTD_WEEKLY_REST = 7;
	public static final int ITEM_WTD_NIGHT_SHIFT = 8;

	
	
	public static void setShowItem(Context c, int item, boolean show){
		SharedPreferences prefs = c.getSharedPreferences(ShowItemsSettings.KEY, Context.MODE_PRIVATE);
		switch(item){
		
		case ITEM_DAILY_DRIVING_TIME:
			prefs.edit().putBoolean(KEY_SETTINGS_SHOW_DAILY_DRV_TIME, show).apply();
			break;
		case ITEM_DAILY_REST:
			prefs.edit().putBoolean(KEY_SETTINGS_SHOW_DAILY_REST, show).apply();
			break;
		case ITEM_FORTNIGHTLY_DRIVING_TIME:
			prefs.edit().putBoolean(KEY_SETTINGS_SHOW_FORTNIGHTLY_DRV_TIME, show).apply();
			break;
		case ITEM_WEEKLY_RESTING:
			prefs.edit().putBoolean(KEY_SETTINGS_SHOW_WEEKLY_REST, show).apply();
			break;
		case ITEM_WEEKLY_DRIVING_TIME:
			prefs.edit().putBoolean(KEY_SETTINGS_SHOW_WEEKLY_DRV_TIME, show).apply();
			break;
		case ITEM_WTD_DAILY_REST:
			prefs.edit().putBoolean(KEY_SETTINGS_SHOW_WTD_DAILY_REST, show).apply();
			break;
		case ITEM_WTD_WEEKLY_REST:
			prefs.edit().putBoolean(KEY_SETTINGS_SHOW_WTD_WEEKLY_REST, show).apply();
			break;
		case ITEM_WTD_WEEKLY_WORK_TIME:
			prefs.edit().putBoolean(KEY_SETTINGS_SHOW_WTD_WEEKLY_WORK_TIME, show).apply();
			break;
		case ITEM_WTD_NIGHT_SHIFT:
			prefs.edit().putBoolean(KEY_SETTINGS_SHOW_WTD_NIGHT_SHIFT, show).apply();
			break;
		}
	}
	
	public static boolean getShowItem(SharedPreferences prefs, int item){
		switch(item){
		
		case ITEM_DAILY_DRIVING_TIME:
			return prefs.getBoolean(KEY_SETTINGS_SHOW_DAILY_DRV_TIME, true);
		case ITEM_DAILY_REST:
			return prefs.getBoolean(KEY_SETTINGS_SHOW_DAILY_REST, true);
		case ITEM_FORTNIGHTLY_DRIVING_TIME:
			return prefs.getBoolean(KEY_SETTINGS_SHOW_FORTNIGHTLY_DRV_TIME, true);
		case ITEM_WEEKLY_RESTING:
			return prefs.getBoolean(KEY_SETTINGS_SHOW_WEEKLY_REST, true);
		case ITEM_WEEKLY_DRIVING_TIME:
			return prefs.getBoolean(KEY_SETTINGS_SHOW_WEEKLY_DRV_TIME, true);
		case ITEM_WTD_DAILY_REST:
			return prefs.getBoolean(KEY_SETTINGS_SHOW_WTD_DAILY_REST, false);
		case ITEM_WTD_WEEKLY_REST:
			return prefs.getBoolean(KEY_SETTINGS_SHOW_WTD_WEEKLY_REST, false);
		case ITEM_WTD_WEEKLY_WORK_TIME:
			return prefs.getBoolean(KEY_SETTINGS_SHOW_WTD_WEEKLY_WORK_TIME, true);
		case ITEM_WTD_NIGHT_SHIFT:
			return prefs.getBoolean(KEY_SETTINGS_SHOW_WTD_NIGHT_SHIFT, false);
		}
		
		return false;
	}
	
	public static void setItemPosition(SharedPreferences prefs, int item, int newPosition){
		switch(item){
		
		case ITEM_DAILY_DRIVING_TIME:
			prefs.edit().putInt(KEY_SETTINGS_SHOW_DAILY_DRV_TIME + KEY_SETTINGS_POSITION, newPosition).apply();
			break;
		case ITEM_DAILY_REST:
			prefs.edit().putInt(KEY_SETTINGS_SHOW_DAILY_REST + KEY_SETTINGS_POSITION, newPosition).apply();
			break;
		case ITEM_FORTNIGHTLY_DRIVING_TIME:
			prefs.edit().putInt(KEY_SETTINGS_SHOW_FORTNIGHTLY_DRV_TIME + KEY_SETTINGS_POSITION, newPosition).apply();
			break;
		case ITEM_WEEKLY_RESTING:
			prefs.edit().putInt(KEY_SETTINGS_SHOW_WEEKLY_REST + KEY_SETTINGS_POSITION, newPosition).apply();
			break;
		case ITEM_WEEKLY_DRIVING_TIME:
			prefs.edit().putInt(KEY_SETTINGS_SHOW_WEEKLY_DRV_TIME + KEY_SETTINGS_POSITION, newPosition).apply();
			break;
		case ITEM_WTD_DAILY_REST:
			prefs.edit().putInt(KEY_SETTINGS_SHOW_WTD_DAILY_REST + KEY_SETTINGS_POSITION, newPosition).apply();
			break;
		case ITEM_WTD_WEEKLY_REST:
			prefs.edit().putInt(KEY_SETTINGS_SHOW_WTD_WEEKLY_REST + KEY_SETTINGS_POSITION, newPosition).apply();
			break;
		case ITEM_WTD_WEEKLY_WORK_TIME:
			prefs.edit().putInt(KEY_SETTINGS_SHOW_WTD_WEEKLY_WORK_TIME + KEY_SETTINGS_POSITION, newPosition).apply();
			break;
		case ITEM_WTD_NIGHT_SHIFT:
			prefs.edit().putInt(KEY_SETTINGS_SHOW_WTD_NIGHT_SHIFT + KEY_SETTINGS_POSITION, newPosition).apply();
			break;
		}
	}
	
	public static int getItemPosition(SharedPreferences prefs, int item){
		switch(item){
		
		case ITEM_DAILY_DRIVING_TIME:
			return prefs.getInt(KEY_SETTINGS_SHOW_DAILY_DRV_TIME + KEY_SETTINGS_POSITION, 0);
		case ITEM_DAILY_REST:
			return prefs.getInt(KEY_SETTINGS_SHOW_DAILY_REST + KEY_SETTINGS_POSITION, 1);
		case ITEM_FORTNIGHTLY_DRIVING_TIME:
			return prefs.getInt(KEY_SETTINGS_SHOW_FORTNIGHTLY_DRV_TIME + KEY_SETTINGS_POSITION, 2);
		case ITEM_WEEKLY_RESTING:
			return prefs.getInt(KEY_SETTINGS_SHOW_WEEKLY_REST + KEY_SETTINGS_POSITION, 3);
		case ITEM_WEEKLY_DRIVING_TIME:
			return prefs.getInt(KEY_SETTINGS_SHOW_WEEKLY_DRV_TIME + KEY_SETTINGS_POSITION, 4);
		case ITEM_WTD_DAILY_REST:
			return prefs.getInt(KEY_SETTINGS_SHOW_WTD_DAILY_REST + KEY_SETTINGS_POSITION, 5);
		case ITEM_WTD_WEEKLY_REST:
			return prefs.getInt(KEY_SETTINGS_SHOW_WTD_WEEKLY_REST + KEY_SETTINGS_POSITION, 6);
		case ITEM_WTD_WEEKLY_WORK_TIME:
			return prefs.getInt(KEY_SETTINGS_SHOW_WTD_WEEKLY_WORK_TIME + KEY_SETTINGS_POSITION, 7);
		case ITEM_WTD_NIGHT_SHIFT:
			return prefs.getInt(KEY_SETTINGS_SHOW_WTD_NIGHT_SHIFT + KEY_SETTINGS_POSITION, 8);
		}
		return item;
	}
	
	public static void restoreDefaults(Context c){
		SharedPreferences prefs = c.getSharedPreferences(ShowItemsSettings.KEY, Context.MODE_PRIVATE);
		prefs.edit().clear().commit();
		
		// To trigger OnSharedPreferenceChangeListener
		setShowItem(c, ITEM_DAILY_DRIVING_TIME, true);
	}
	
	/** 
	 * @return integer array where key is the item position and value is the item to show
	*/
	public static int[] getItemsToShowPosition(SharedPreferences prefs){
		int[] list = new int[9];
		
		for(int i = 0; i < 9; i++){
			int position = getItemPosition(prefs, i);
			list[position] = i;
		}
		
		return list;
	}
	
	/**
	 * @return integer array where key is the item to show and value is whether to show this item
	*/
	public static boolean[] getItemsToShow(SharedPreferences prefs){
		boolean[] list = new boolean[9];
		
		for(int i = 0; i < 9; i++){
			list[i] = getShowItem(prefs, i);
		}
		
		return list;
	}
	
	public static ArrayList<ListItemToShow> getCompleteList(Context c, Event recordingEvent){
		ArrayList<ListItemToShow> list = new ArrayList<>();
		SharedPreferences prefs = c.getSharedPreferences(ShowItemsSettings.KEY, Context.MODE_PRIVATE);
		
		list.add(new ListItemToShow(0, ListItemWorkLimit.TYPE_CHART, true));
		
		int minusIndex = 0;
		int plusIndex = 1;
		
		if(recordingEvent != null){
			if(recordingEvent.getEventType() == Event.EVENT_TYPE_DRIVING) {
				list.add(new ListItemToShow(1, ListItemWorkLimit.TYPE_CONTINIOUS_DRIVING, true));
				plusIndex++;
			} else if(recordingEvent.getEventType() == Event.EVENT_TYPE_NORMAL_BREAK
					|| recordingEvent.getEventType() == Event.EVENT_TYPE_SPLIT_BREAK) {
				list.add(new ListItemToShow(1, recordingEvent.isSplitBreak() ?
						ListItemWorkLimit.TYPE_SPLIT_BREAK : ListItemWorkLimit.TYPE_BREAK, true));
				plusIndex++;
			}
		}
		
		for(int i = 0; i < 9; i++){
			int position = getItemPosition(prefs, i);
			list.add(new ListItemToShow((plusIndex + position), i, getShowItem(prefs, i)));
			
		}
		
		Collections.sort(list, new CustomComparator());
		
		int lenght = list.size();
		int newSize = 0;
		for(int i = 0; i < lenght; i++){
			ListItemToShow item = list.get(i);
			
			if(!item.show) {
				minusIndex++;
			} else {
				newSize++;
				item.position = item.position - minusIndex;
			}
		}
		
		ListItemToShow[] finalList = new ListItemToShow[newSize];
		for(ListItemToShow item : list){
			if(item.show){
				finalList[item.position] = item;
			}
		}
		
		return new ArrayList<>(Arrays.asList(finalList));
	}
	
	public static class CustomComparator implements Comparator<ListItemToShow> {
	   
		@Override
	    public int compare(ListItemToShow o1, ListItemToShow o2) {
	        return o1.position - o2.position;
	    }
	}
}
