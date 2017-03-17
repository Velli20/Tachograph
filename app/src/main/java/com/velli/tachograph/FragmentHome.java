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

import com.afollestad.materialdialogs.MaterialDialog;
import com.velli.tachograph.collections.ListItemToShow;
import com.velli.tachograph.collections.SpacesItemDecoration;
import com.velli.tachograph.collections.SummaryListAdapter;
import com.velli.tachograph.collections.SummaryListAdapter.OnSpiltBreakSwitchCheckedChangedListener;
import com.velli.tachograph.database.DataBaseHandler;
import com.velli.tachograph.database.DataBaseHandler.OnDatabaseEditedListener;
import com.velli.tachograph.database.DataBaseHandler.OnGetEventTaskCompleted;
import com.velli.tachograph.restingtimes.RegulationTimesSummary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.ViewGroup;

public class FragmentHome extends Fragment implements OnClickListener, OnDatabaseEditedListener, OnSpiltBreakSwitchCheckedChangedListener {
	public static final String Tag = "FragmentHome ";
	private static final String BUNDLE_KEY_LAYOUT_MANAGER_STATE = "layout manager state";
	
	private RecyclerView mListWorkLimits;
	private SummaryListAdapter mWorkAdapter;

	private Event mRecordingEvent;

    private ArrayList<ListItemToShow> mItemsToShow = new ArrayList<>();
	
	private boolean mShowTimesCountDown = false;
    private boolean mPaused = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mShowTimesCountDown = Integer.parseInt(prefs.getString(getString(R.string.preference_key_count_activity_progress), "0")) == 0;
		DataBaseHandler.getInstance().registerOnDatabaseEditedListener(this);
	
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
	    final View mView = inflater.inflate(R.layout.fragment_summary, root, false);
	    
		mListWorkLimits = (RecyclerView)mView.findViewById(R.id.summary_work_limit_list);

		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		getCurrentAction();

	    final int screenWidth = getScreenSize().x;
	    final int requiredMinWidth = getResources().getDimensionPixelSize(R.dimen.list_item_work_limit_min_required_width);
	    
	    
	    final int spanCount;
	    Parcelable layoutManagerState = null;
	    
	    if(savedInstanceState != null){
	    	layoutManagerState = savedInstanceState.getParcelable(BUNDLE_KEY_LAYOUT_MANAGER_STATE);
	    } 
	    
	    if(screenWidth > requiredMinWidth){
	    	spanCount = screenWidth / requiredMinWidth;
	    } else {
	    	spanCount = 1;
	    }
	    
