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

import java.util.ArrayList;

import com.afollestad.materialdialogs.MaterialDialog;
import com.velli20.tachograph.collections.ListAdapterFragmentNow;
import com.velli20.tachograph.collections.ListItemRegulationToShow;
import com.velli20.tachograph.collections.SpacesItemDecorationWorkingLimit;
import com.velli20.tachograph.database.DataBaseHandler;
import com.velli20.tachograph.database.DataBaseHandler.OnDatabaseEditedListener;
import com.velli20.tachograph.database.DataBaseHandler.OnGetEventTaskCompleted;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class FragmentNow extends Fragment implements OnClickListener, OnDatabaseEditedListener {
	public static final String Tag = "FragmentNow ";
	private static final String BUNDLE_KEY_LAYOUT_MANAGER_STATE = "layout manager state";
	
	private RecyclerView mListWorkLimits;
	private ListAdapterFragmentNow mWorkAdapter;

	private Event mRecordingEvent;

    private ArrayList<ListItemRegulationToShow> mItemsToShow;
	
	private boolean mShowTimesCountDown = false;
	private boolean mShowBigActivityCards = true;
    private boolean mPaused = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		mShowTimesCountDown = Integer.parseInt(prefs.getString(getString(R.string.preference_key_count_activity_progress), "0")) == 0;
		mShowBigActivityCards = prefs.getBoolean(getString(R.string.preference_key_show_big_activity_cards), true);

		DataBaseHandler.getInstance().registerOnDatabaseEditedListener(this);
	
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
	    final View mView = inflater.inflate(R.layout.fragment_home, root, false);
	    
		mListWorkLimits = (RecyclerView) mView.findViewById(R.id.summary_work_limit_list);

		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		getCurrentAction();

	    Parcelable layoutManagerState = null;
	    
	    if(savedInstanceState != null){
	    	layoutManagerState = savedInstanceState.getParcelable(BUNDLE_KEY_LAYOUT_MANAGER_STATE);
	    } 

		mListWorkLimits.setLayoutManager(new LinearLayoutManager(getActivity()));
		mListWorkLimits.addItemDecoration(new SpacesItemDecorationWorkingLimit(getResources().getDimensionPixelSize(R.dimen.card_padding)));

		mWorkAdapter = new ListAdapterFragmentNow(getActivity(),  mItemsToShow, mShowBigActivityCards);
		mWorkAdapter.setListeners(this);
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
		mItemsToShow = SettingsRegulationsToShow.getCompleteList(getActivity(), mRecordingEvent);
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		mShowTimesCountDown = Integer.parseInt(prefs.getString(getString(R.string.preference_key_count_activity_progress), "0")) == 0;

		if(mWorkAdapter != null && mPaused) {
			mWorkAdapter.setShowTimesCountDown(mShowTimesCountDown);
			mWorkAdapter.setItemsToShow(mItemsToShow);
			mWorkAdapter.setCurrentEvent(mRecordingEvent);
			mWorkAdapter.setListeners(this);
		}
        mPaused = false;

	}

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
		if(mWorkAdapter != null) {
			mWorkAdapter.setListeners(this);
		}
    }

	
	@Override
	public void onDestroy(){
		super.onDestroy();
		DataBaseHandler.getInstance().unregisterOnDatabaseEditedListener(this);
        if(mWorkAdapter != null) {
            mWorkAdapter.setListeners(null);
        }
		mWorkAdapter = null;
		mListWorkLimits = null;
		mItemsToShow = null;
		mRecordingEvent = null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_fragment_home, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem cardSize = menu.findItem(R.id.action_show_activity_card_size);
		if(cardSize != null) {
			cardSize.setIcon(mShowBigActivityCards ? R.drawable.ic_view_activity_small_white : R.drawable.ic_view_activity_big_white);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_show_items:
				Intent i = new Intent(getActivity(), ActivityItemsToShowInSummary.class);
				getActivity().startActivity(i);
				return true;
			case R.id.action_show_activity_card_size:
				final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
				mShowBigActivityCards = !mShowBigActivityCards;
				prefs.edit().putBoolean(getString(R.string.preference_key_show_big_activity_cards), mShowBigActivityCards).apply();
				getActivity().invalidateOptionsMenu();

				if(mWorkAdapter != null) {
					mWorkAdapter.setShowBigActivityCards(mShowBigActivityCards);
				}
				return true;
		}

		return false;
	}
	
	private void getCurrentAction(){
		DataBaseHandler.getInstance().getRecordingEvent(new OnGetEventTaskCompleted() {

			@Override
			public void onGetEvent(final Event ev) {
				mRecordingEvent = ev;

				mItemsToShow = SettingsRegulationsToShow.getCompleteList(getActivity(), mRecordingEvent);
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
						new EventRecorder().startRecordingEvent(Event.EVENT_TYPE_WEEKLY_REST, System.currentTimeMillis());
	                } else if(which == 1){
						new EventRecorder().startRecordingEvent(Event.EVENT_TYPE_DAILY_REST, System.currentTimeMillis());
	                } else if(which == 2){
						new EventRecorder().startRecordingEvent(Event.EVENT_TYPE_NORMAL_BREAK, System.currentTimeMillis());
	                }
				}
			});
			
		} else {
			new EventRecorder().startRecordingEvent(eventToRecord, System.currentTimeMillis());
		}
	}
	
	@Override
	public void onDatabaseEdited(int action, int rowId) {
		getCurrentAction();
	}
	

	@Override
    public void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    	outState.putParcelable(BUNDLE_KEY_LAYOUT_MANAGER_STATE, mListWorkLimits.getLayoutManager().onSaveInstanceState());
	}


}
