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





import com.google.android.gms.location.DetectedActivity;
import com.velli.tachograph.database.DataBaseHandler;
import com.velli.tachograph.database.DataBaseHandler.OnGetEventTaskCompleted;
import com.velli.tachograph.location.LocationHistoryFragment;
import com.velli.tachograph.restingtimes.RegulationTimesSummary;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

public class ActivityMain extends AppCompatActivity implements OnItemSelectedListener{
	private static final String BUNDLE_KEY_SELECTED_NAV_ITEM = "seleced nav item";
	private static final boolean DEBUG = false;

	private Toolbar mToolbar;
    private int mSelectedNavItem = -1;
   
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if(savedInstanceState != null){
			mSelectedNavItem = savedInstanceState.getInt(BUNDLE_KEY_SELECTED_NAV_ITEM, -1);
		}
		
		mToolbar = (Toolbar)findViewById(R.id.toolbar);
        View mToolbarShadow = findViewById(R.id.toolbar_shadow);

		final Spinner mNavSpinner = (Spinner) findViewById(R.id.navigation_spinner);
		mNavSpinner.setAdapter(new NavigationSpinnerAdapter(this));
		mNavSpinner.setOnItemSelectedListener(this);
		
		setSupportActionBar(mToolbar);
		
		final ActionBar ab = getSupportActionBar();
	    ab.setDisplayShowTitleEnabled(false);
	    ab.setDisplayShowHomeEnabled(false);

	    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
	    	mToolbarShadow.setVisibility(View.GONE);
	    }
		if(!isServiceRunning(BackgroundService.class)) {
			Intent i = new Intent(this, BackgroundService.class);
			startService(i);
		}
	}
	

	
	public Toolbar getToolbar(){
		return mToolbar;
	}
	

	@Override
	public void onStop(){
		super.onStop();
		DataBaseHandler.getInstance().closeDatabase();
	}

    @Override
    public void onResume() {
        super.onResume();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean showAutomaticallyCalculatedRestingEvents = prefs.getBoolean(getString(R.string.preference_key_count_daily_and_weekly_rest_automatically), false);

        RegulationTimesSummary.getInstance().setIncludeAutomaticallyCalculatedRestingEvents(showAutomaticallyCalculatedRestingEvents);
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
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.menu_activity_main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_settings:
			final Intent settings = new Intent(this, ActivitySettings.class);
			startActivity(settings);
    		return true;

		}
		
		return super.onOptionsItemSelected(item);
	}
	
	
	public static void startRecordingEvent(final int event, final Context c){
		final long millis = System.currentTimeMillis();
		
		DataBaseHandler.getInstance().getRecordingEvent(new OnGetEventTaskCompleted() {
			
			@Override
			public void onGetEvent(Event ev) {
				
				if(ev != null && ev.isRecordingEvent()){
					ev.setRecording(false);
					ev.setEndDate(millis);
					ev.setEndTime(DateCalculations.getCurrentHour(millis), DateCalculations.getCurrentMinute(millis));
					DataBaseHandler.getInstance().updateEvent(ev, true);
					if(ev.getEventType() == event){
						Toast.makeText(c, c.getString(R.string.notification_logging_stopped), Toast.LENGTH_SHORT).show();
					}
					updateWidget(-1, c);
				}
				
				if((ev != null && ev.getEventType() != event) || ev == null){
					final Event eventToStart = new Event();
					if(event == Event.EVENT_TYPE_NORMAL_BREAK && RegulationTimesSummary.getInstance().getLastSpiltbreak() != null){
						eventToStart.setAsSplitBreak(true);
					}
					eventToStart.setRecording(true);
					eventToStart.setStartDate(millis);
					eventToStart.setStartTime(DateCalculations.getCurrentHour(millis), DateCalculations.getCurrentMinute(millis));
					eventToStart.setEventType(event);
					DataBaseHandler.getInstance().addNewEvent(eventToStart);
					Toast.makeText(c, c.getString(R.string.notification_logging_started), Toast.LENGTH_SHORT).show();
					updateWidget(event, c);
				}
			}
		});
	}
	
	
	public static void updateWidget(int eventtype, Context c){
		final  Intent svcIntent = new Intent(c, WidgetService.class);
        svcIntent.putExtra(StartEventAppWidgetProvider.INTENT_EXTRA_EVENTTYPE, eventtype);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        
		final AppWidgetManager man = AppWidgetManager.getInstance(c);
		final RemoteViews widget = new RemoteViews(c.getPackageName(), R.layout.widget_layout);
		
		widget.setRemoteAdapter(R.id.widget_start_logging_gridView, svcIntent);
		
		man.updateAppWidget(new ComponentName(c.getPackageName(), StartEventAppWidgetProvider.class.getName()), widget);
	}
	
	public static void updateWidget(int speed, int activityType, Context c){
		final AppWidgetManager man = AppWidgetManager.getInstance(c.getApplicationContext());
		final RemoteViews remViews = new RemoteViews(c.getPackageName(), R.layout.widget_layout);
		
		if(speed != -1 || activityType != -1 && DEBUG){
			remViews.setViewVisibility(R.id.widget_speed, View.VISIBLE);
			final StringBuilder b = new StringBuilder();
			b.append(String.valueOf(speed == -1 ? 0 : speed));
			b.append(" km/h");
			if(activityType != -1 || activityType != DetectedActivity.UNKNOWN){
				b.append(" (" + Utils.getNameFromType(activityType) + ")");
			}
			remViews.setTextViewText(R.id.widget_speed, b.toString());
		} else {
			remViews.setViewVisibility(R.id.widget_speed, View.GONE);
		}
		
		man.updateAppWidget(new ComponentName(c.getPackageName(), StartEventAppWidgetProvider.class.getName()), remViews);
	}
	


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {	
		if(position == mSelectedNavItem || isFinishing()){
			return;
		}
		mSelectedNavItem = position;
		
		Fragment frag;
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
		
    	switch(position){
    	case 0:
    		frag = new FragmentHome();
    		frag.setRetainInstance(true);
    		ft.replace(R.id.container, frag, FragmentHome.Tag).commit();
    		break;
    	case 1:
    		frag = new FragmentLog();
    		frag.setRetainInstance(true);
    		ft.replace(R.id.container, frag, FragmentLog.tag).commit();
    		break;
    	case 2:
    		frag = new FragmentWorkingDays();
    		frag.setRetainInstance(true);
    		ft.replace(R.id.container, frag, FragmentWorkingDays.TAG).commit();
    		break;
    	case 3:
    		frag = new LocationHistoryFragment();
    		frag.setRetainInstance(true);
    		ft.replace(R.id.container, frag, LocationHistoryFragment.TAG).commit();
    		break;
    	}
		
    	
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    
	    outState.putInt(BUNDLE_KEY_SELECTED_NAV_ITEM, mSelectedNavItem);
	    if(mSelectedNavItem == 0){
	    	final FragmentHome home = (FragmentHome) getSupportFragmentManager().findFragmentByTag(FragmentHome.Tag);
	    	if(home != null){
	    		getSupportFragmentManager().putFragment(outState, FragmentHome.Tag, home);
	    	}
	    } else if(mSelectedNavItem == 1){
	    	final FragmentLog log = (FragmentLog) getSupportFragmentManager().findFragmentByTag(FragmentLog.tag);
	    	if(log != null){
	    		getSupportFragmentManager().putFragment(outState, FragmentLog.tag, log);
	    	}
	    } else if(mSelectedNavItem == 3){
	    	final FragmentWorkingDays wd = (FragmentWorkingDays) getSupportFragmentManager().findFragmentByTag(FragmentWorkingDays.TAG);
	    	if(wd != null){
	    		getSupportFragmentManager().putFragment(outState, FragmentWorkingDays.TAG, wd);
	    	}
	    } else if(mSelectedNavItem == 4){
	    	final LocationHistoryFragment routes = (LocationHistoryFragment) getSupportFragmentManager().findFragmentByTag(LocationHistoryFragment.TAG);
	    	if(routes != null){
	    		getSupportFragmentManager().putFragment(outState, LocationHistoryFragment.TAG, routes);
	    	}
	    }
	}
	
	
	
	

}
