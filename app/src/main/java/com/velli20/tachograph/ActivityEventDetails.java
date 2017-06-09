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


import com.velli20.tachograph.collections.ListAdapterEventDetails;
import com.velli20.tachograph.collections.SpacesItemDecorationEventDetails;
import com.velli20.tachograph.database.DataBaseHandler;
import com.velli20.tachograph.database.DataBaseHandler.OnGetEventTaskCompleted;
import com.velli20.tachograph.database.GetLoggedRouteTask.OnGetLoggedRouteListener;
import com.velli20.tachograph.location.ActivityLoggedRouteMap;
import com.velli20.tachograph.location.LoggedRoute;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Locale;

public class ActivityEventDetails extends AppCompatActivity implements OnGetLoggedRouteListener, View.OnClickListener, DataBaseHandler.OnDatabaseEditedListener {
    public static final String INTENT_EXTRA_EVENT_ID = "intent extra event id";
    private ListAdapterEventDetails mAdapter;

    private Event mEvent;
    private LoggedRoute mLoggedRoute;

    private int mEventId = -1;
    private int mColorBlue;

    private float mDensity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        mAdapter = new ListAdapterEventDetails(this);
        mAdapter.setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activity_event_details_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SpacesItemDecorationEventDetails(getResources().getDimensionPixelSize(R.dimen.card_padding)));
        recyclerView.setAdapter(mAdapter);


        final Resources res = getResources();

        mDensity = res.getDisplayMetrics().density;
        mColorBlue = res.getColor(R.color.color_primary);

        final Intent intent = getIntent();

        if (intent != null && intent.getExtras() != null) {
            mEventId = intent.getExtras().getInt(INTENT_EXTRA_EVENT_ID, -1);
        }

        setSupportActionBar((Toolbar) findViewById(R.id.activity_event_details_toolbar));

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowHomeEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getEventData(mEventId);
        DataBaseHandler.getInstance().registerOnDatabaseEditedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        DataBaseHandler.getInstance().registerOnDatabaseEditedListener(this);
    }

    @Override
    public void onDatabaseEdited(int action, int rowId) {
        getEventData(mEventId);
    }

    private void getEventData(int eventId) {
        if (eventId == -1) {
            return;
        }

        DataBaseHandler.getInstance().getEvent(eventId, new OnGetEventTaskCompleted() {

            @Override
            public void onGetEvent(Event ev) {
                if (ev != null) {
                    setData(ev);
                } else {
                    finish();
                }

            }
        }, true);
    }


    private void setData(Event ev) {
        mEvent = ev;
        if (ev == null) {
            return;
        }

        String[] events = getResources().getStringArray(R.array.event_explanations);
        int[] colors = getResources().getIntArray(R.array.event_colors);

        (findViewById(R.id.activity_event_details_toolbar)).setBackgroundColor(colors[ev.getEventType()]);
        getSupportActionBar().setTitle(events[ev.getEventType()]);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getStatusBarHue(colors[ev.getEventType()]));
        }

        setTitle((events[ev.getEventType()]));

        if (mAdapter != null) {
            mAdapter.setEvent(ev);
        }

        if (ev.hasLoggedRoute()) {
            DataBaseHandler.getInstance().getLoggedRoute(ev.getRowId(), (int) mDensity * 2, mColorBlue, this);
        } else if(mAdapter != null) {
            mLoggedRoute = null;
            mAdapter.setLoggedRoute(null);
        }
    }


    private int getStatusBarHue(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_event_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_item_discard:
                DataBaseHandler.getInstance().deleteEvent(mEventId);
                finish();
                return true;
            case R.id.menu_item_edit:
                Intent intent = new Intent(this, ActivityAddNewEvent.class);
                intent.putExtra(ActivityAddNewEvent.INTENT_EXTRA_DISPLAY_MODE, ActivityAddNewEvent.DISPLAY_MODE_EDIT_OR_DELETE);
                intent.putExtra(ActivityAddNewEvent.INTENT_EXTRA_EVENT_ROW_ID, mEventId);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.list_item_event_map_overflow_button:
                showMapContextualMenu(view);
                break;
        }
    }

    private void showMapContextualMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_open_full_map:
                        Intent i = new Intent(ActivityEventDetails.this, ActivityLoggedRouteMap.class);
                        i.putExtra(ActivityLoggedRouteMap.EXTRAS_LOCATION_EVENT_ID, mEventId);

                        if (mLoggedRoute != null) {
                            i.putExtra(ActivityLoggedRouteMap.EXTRAS_TITLE, String.format(Locale.getDefault(), "%.2f km", mLoggedRoute.getDistance()));
                        }

                        startActivity(i);
                        return true;
                    case R.id.menu_delete_map:
                        DataBaseHandler.getInstance().deleteLocations(mEventId);
                        return true;
                }
                return false;
            }
        });
        popupMenu.inflate(R.menu.menu_activity_event_details_map_contextual);
        popupMenu.show();
    }

    @Override
    public void onGetLoggedRoute(LoggedRoute route) {
        mLoggedRoute = route;

        if (route != null && mAdapter != null) {
            mAdapter.setLoggedRoute(route);
        } else {
            mAdapter.setLoggedRoute(null);
        }

    }

}
