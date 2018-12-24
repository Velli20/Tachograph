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

package com.velli20.tachograph.location;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.velli20.tachograph.DateUtils;
import com.velli20.tachograph.R;
import com.velli20.tachograph.database.DataBaseHandler;
import com.velli20.tachograph.database.DataBaseHandler.OnGetLocationsListener;

import java.util.ArrayList;

public class ActivityLoggedRouteMap extends AppCompatActivity implements OnMapReadyCallback, OnGetLocationsListener, OnMapClickListener {
    public static final String EXTRAS_LOCATION_EVENT_ID = "event mId";
    public static final String INTENT_EXTRA_START_DAY = "start day";
    public static final String INTENT_EXTRA_END_DAY = "end day";
    public static final String EXTRAS_TITLE = "title";

    public static final int MAP_TYPE_SATELLITE = 0;
    public static final int MAP_TYPE_HYBRID = 1;
    public static final int MAP_TYPE_NORMAL = 2;
    public static final int MAP_TYPE_TERRAIN = 3;

    private Toolbar mToolbar;
    private GoogleMap mMap;
    private int mColorBlue;
    private int mSelectedGroundLayer = MAP_TYPE_NORMAL;
    private float mDensity;
    private ArrayList<CustomLocation> mLocationList;
    private PolylineOptions mRoute;
    private Marker mVisibleMarker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSelectedGroundLayer = prefs.getInt(getString(R.string.preference_key_map_type), MAP_TYPE_NORMAL);

        setContentView(R.layout.activity_logged_route_map);

        mColorBlue = getResources().getColor(R.color.event_driving);
        mDensity = getResources().getDisplayMetrics().density;

        final MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.route_map);
        mapFragment.getMapAsync(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_route);

        setSupportActionBar(mToolbar);
        setMapType(mSelectedGroundLayer);

        final ActionBar ab = getSupportActionBar();

        if (ab != null) {
            ab.setDisplayShowHomeEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent() != null && getIntent().getExtras() != null) {
            final Bundle b = getIntent().getExtras();

            final int id = b.getInt(EXTRAS_LOCATION_EVENT_ID, -1);
            final String title = b.getString(EXTRAS_TITLE);
            long start = b.getLong(INTENT_EXTRA_START_DAY, -1);
            long end = b.getLong(INTENT_EXTRA_END_DAY, -1);

            if (id != -1) {
                DataBaseHandler.getInstance().getAllLocationByEventId(id, this);
            } else if (start != -1 && end != -1) {
                DataBaseHandler.getInstance().getAllLocationByTimeFrame(start, end, this);
            }

            if (title != null) {
                ab.setTitle(title);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMapClickListener(this);

        setRoute(mRoute);
        setMapType(mSelectedGroundLayer);
    }

    private void setRoute(PolylineOptions opt) {
        if (mMap != null && opt != null) {
            if (mLocationList != null && !mLocationList.isEmpty()) {
                final CustomLocation loc = mLocationList.get(0);
                final LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
                final CameraUpdate c = CameraUpdateFactory.newLatLngZoom(pos, 16);

                mMap.moveCamera(c);
                mMap.addMarker(new MarkerOptions().position(pos).title(getResources().getString(R.string.add_event_start_location)));
            }

            mMap.addPolyline(opt);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_route_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.map_layer:
                showMapLayerPicker();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

    private void showMapLayerPicker() {
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_title_map_layer)
                .items(getResources().getStringArray(R.array.map_ground_layers))
                .positiveText(R.string.action_ok)
                .negativeText(R.string.action_cancel)
                .itemsCallbackSingleChoice(mSelectedGroundLayer, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        setMapType(i);
                        return true;
                    }

                }).show();
    }

    private void setMapType(int type) {
        mSelectedGroundLayer = type;

        if (mMap == null) {
            return;
        }
        switch (type) {
            case MAP_TYPE_SATELLITE:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return;
            case MAP_TYPE_HYBRID:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return;
            case MAP_TYPE_NORMAL:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return;
            case MAP_TYPE_TERRAIN:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return;
        }
    }

    @Override
    public void onGetLocations(ArrayList<CustomLocation> locations) {
        if (locations != null && !locations.isEmpty()) {
            mLocationList = locations;
            new GetRoutePath(locations).execute();
        }

    }

    @Override
    public void onMapClick(LatLng point) {
        Log.i("", "onMapClick()");
        double lat = point.latitude;
        double lon = point.longitude;

        if (mVisibleMarker != null) {
            mVisibleMarker.remove();
            mVisibleMarker = null;
        }

        for (CustomLocation location : mLocationList) {


            float[] results = new float[1];
            Location.distanceBetween(lat, lon, location.getLatitude(), location.getLongitude(), results);

            if (results[0] < 10) {
                // If distance is less than 100 meters, this is your polyline
                mVisibleMarker = mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .snippet(getResources().getString(R.string.title_speed) + ": " + Math.round(location.getSpeed()) + " km/h \n" + DateUtils.createTimeString(location.getTime()))
                        .title(getResources().getString(R.string.title_info)));

                mVisibleMarker.showInfoWindow();
                break;
            }
        }

    }

    private class GetRoutePath extends AsyncTask<Void, Void, PolylineOptions> {
        private ArrayList<CustomLocation> mList;

        public GetRoutePath(ArrayList<CustomLocation> locationList) {
            mList = locationList;
        }

        @Override
        protected PolylineOptions doInBackground(Void... params) {
            final PolylineOptions opt = getNewLine();

            for (CustomLocation location : mList) {
                opt.add(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            return opt;
        }

        @Override
        protected void onPostExecute(PolylineOptions line) {
            if (line != null && !line.getPoints().isEmpty()) {
                if (mMap != null) {
                    setRoute(line);
                } else {
                    mRoute = line;
                }
            }
        }

        private PolylineOptions getNewLine() {
            PolylineOptions opt = new PolylineOptions();
            opt.width(mDensity * 5);
            opt.color(mColorBlue);
            opt.visible(true);

            return opt;
        }
    }


}
