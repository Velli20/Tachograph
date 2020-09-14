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


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

import com.velli20.tachograph.collections.ListAdapterNavigationSpinner;
import com.velli20.tachograph.database.DataBaseHandler;

public class ActivityMain extends AppCompatActivity implements OnItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener, DataBaseHandler.OnDatabaseEditedListener {
    private static final String BUNDLE_KEY_SELECTED_NAV_ITEM = "selected nav item";
    private static final boolean DEBUG = false;

    private Toolbar mToolbar;
    private int mSelectedNavItem = -1;
    private Event mCurrentEvent; /* Currently recording Event */

    public static boolean isLocationPermissionGrated(Activity c) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int permission = ContextCompat.checkSelfPermission(c, android.Manifest.permission.ACCESS_FINE_LOCATION);

            return permission == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static void requestLocationPermission(Activity c) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            int permission = ContextCompat.checkSelfPermission(c, android.Manifest.permission.ACCESS_FINE_LOCATION);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                c.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FragmentSettings.PERMISSION_REQUEST_FINE_LOCATION);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mSelectedNavItem = savedInstanceState.getInt(BUNDLE_KEY_SELECTED_NAV_ITEM, -1);
        }

		/* Add callback for getting notified if user has changed app settings */
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        /* Get notified if there is changes on database */
        DataBaseHandler.getInstance().registerOnDatabaseEditedListener(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        View toolbarElevation = findViewById(R.id.toolbar_shadow);

        final Spinner navigationSpinner = (Spinner) findViewById(R.id.navigation_spinner);
        navigationSpinner.setAdapter(new ListAdapterNavigationSpinner(this));
        navigationSpinner.setOnItemSelectedListener(this);

        setSupportActionBar(mToolbar);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowTitleEnabled(false);
            ab.setDisplayShowHomeEnabled(false);
        }

        /* Add "elevation" on ToolBar for pre lollipop devices */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && toolbarElevation != null) {
            toolbarElevation.setVisibility(View.GONE);
        }

        getRecordingEvent();
        requestLocationPermission(this);
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }


    @Override
    public void onStop() {
        super.onStop();
        DataBaseHandler.getInstance().closeDatabase();
    }

    @Override
    public void onResume() {
        super.onResume();
        GpsRouteLoggerStatus.INSTANCE.setGpsPermissionGranted(isLocationPermissionGrated(this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        DataBaseHandler.getInstance().registerOnDatabaseEditedListener(this);

        Spinner navigationSpinner = (Spinner) findViewById(R.id.navigation_spinner);
        if (navigationSpinner != null) {
            navigationSpinner.setOnItemSelectedListener(null);
        }
    }

    /* Starts GpsBackgroundService if required. If not required and service is running, then shut service down
     * in order to save battery.
     */
    private void startOrStopBackgroundServiceIfRequired() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean useGps = prefs.getBoolean(getString(R.string.preference_key_use_gps), false);
        boolean showNotifications = prefs.getBoolean(getString(R.string.preference_key_show_notifications), true);
        boolean serviceRunning = isServiceRunning(GpsBackgroundService.class);

        int eventType = mCurrentEvent == null ? -1 : mCurrentEvent.getEventType();

        if ((!serviceRunning && showNotifications) || (!serviceRunning && useGps
                && (eventType == Event.EVENT_TYPE_DRIVING || eventType == Event.EVENT_TYPE_OTHER_WORK || eventType == Event.EVENT_TYPE_NORMAL_BREAK))) {
            Intent i = new Intent(this, GpsBackgroundService.class);
            startService(i);
        }
    }


    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /* Load currently recording Event asynchronously from database. */
    private void getRecordingEvent() {
        DataBaseHandler.getInstance().getRecordingEvent(new DataBaseHandler.OnGetEventTaskCompleted() {
            @Override
            public void onGetEvent(Event ev) {
                mCurrentEvent = ev;
                startOrStopBackgroundServiceIfRequired();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                final Intent settings = new Intent(this, ActivitySettings.class);
                startActivity(settings);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case FragmentSettings.PERMISSION_REQUEST_FINE_LOCATION:
                boolean permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                Intent i = new Intent(getApplicationContext(), GpsBackgroundService.class);
                i.putExtra(GpsBackgroundService.INTENT_KEY_LOCATION_PERMISSION_GRANTED, permissionGranted);
                startService(i);
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == mSelectedNavItem || isFinishing()) {
            return;
        }
        mSelectedNavItem = position;

        Fragment frag;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        switch (position) {
            case 0:
                frag = new FragmentNow();
                frag.setRetainInstance(true);
                ft.replace(R.id.container, frag, FragmentNow.Tag).commit();
                break;
            case 1:
                frag = new FragmentLog();
                frag.setRetainInstance(true);
                ft.replace(R.id.container, frag, FragmentLog.tag).commit();
                break;
            case 2:
                frag = new FragmentLogSummary();
                frag.setRetainInstance(true);
                ft.replace(R.id.container, frag, FragmentLogSummary.TAG).commit();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }


    /* Save state of the Fragments if activity is being recreated (i.g on screen rotation ) */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(BUNDLE_KEY_SELECTED_NAV_ITEM, mSelectedNavItem);
        if (mSelectedNavItem == 0) {
            final FragmentNow home = (FragmentNow) getSupportFragmentManager().findFragmentByTag(FragmentNow.Tag);
            if (home != null) {
                getSupportFragmentManager().putFragment(outState, FragmentNow.Tag, home);
            }
        } else if (mSelectedNavItem == 1) {
            final FragmentLog log = (FragmentLog) getSupportFragmentManager().findFragmentByTag(FragmentLog.tag);
            if (log != null) {
                getSupportFragmentManager().putFragment(outState, FragmentLog.tag, log);
            }
        } else if (mSelectedNavItem == 3) {
            final FragmentLogSummary wd = (FragmentLogSummary) getSupportFragmentManager().findFragmentByTag(FragmentLogSummary.TAG);
            if (wd != null) {
                getSupportFragmentManager().putFragment(outState, FragmentLogSummary.TAG, wd);
            }
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /* User has toggled app notification on/off. In order to mShow notification we
         * have to start background service
         */
        startOrStopBackgroundServiceIfRequired();
    }

    @Override
    public void onDatabaseEdited(int action, int rowId) {
        getRecordingEvent();
    }
}