	    if(spanCount > 1){
	    	final GridLayoutManager manager = new GridLayoutManager(getActivity(), spanCount);
	 	    manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

	 			@Override
	 			public int getSpanSize(int position) {
	 				if(mWorkAdapter != null){
	 					if(mWorkAdapter.getItemViewType(position) == SummaryListAdapter.VIEW_TYPE_CHART
                                || mWorkAdapter.getItemViewType(position) == SummaryListAdapter.VIEW_TYPE_ACTIVITY_CHOOSER){
	 						return spanCount;
	 					} else {
	 						return 1;
	 					}
	 				}
	 				if(position == 0){
	 					return spanCount;
	 				} else {
	 					return 1;
	 				}
	 			}
	 	    	
	 	    });
	 	    mListWorkLimits.setLayoutManager(manager);
	 	    
	    } else {
		    mListWorkLimits.setLayoutManager(new LinearLayoutManager(getActivity()));
	    }

		mListWorkLimits.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.card_padding), spanCount));
	    mListWorkLimits.setItemAnimator(new DefaultItemAnimator());


		mWorkAdapter = new SummaryListAdapter(getActivity(),  mItemsToShow);
		mWorkAdapter.setListeners(this, this);
		mWorkAdapter.setShowTimesCountDown(mShowTimesCountDown);
		mWorkAdapter.setCurrentEvent(mRecordingEvent);

		mListWorkLimits.setAdapter(mWorkAdapter);

		if (layoutManagerState != null) {
			mListWorkLimits.getLayoutManager().onRestoreInstanceState(layoutManagerState);
		}
		
		
		setHasOptionsMenu(true);
	}
	

	
	    
	@Override
	public void onResume(){
		super.onResume();
		mItemsToShow = ShowItemsSettings.getCompleteList(getActivity(), mRecordingEvent);
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		mShowTimesCountDown = Integer.parseInt(prefs.getString(getString(R.string.preference_key_count_activity_progress), "0")) == 0;

		if(mWorkAdapter != null && mPaused) {
			mWorkAdapter.setShowTimesCountDown(mShowTimesCountDown);
			mWorkAdapter.setItemsToShow(mItemsToShow);
			mWorkAdapter.setCurrentEvent(mRecordingEvent);
		}
        mPaused = false;

	}

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
    }

	
	@Override
	public void onDestroy(){
		super.onDestroy();
		DataBaseHandler.getInstance().unregisterOnDatabaseEditedListener(this);
        if(mWorkAdapter != null) {
            mWorkAdapter.setListeners(null, null);
        }
		mWorkAdapter = null;
		mListWorkLimits = null;
		mItemsToShow = null;
		mRecordingEvent = null;
	}
	
	private Point getScreenSize(){
		final Point point = new Point();
		final WindowManager wm = (WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE);
		final Display display = wm.getDefaultDisplay();

		display.getSize(point);
		return point;
	}
	
	private void getCurrentAction(){
		DataBaseHandler.getInstance().getRecordingEvent(new OnGetEventTaskCompleted() {

			@Override
			public void onGetEvent(final Event ev) {
				if(ev != null){
					long sync = (((System.currentTimeMillis() - ev.getStartDateInMillis()) % 60000) );
					RegulationTimesSummary.getInstance().syncAutoUpdater(sync);
                }
				mRecordingEvent = ev;

				mItemsToShow = ShowItemsSettings.getCompleteList(getActivity(), mRecordingEvent);
				if(mWorkAdapter != null){
					mWorkAdapter.setShowTimesCountDown(mShowTimesCountDown);
					mWorkAdapter.setItemsToShow(mItemsToShow);
					mWorkAdapter.setCurrentEvent(ev);
				}
			}
		});
	}
	

	
	@Override
	public void onClick(View v) {

		switch(v.getId()){

		case R.id.list_item_activity_event_chooser_driving:
			recordEvent(Event.EVENT_TYPE_DRIVING);
			break;
		case R.id.list_item_activity_event_chooser_resting:
			recordEvent(Event.EVENT_TYPE_NORMAL_BREAK);
			break;
		case R.id.list_item_activity_event_chooser_other_work:
			recordEvent(Event.EVENT_TYPE_OTHER_WORK);
			break;
		case R.id.list_item_activity_event_chooser_poa:
			recordEvent(Event.EVENT_TYPE_POA);
			break;
			
		}
		
	}
	
	private void recordEvent(int eventToRecord){
		if(eventToRecord == Event.EVENT_TYPE_NORMAL_BREAK){
			ActivityRestTypeChooser.showChooseRestTypeDialog(getActivity(), mRecordingEvent == null? -1 : mRecordingEvent.getEventType(), new MaterialDialog.ListCallback() {
				
				@Override
				public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
					if(which == 0){
	                	ActivityMain.startRecordingEvent(Event.EVENT_TYPE_WEEKLY_REST, getActivity());
	                } else if(which == 1){
	                	ActivityMain.startRecordingEvent(Event.EVENT_TYPE_DAILY_REST, getActivity());
	                } else if(which == 2){
	                	ActivityMain.startRecordingEvent(Event.EVENT_TYPE_NORMAL_BREAK, getActivity());
	                }
				}
			});
			
		} else {
			ActivityMain.startRecordingEvent(eventToRecord, getActivity());
		}
	}
	
	@Override
	public void onDatabaseEdited(int action, int rowId) {
		getCurrentAction();
	}


	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.home_fragment_options, menu);
	    super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		case R.id.action_show_items:
			Intent i = new Intent(getActivity(), ActivityItemsToShowInSummary.class);
			getActivity().startActivity(i);
			return true;
		}
		
		return false;
	}
	

	

	
	@Override
    public void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    	
    	outState.putParcelable(BUNDLE_KEY_LAYOUT_MANAGER_STATE, mListWorkLimits.getLayoutManager().onSaveInstanceState());
	
	}

	@Override
	public void onSplitBreakSwitchChecked(boolean checked) {
		if(mRecordingEvent != null){
			if(mRecordingEvent.getEventType() == Event.EVENT_TYPE_NORMAL_BREAK){
				mRecordingEvent.setAsSplitBreak(checked);
				DataBaseHandler.getInstance().updateEvent(mRecordingEvent, true);
			} 
		}
	}



}
