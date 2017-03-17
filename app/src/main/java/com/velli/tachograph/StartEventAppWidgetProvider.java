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

import com.velli.tachograph.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

public class StartEventAppWidgetProvider extends AppWidgetProvider {
	private static final String tag = "StartEventAppWidgetProvider ";
	private int currentAction = -2;
	public static final String BROADCAST_WIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
	public static final String INTENT_EXTRA_EVENTTYPE = "event type";
	public static final String INTENT_START_OR_STOP_EVENT = "start or stop event";
	
	@SuppressWarnings("deprecation")
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.i(tag, tag + "onUpdate()");

        final int lenght = appWidgetIds.length;
      
        for (int i = 0; i < lenght; i++) {
        	int appWidgetId = appWidgetIds[i];
            
         
            
            final  Intent svcIntent = new Intent(context, WidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            svcIntent.putExtra(INTENT_EXTRA_EVENTTYPE, currentAction);

            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
            
            final RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            widget.setRemoteAdapter(appWidgetId, R.id.widget_start_logging_gridView, svcIntent);

            final Intent clickIntent = new Intent(context, StartEventService.class);
            final PendingIntent pending = PendingIntent.getService(context, 0, clickIntent, 0);
            widget.setPendingIntentTemplate(R.id.widget_start_logging_gridView, pending);
           
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, widget);
            super.onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }
	

	
	@Override
	public void onReceive(Context context, Intent intent){
		super.onReceive(context, intent);

		if(intent.getAction().equals(BROADCAST_WIDGET_UPDATE)){
			currentAction = intent.getIntExtra(INTENT_EXTRA_EVENTTYPE, -1);

			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
		    ComponentName thisWidget = new ComponentName(context.getApplicationContext(), StartEventAppWidgetProvider.class);
		    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		    if (appWidgetIds != null && appWidgetIds.length > 0) {
		    	onUpdate(context, appWidgetManager, appWidgetIds);
		    }

		}
	}


	
	
	
	


}
