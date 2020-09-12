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

package com.velli20.tachograph;

import android.content.Context;
import android.content.SharedPreferences;

import com.velli20.tachograph.collections.ListAdapterFragmentNow;
import com.velli20.tachograph.collections.ListItemRegulationToShow;
import com.velli20.tachograph.views.ListItemRegulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class CardsToShowInFragmentNow {
    public static final String KEY = "preference_key_show_items";
    private static final String KEY_SETTINGS_SHOW_FORTNIGHTLY_DRV_TIME = "preference_key_show_fortnightly_driving_time";
    private static final String KEY_SETTINGS_SHOW_WEEKLY_REST = "preference_key_show_weekly_rest";
    private static final String KEY_SETTINGS_SHOW_DAILY_REST = "preference_key_show_daily_rest";
    private static final String KEY_SETTINGS_SHOW_DAILY_DRV_TIME = "preference_key_show_daily_driving_time";
    private static final String KEY_SETTINGS_SHOW_WEEKLY_DRV_TIME = "preference_key_show_weekly_driving_time";
    private static final String KEY_SETTINGS_SHOW_WTD_WEEKLY_WORK_TIME = "preference_key_show_wtd_weekly_working_time";
    private static final String KEY_SETTINGS_POSITION = "preference_key_position";
    private static final String KEY_SETTINGS_SHOW_GPS_LOGGER_STATUS = "preference_key_show_gps_logger_status";

    private static final int ITEM_GPS_LOGGER_STATUS = 0;
    private static final int ITEM_DAILY_DRIVING_TIME = 1;
    private static final int ITEM_DAILY_REST = 2;
    private static final int ITEM_FORTNIGHTLY_DRIVING_TIME = 3;
    private static final int ITEM_WEEKLY_RESTING = 4;
    private static final int ITEM_WEEKLY_DRIVING_TIME = 5;
    private static final int ITEM_WTD_WEEKLY_WORK_TIME = 6;


    public static void setShowItem(Context c, int item, boolean show) {
        SharedPreferences prefs = c.getSharedPreferences(CardsToShowInFragmentNow.KEY, Context.MODE_PRIVATE);
        switch (item) {

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
            case ITEM_WTD_WEEKLY_WORK_TIME:
                prefs.edit().putBoolean(KEY_SETTINGS_SHOW_WTD_WEEKLY_WORK_TIME, show).apply();
                break;
            case ITEM_GPS_LOGGER_STATUS:
                prefs.edit().putBoolean(KEY_SETTINGS_SHOW_GPS_LOGGER_STATUS, show).apply();
                break;
        }
    }

    private static boolean getShowItem(SharedPreferences prefs, int item) {
        switch (item) {

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
            case ITEM_WTD_WEEKLY_WORK_TIME:
                return prefs.getBoolean(KEY_SETTINGS_SHOW_WTD_WEEKLY_WORK_TIME, true);
            case ITEM_GPS_LOGGER_STATUS:
                return prefs.getBoolean(KEY_SETTINGS_SHOW_GPS_LOGGER_STATUS, true);
        }

        return false;
    }

    public static void setItemPosition(SharedPreferences prefs, int item, int newPosition) {
        switch (item) {

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
            case ITEM_WTD_WEEKLY_WORK_TIME:
                prefs.edit().putInt(KEY_SETTINGS_SHOW_WTD_WEEKLY_WORK_TIME + KEY_SETTINGS_POSITION, newPosition).apply();
                break;
            case ITEM_GPS_LOGGER_STATUS:
                prefs.edit().putInt(KEY_SETTINGS_SHOW_GPS_LOGGER_STATUS + KEY_SETTINGS_POSITION, newPosition).apply();
                break;
        }
    }

    private static int getItemPosition(SharedPreferences prefs, int item) {
        switch (item) {

            case ITEM_DAILY_DRIVING_TIME:
                return prefs.getInt(KEY_SETTINGS_SHOW_DAILY_DRV_TIME + KEY_SETTINGS_POSITION, ITEM_DAILY_DRIVING_TIME);
            case ITEM_DAILY_REST:
                return prefs.getInt(KEY_SETTINGS_SHOW_DAILY_REST + KEY_SETTINGS_POSITION, ITEM_DAILY_REST);
            case ITEM_FORTNIGHTLY_DRIVING_TIME:
                return prefs.getInt(KEY_SETTINGS_SHOW_FORTNIGHTLY_DRV_TIME + KEY_SETTINGS_POSITION, ITEM_FORTNIGHTLY_DRIVING_TIME);
            case ITEM_WEEKLY_RESTING:
                return prefs.getInt(KEY_SETTINGS_SHOW_WEEKLY_REST + KEY_SETTINGS_POSITION, ITEM_WEEKLY_RESTING);
            case ITEM_WEEKLY_DRIVING_TIME:
                return prefs.getInt(KEY_SETTINGS_SHOW_WEEKLY_DRV_TIME + KEY_SETTINGS_POSITION, ITEM_WEEKLY_DRIVING_TIME);
            case ITEM_WTD_WEEKLY_WORK_TIME:
                return prefs.getInt(KEY_SETTINGS_SHOW_WTD_WEEKLY_WORK_TIME + KEY_SETTINGS_POSITION, ITEM_WTD_WEEKLY_WORK_TIME);
            case ITEM_GPS_LOGGER_STATUS:
                return prefs.getInt(KEY_SETTINGS_SHOW_GPS_LOGGER_STATUS + KEY_SETTINGS_POSITION, ITEM_GPS_LOGGER_STATUS);
        }
        return item;
    }

    public static void restoreDefaults(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(CardsToShowInFragmentNow.KEY, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        // To trigger OnSharedPreferenceChangeListener
        setShowItem(c, ITEM_DAILY_DRIVING_TIME, true);
    }


    public static int[] getItemsToShowPosition(SharedPreferences prefs) {
        int[] list = new int[7];

        for (int i = 0; i < 7; i++) {
            int position = getItemPosition(prefs, i);
            list[position] = i;
        }

        return list;
    }


    public static boolean[] getItemsToShow(SharedPreferences prefs) {
        boolean[] list = new boolean[7];

        for (int i = 0; i < 7; i++) {
            list[i] = getShowItem(prefs, i);
        }

        return list;
    }

    public static ArrayList<ListItemRegulationToShow> getCompleteList(Context c, Event recordingEvent) {
        int minusIndex = 0; // Position delta for cards that are not visible
        int plusIndex = 0;  // Position delta for cards that are fixed on top of the list
        int id = 3;         // Stable mId for RecyclerView

        boolean showGpsMonitor = false;

        ArrayList<ListItemRegulationToShow> list = new ArrayList<>();
        SharedPreferences prefs = c.getSharedPreferences(CardsToShowInFragmentNow.KEY, Context.MODE_PRIVATE);

        list.add(new ListItemRegulationToShow(plusIndex, ListAdapterFragmentNow.VIEW_TYPE_ACTIVITY_CHOOSER, true, 0));
        plusIndex++;

        if (recordingEvent != null) {

            if (recordingEvent.getEventType() == Event.EVENT_TYPE_DRIVING) {
                list.add(new ListItemRegulationToShow(plusIndex, ListAdapterFragmentNow.VIEW_TYPE_REGULATION, ListItemRegulation.TYPE_CONTINUOUS_DRIVING, true, 1));
                plusIndex++;

            }
            if (recordingEvent.getEventType() == Event.EVENT_TYPE_NORMAL_BREAK) {
                list.add(new ListItemRegulationToShow(plusIndex, ListAdapterFragmentNow.VIEW_TYPE_REGULATION, ListItemRegulation.TYPE_BREAK, true, 2));
                plusIndex++;
            }

            showGpsMonitor = (recordingEvent.getEventType() == Event.EVENT_TYPE_DRIVING || recordingEvent.getEventType() == Event.EVENT_TYPE_OTHER_WORK || recordingEvent.getEventType() == Event.EVENT_TYPE_NORMAL_BREAK);
        }


        for (int i = 0; i < 7; i++) {
            int position = getItemPosition(prefs, i);
            boolean show = getShowItem(prefs, i);
            if (i == 0) {
                list.add(new ListItemRegulationToShow((plusIndex + position), ListAdapterFragmentNow.VIEW_TYPE_GPS_STATUS, showGpsMonitor && show, id));

            } else {
                list.add(new ListItemRegulationToShow((plusIndex + position), ListAdapterFragmentNow.VIEW_TYPE_REGULATION, i - 1, show, id));
            }
            id++;
        }

        Collections.sort(list, new PositionComparator());

        int length = list.size();
        int newSize = 0;
        for (int i = 0; i < length; i++) {
            ListItemRegulationToShow item = list.get(i);

            if (!item.mShow) {
                minusIndex++;
            } else {
                newSize++;
                item.mPosition = item.mPosition - minusIndex;
            }
        }

        ListItemRegulationToShow[] finalList = new ListItemRegulationToShow[newSize];
        for (ListItemRegulationToShow item : list) {
            if (item.mShow) {
                finalList[item.mPosition] = item;
            }
        }

        return new ArrayList<>(Arrays.asList(finalList));
    }

    private static class PositionComparator implements Comparator<ListItemRegulationToShow> {

        @Override
        public int compare(ListItemRegulationToShow o1, ListItemRegulationToShow o2) {
            return o1.mPosition - o2.mPosition;
        }
    }
}
