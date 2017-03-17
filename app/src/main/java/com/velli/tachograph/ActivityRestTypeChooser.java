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


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

public class ActivityRestTypeChooser extends Activity {
	public static final String INTENT_EXTRA_CURRENT_EVENT_TYPE = "current event type";
	public static final String TAG = "ActivityRestTypeChooser ";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(getIntent() != null){
			int type = getIntent().getIntExtra(INTENT_EXTRA_CURRENT_EVENT_TYPE, -1);
			
			showChooseRestTypeDialog(this, type, new MaterialDialog.ListCallback() {
				
				@Override
				public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
					if(which == 0){
						ActivityMain.startRecordingEvent(Event.EVENT_TYPE_WEEKLY_REST, ActivityRestTypeChooser.this);
		            } else if(which == 1){
		                ActivityMain.startRecordingEvent(Event.EVENT_TYPE_DAILY_REST, ActivityRestTypeChooser.this);
		            } else if(which == 2){
		                ActivityMain.startRecordingEvent(Event.EVENT_TYPE_NORMAL_BREAK, ActivityRestTypeChooser.this);
		            }
					finish();
					}
				});
			
		}
	}
	
	public static void showChooseRestTypeDialog(final Context c, int currentEventType, final MaterialDialog.ListCallback callback){
		new MaterialDialog.Builder(c)
        .items(Utils.getRestTimeTypeArray(c.getResources(), currentEventType == -1? -1 : currentEventType))
        .itemsCallbackSingleChoice(currentEventType == -1? 0 : Utils.getRestingSelectedOption(currentEventType), new MaterialDialog.ListCallbackSingleChoice() {
			
			@Override
			public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
				callback.onSelection(dialog, itemView, which, text);
				if(c instanceof Activity && !(c instanceof AppCompatActivity)){
					((Activity)c).finish();
				}
				return true;
			}
		})
        .negativeText(R.string.action_cancel)
        .positiveText(R.string.action_ok)
        .show();
	}

}
