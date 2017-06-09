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

package com.velli20.tachograph.database;

public class DataBaseHandlerConstants {
	public static final String TABLE_EVENTS = "events";
    public static final String TABLE_LOCATIONS = "locationpoints";

	public static final String KEY_ID = "rowid";
    public static final String KEY_EVENT_TYPE = "type";
    public static final String KEY_EVENT_START_HOUR= "starthour";
    public static final String KEY_EVENT_START_MINUTE= "startminute";
    public static final String KEY_EVENT_END_HOUR= "endhour";
    public static final String KEY_EVENT_END_MINUTE= "endminute";
    public static final String KEY_EVENT_START_DATE= "startdate";
    public static final String KEY_EVENT_END_DATE= "enddate";
    public static final String KEY_EVENT_START_DATE_IN_MILLIS= "startdateinmillis";
    public static final String KEY_EVENT_END_DATE_IN_MILLIS= "enddateinmillis";
    public static final String KEY_EVENT_NAME = "name";
    public static final String KEY_EVENT_MILEAGE_START= "milagestart";
    public static final String KEY_EVENT_MILEAGE_END = "milageend";
    public static final String KEY_EVENT_MILEAGE_UNIT = "milageunit";
    public static final String KEY_EVENT_RECORDING = "recording";
    public static final String KEY_EVENT_NOTE = "note";
    public static final String KEY_EVENT_START_LOCATION = "startlocation";
    public static final String KEY_EVENT_END_LOCATION = "endlocation";
    public static final String KEY_EVENT_IS_SPLIT_BREAK = "issplitbreak";
    
    public static final String KEY_LOCATION_LATITUDE = "latitude";
    public static final String KEY_LOCATION_LONGITUDE = "longitude";
    public static final String KEY_LOCATION_TIME = "time";
    public static final String KEY_LOCATION_SPEED = "speed";
    public static final String KEY_LOCATION_EVENT = "event";
    public static final String KEY_LOCATION_EVENT_ID = "eventid";

	public static final int DATABASE_ACTION_EDIT = 0;
	public static final int DATABASE_ACTION_INSERT = 1;
	public static final int DATABASE_ACTION_DELETE = 2;
    public static final int DATABASE_ACTION_DELETE_MULTIPLE = 3;
    public static final int DATABASE_ACTION_DELETE_ALL_DATA = 4;
	public static final int DATABASE_ACTION_ERROR = 5;
    

	
	public static final String columnsSelection =
		KEY_EVENT_TYPE + ", " +
	    KEY_EVENT_START_HOUR + ", " + 
	    KEY_EVENT_START_MINUTE + ", " + 
	    KEY_EVENT_END_HOUR + ", "+
        KEY_EVENT_END_MINUTE + ", " + 
	    KEY_EVENT_START_DATE + ", " + 
        KEY_EVENT_END_DATE + ", " + 
	    KEY_EVENT_START_DATE_IN_MILLIS + ", " + 
        KEY_EVENT_END_DATE_IN_MILLIS + ", " + 
	    KEY_EVENT_NAME + ", " + 
        KEY_EVENT_MILEAGE_START + ", " + 
        KEY_EVENT_MILEAGE_END + ", " + 
        KEY_EVENT_MILEAGE_UNIT + ", " + 
        KEY_EVENT_RECORDING + ", "+ 
        KEY_EVENT_NOTE + ", " + 
        KEY_EVENT_START_LOCATION + ", " +
        KEY_EVENT_END_LOCATION + ", " +
        KEY_EVENT_IS_SPLIT_BREAK + ", " +
        KEY_ID;

	
	public static final String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "(" + 
		KEY_EVENT_TYPE + " INTEGER," + 
		KEY_EVENT_START_HOUR + " INTEGER, " + 
		KEY_EVENT_START_MINUTE + " INTEGER, " + 
		KEY_EVENT_END_HOUR + " INTEGER," + 
		KEY_EVENT_END_MINUTE + " INTEGER, " + 
		KEY_EVENT_START_DATE + " TEXT, " + 
		KEY_EVENT_END_DATE + " TEXT, "+ 
		KEY_EVENT_START_DATE_IN_MILLIS + " TEXT, " + 
		KEY_EVENT_END_DATE_IN_MILLIS + " TEXT," + 
		KEY_EVENT_NAME + " TEXT," + 
		KEY_EVENT_MILEAGE_START + " INTEGER," + 
		KEY_EVENT_MILEAGE_END + " INTEGER," + 
		KEY_EVENT_MILEAGE_UNIT + " TEXT," + 
		KEY_EVENT_RECORDING + " INTEGER, " + 
		KEY_EVENT_NOTE + " TEXT," +
		KEY_EVENT_START_LOCATION + " TEXT," +
		KEY_EVENT_END_LOCATION + " TEXT," +
		KEY_EVENT_IS_SPLIT_BREAK + " INTEGER" +")";
	
	public static final String CREATE_LOCATION_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + "(" + 
	    KEY_LOCATION_LATITUDE + " REAL, " + 
		KEY_LOCATION_LONGITUDE + " REAL, " + 
	    KEY_LOCATION_EVENT + " INTEGER, "+ 
		KEY_LOCATION_SPEED + " REAL, " + 
	    KEY_LOCATION_TIME + " TEXT, " +
		KEY_LOCATION_EVENT_ID + " INTEGER" + ")";

	
	    
}
