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

import com.velli.tachograph.views.RobotoSwitchCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;

public class ActivityItemsToShowInSummary extends AppCompatActivity implements OnSharedPreferenceChangeListener {
	private static final boolean DEBUG = true;
	private static final String TAG = "ActivityItemsToShowInSummary ";
	private RecyclerView mList;
	private ItemsListAdapter mAdapter;
	private ItemTouchHelper mItemTouchHelper;
	private int[] mPositions;
	private boolean[] mShow;
	private boolean mNotifyDataSetChanged = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_items_to_show_in_summary);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		View mToolbarShadow = findViewById(R.id.toolbar_shadow);
		
		mAdapter = new ItemsListAdapter(this);
		
		mList = (RecyclerView) findViewById(R.id.activity_items_to_show_list);
		mList.setLayoutManager(new LinearLayoutManager(this));
		mList.setAdapter(mAdapter);
		
		mItemTouchHelper = new ItemTouchHelper(mSimpleItemTouchCallback);
		mItemTouchHelper.attachToRecyclerView(mList);
		
		setSupportActionBar(mToolbar);
		
		final ActionBar ab = getSupportActionBar();
		ab.setTitle(R.string.title_items_to_show_in_summary);
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowHomeEnabled(true);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
	    	mToolbarShadow.setVisibility(View.GONE);
	    }
	}
	
	@Override
	public void onResume(){
		super.onResume();
		getSharedPreferences(ShowItemsSettings.KEY, Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		getSharedPreferences(ShowItemsSettings.KEY, Context.MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.menu_activity_items_to_show, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_item_restore:
			mNotifyDataSetChanged = true;
			ShowItemsSettings.restoreDefaults(this);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private final ItemTouchHelper.SimpleCallback mSimpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
	    
		@Override
	    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
			
	    }

		@Override
		public boolean onMove(RecyclerView arg0, ViewHolder viewHolder, ViewHolder target) {
			final int fromPos = mPositions[viewHolder.getAdapterPosition()];
            final int toPos = mPositions[target.getAdapterPosition()];

            final boolean showFromPos = mShow[viewHolder.getAdapterPosition()];
            final boolean showToPos = mShow[target.getAdapterPosition()];
            
            if(DEBUG){
            	Log.i(TAG, TAG + "onMove() from: " + fromPos + " to: " + toPos);
            }
            
            mPositions[viewHolder.getAdapterPosition()] = toPos;
            mPositions[target.getAdapterPosition()] = fromPos;
           
            mShow[viewHolder.getAdapterPosition()] = showToPos;
            mShow[target.getAdapterPosition()] = showFromPos;
            
            ShowItemsSettings.setItemPosition(getSharedPreferences(ShowItemsSettings.KEY, Context.MODE_PRIVATE), fromPos, target.getAdapterPosition());
            ShowItemsSettings.setItemPosition(getSharedPreferences(ShowItemsSettings.KEY, Context.MODE_PRIVATE), toPos, viewHolder.getAdapterPosition());
            
            mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            
			return true;
		}
		
		public boolean isLongPressDragEnabled() {
			return false;
		}
	};
	
	private class ItemsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private final LayoutInflater mInflater;
		private final String[] mItems;
		
		
		
		public ItemsListAdapter(Context c) {
			mInflater = LayoutInflater.from(c);
			mItems = c.getResources().getStringArray(R.array.items_to_show_in_summary);
			
			mPositions = ShowItemsSettings.getItemsToShowPosition(c.getSharedPreferences(ShowItemsSettings.KEY, Context.MODE_PRIVATE));
			mShow = ShowItemsSettings.getItemsToShow(c.getSharedPreferences(ShowItemsSettings.KEY, Context.MODE_PRIVATE));
		}
		
		@Override
		public int getItemCount() {
			return mItems.length;
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, int position) {
			
			((ViewHolderSwitch)holder).mSwitch.setText(mItems[mPositions[position]]);
			((ViewHolderSwitch)holder).mSwitch.setChecked(mShow[mPositions[position]], false);
			((ViewHolderSwitch)holder).mSwitch.setOnCheckedChangeListener(new SwitchCheckedListener(position));
			((ViewHolderSwitch)holder).mDrag.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					mItemTouchHelper.startDrag(holder);
					return false;
				}
			});
				
				
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
			return new ViewHolderSwitch(mInflater.inflate(R.layout.list_item_items_to_show, root, false));
		}
		
		private class SwitchCheckedListener implements OnCheckedChangeListener {
			final int mPosition;
			
			public SwitchCheckedListener(int position){
				mPosition = position;
			}

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				ShowItemsSettings.setShowItem(ActivityItemsToShowInSummary.this, mPositions[mPosition], isChecked);
			}
			
		}
		
		
		
	}
	
	
	
	public class ViewHolderSwitch extends RecyclerView.ViewHolder {
		final RobotoSwitchCompat mSwitch;
		final ImageButton mDrag;
		
		public ViewHolderSwitch(View itemView) {
			super(itemView);
			mSwitch = (RobotoSwitchCompat) itemView.findViewById(R.id.list_items_to_show_switch);
			mDrag = (ImageButton) itemView.findViewById(R.id.list_items_to_show_drag);
		}

	}



	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(DEBUG){
			Log.i(TAG, TAG + "onSharedPreferenceChanged() key: " + key);
		}
		mPositions = ShowItemsSettings.getItemsToShowPosition(getSharedPreferences(ShowItemsSettings.KEY, Context.MODE_PRIVATE));
		mShow = ShowItemsSettings.getItemsToShow(getSharedPreferences(ShowItemsSettings.KEY, Context.MODE_PRIVATE));
		if(mNotifyDataSetChanged && mList != null && mAdapter != null){
			mAdapter.notifyDataSetChanged();
		}
		mNotifyDataSetChanged = false;
	}
}
