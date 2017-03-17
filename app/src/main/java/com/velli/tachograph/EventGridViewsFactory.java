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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

public class EventGridViewsFactory implements RemoteViewsFactory {
    private Context mContext;
    private final int iconRes[] = {R.drawable.ic_action_driving, R.drawable.ic_action_rest, 
    		R.drawable.ic_action_other, R.drawable.ic_action_poa};
     private int mEvent;
    
	public EventGridViewsFactory(Context context, int event){
		mContext = context;
		mEvent = event;
	}
	
	@Override
	public void onCreate() {
		
	}

	@Override
	public void onDataSetChanged() {
		
	}

	@Override
	public void onDestroy() {
		
	}

	@Override
	public int getCount() {
		return 4;
	}

	public int getEventType(){
		if(mEvent == Event.EVENT_TYPE_DRIVING){
			return 0;
		} else if(mEvent == Event.EVENT_TYPE_NORMAL_BREAK 
        		|| mEvent == Event.EVENT_TYPE_DAILY_REST
        		|| mEvent == Event.EVENT_TYPE_WEEKLY_REST
        		|| mEvent == Event.EVENT_TYPE_SPLIT_BREAK){
        	return 1;
        } else if(mEvent == Event.EVENT_TYPE_OTHER_WORK){
        	return 2;
        } else if(mEvent == Event.EVENT_TYPE_POA){
        	return 3;
        }
		return -1;
	}
	@Override
	public RemoteViews getViewAt(int position) {
		 RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_grid_item);
		 Intent intent = new Intent();
		 Bundle bundle = new Bundle();
		 
		 bundle.putInt(StartEventService.INTENT_EXTRA_EVENT, position);
		 bundle.putInt(StartEventService.INTENT_EXTRA_CURRENT_EVENT, mEvent);
		 intent.putExtras(bundle);
		 
         views.setOnClickFillInIntent(R.id.widget_start_logging_button, intent);
         views.setImageViewResource(R.id.widget_start_logging_button, iconRes[position]);
         
		 if(position == getEventType()){
			 views.setInt(R.id.widget_start_logging_button, "setAlpha", 255);
		 } else {
			 views.setInt(R.id.widget_start_logging_button, "setAlpha", 125);
		 }
		     
		 return views;
	}

	@Override
	public RemoteViews getLoadingView() {
		 RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_grid_item);
	
		 views.setInt(R.id.widget_start_logging_button, "setAlpha", 125);
		 
		     
		 return views;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}
	
}
