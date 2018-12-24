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


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;

import com.velli20.tachograph.Event;

import java.util.List;
import java.util.Locale;

public class DatabaseLocationUtils {


    /* Get name of the address with given coordinates */
    public static String getAdressForLocation(Context context, double latitude, double longitude) {
        if (context == null) {
            return null;
        }

        try {

            final Geocoder geo = new Geocoder(context, Locale.getDefault());
            final List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);

            if (addresses == null || addresses.isEmpty()) {
                return null;
            } else {
                return addresses.get(0).getAddressLine(1);
            }

        } catch (Exception ignored) {
        }

        return null;
    }

    /* Returns result in meters */
    public static double calculateRouteDistance(Cursor routeCursor) {
        if (routeCursor == null || routeCursor.isClosed() || !routeCursor.moveToFirst()) {
            return 0;
        }
        double distance = 0.0;
        double latitude;
        double longitude;

        double previousLatitude = -1;
        double previousLongitude = -1;

        do {
            latitude = routeCursor.getDouble(routeCursor.getColumnIndex(DataBaseHandlerConstants.KEY_LOCATION_LATITUDE));
            longitude = routeCursor.getDouble(routeCursor.getColumnIndex(DataBaseHandlerConstants.KEY_LOCATION_LONGITUDE));

            if (previousLatitude != -1 && previousLongitude != -1 && latitude != -1 && longitude != -1) {
                /* Calculate distance between two points */
                distance += calculateDistance(previousLatitude, previousLongitude, latitude, longitude);
            }

            previousLatitude = latitude;
            previousLongitude = longitude;


        } while (routeCursor.moveToNext());

        return distance;
    }

    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    /* Checks if there is logged locations with given Event mId. */
    public static boolean checkForLoggedRoute(SQLiteDatabase database, Event event) {
        if (event == null || database == null || !database.isOpen()) {
            return false;
        }
        String query = new DatabaseLocationQueryBuilder()
                .fromTable(DataBaseHandlerConstants.TABLE_LOCATIONS)
                .selectCountOfAllColumns()
                .whereLocationEventIdIs(event.getRowId())
                .setMaxResults(3)
                .buildQuery();

        final Cursor locCursor = database.rawQuery(query, null);

        boolean result = false;

        if (locCursor != null) {
            result = locCursor.moveToFirst() && locCursor.getInt(0) > 1;
            locCursor.close();
        }

        return result;
    }

    /* Returns result in meters */
    public static double getLoggedRouteDistance(SQLiteDatabase database, int eventId) {
        if (database == null || !database.isOpen()) {
            return 0;
        }

        String routeQuery = new DatabaseLocationQueryBuilder()
                .fromTable(DataBaseHandlerConstants.TABLE_LOCATIONS)
                .selectAllColumns()
                .whereLocationEventIdIs(eventId)
                .orderByKey(DataBaseHandlerConstants.KEY_LOCATION_TIME, true)
                .buildQuery();

        Cursor routeCursor = database.rawQuery(routeQuery, null);


        double distance = DatabaseLocationUtils.calculateRouteDistance(routeCursor);

        if (routeCursor != null) {
            routeCursor.close();
        }

        return distance;
    }


}
