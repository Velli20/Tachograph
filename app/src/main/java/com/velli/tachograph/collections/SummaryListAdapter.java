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

package com.velli.tachograph.collections;

import java.util.ArrayList;

import com.velli.tachograph.R;
import com.velli.tachograph.Event;
import com.velli.tachograph.views.ListItemWorkLimit;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;


public class SummaryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnCheckedChangeListener{
	private static final String Tag = "SummaryListAdapter ";
	public static final int VIEW_TYPE_CHART  = 0;
    public static final int VIEW_TYPE_REGULATION = 1;
	public static final int VIEW_TYPE_ACTIVITY_CHOOSER= 2;
    public static final int VIEW_TYPE_BREAK = 3;
    
    private final LayoutInflater mInflater;
    private float mCardElevation = 0;
	
    private ArrayList<ListItemToShow> mItemsToShow;
    private ArrayList<ListItemToShow> mOldItemsToShow;
    private OnSpiltBreakSwitchCheckedChangedListener mSwitchListener;
    private boolean mShowTimesCountDown = false;
	private Event mCurrentEvent;
    private View.OnClickListener mClickListener;
    
    public interface OnSpiltBreakSwitchCheckedChangedListener {
		void onSplitBreakSwitchChecked(boolean checked);
    }
    
    public SummaryListAdapter(Context c,  ArrayList<ListItemToShow> items){
    	mInflater = LayoutInflater.from(c);
    	mCardElevation = (c.getResources().getDimension(R.dimen.card_elevation));
    	mItemsToShow = items;
    	setHasStableIds(true);
    }
    
    @Override
    public long getItemId(int position){
    	return mItemsToShow.get(position).listItem;
    }
    
	@Override
	public int getItemCount() {
		return mItemsToShow.size();
	}


	public void setCurrentEvent(Event ev) {
		mCurrentEvent = ev;
		notifyItemChanged(0);
	}

	
	public void setShowTimesCountDown(boolean showCountDown) {
		mShowTimesCountDown = showCountDown;
	}
	
	public void setItemsToShow(ArrayList<ListItemToShow> list){
		mOldItemsToShow = new ArrayList<>(mItemsToShow);
		mItemsToShow = list;
		
		ArrayList<Integer> toAdd = new ArrayList<>();
		ArrayList<Integer> toUpdate = new ArrayList<>();
		ArrayList<int[]> toMove = new ArrayList<>();
		ArrayList<Integer> toDelete = new ArrayList<>();
		
		int length = list.size();
				
		for(int i = 0; i < length; i++){
			int item = mItemsToShow.get(i).listItem;
			
			if(!mOldItemsToShow.contains(mItemsToShow.get(i))){
				toAdd.add(mItemsToShow.get(i).position);
			} else if(mOldItemsToShow.get(i).listItem != item){
				
				int newPos = mItemsToShow.get(i).position;
				int oldPos = mOldItemsToShow.get(item).position;
				
				if(newPos == oldPos){
					toUpdate.add(mItemsToShow.get(i).position);
				} else {
					toMove.add(new int[]{oldPos, newPos});
				}
			}
			if(item != -1){
				mOldItemsToShow.remove(mItemsToShow.get(i));
			}
		}
		
		int size = mOldItemsToShow.size();
		if(size > 0){
			
			for (int i = 0; i < size; i++) {
			    int value = mOldItemsToShow.get(i).position;
			    
			    toDelete.add(value);
			}
		}
		
		//First, delete
		for(Integer position : toDelete){
			notifyItemRemoved(position);
		}
		
		//Next, move
		for(int[] position : toMove){
			if(toAdd.isEmpty() && toDelete.isEmpty()){
				notifyItemMoved(position[0], position[1]);
			}
		}
		
		//Next, update
		if(toMove.isEmpty() && toAdd.isEmpty()){
			notifyDataSetChanged();
		} 
		
		
		//Finally, add
		for(Integer position : toAdd){
			notifyItemInserted(position);
		}
		
		if(toUpdate.isEmpty()){
			notifyDataSetChanged();
		}
	 
	}

	


	
	public void setListeners(OnSpiltBreakSwitchCheckedChangedListener l, View.OnClickListener clickListener){
		mSwitchListener = l;
        mClickListener = clickListener;
	}
	
	@Override
    public int getItemViewType(int position) {
		int listItem = mItemsToShow.get(position).listItem;
		
		if(listItem == ListItemWorkLimit.TYPE_CHART){
			return VIEW_TYPE_ACTIVITY_CHOOSER;
		} else if(listItem == ListItemWorkLimit.TYPE_BREAK
				|| listItem == ListItemWorkLimit.TYPE_SPLIT_BREAK){
			return VIEW_TYPE_BREAK;
		} else {
			return VIEW_TYPE_REGULATION;
		}
    }
	
	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		final int viewType = getItemViewType(position);
		Log.i(Tag, Tag + "onBindViewHolder()");
		
		if(viewType == VIEW_TYPE_REGULATION) {
			((ViewHolderWorkLimit)holder).mLimit.setShowCountDown(mShowTimesCountDown);
			((ViewHolderWorkLimit)holder).mLimit.setType(mItemsToShow.get(position).listItem );
			
		} else if(viewType == VIEW_TYPE_BREAK){
			((ViewHolderBreak)holder).mLimit.setShowCountDown(mShowTimesCountDown);
			((ViewHolderBreak)holder).mLimit.setType(mItemsToShow.get(position).listItem);
		} else if(viewType == VIEW_TYPE_ACTIVITY_CHOOSER) {
			((ViewHolderActivityChooser)holder).mActivityChooser.setEvent(mCurrentEvent);
            if(mClickListener != null) {
                ((ViewHolderActivityChooser)holder).mActivityChooser.setOnClickListener(mClickListener);
            }
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
		final View itemView;
		Log.i(Tag, Tag + "onCreateViewHolder()");
		if(viewType == VIEW_TYPE_CHART){
			itemView = mInflater.inflate(R.layout.list_item_chart, root, false);
			((CardView)itemView).setCardElevation(mCardElevation);
			return new ViewHolderChart(itemView);
		} else if(viewType == VIEW_TYPE_REGULATION){
        	itemView = mInflater.inflate(R.layout.list_item_work_limit, root, false);
        	((CardView)itemView).setCardElevation(mCardElevation);
        	return new ViewHolderWorkLimit(itemView);
		} else if(viewType == VIEW_TYPE_ACTIVITY_CHOOSER) {
			itemView = mInflater.inflate(R.layout.list_item_activity_chooser, root, false);
			((CardView)itemView).setCardElevation(mCardElevation);
			return new ViewHolderActivityChooser(itemView);
		} else {
			itemView = mInflater.inflate(R.layout.list_item_work_limit_break, root, false);
        	((ListItemWorkLimit)itemView).getSwitch().setOnCheckedChangeListener(SummaryListAdapter.this);
        	((CardView)itemView).setCardElevation(mCardElevation);
        	return new ViewHolderBreak(itemView);
		} 
		
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(mSwitchListener != null){
			mSwitchListener.onSplitBreakSwitchChecked(isChecked);
		}
		
	}

}
