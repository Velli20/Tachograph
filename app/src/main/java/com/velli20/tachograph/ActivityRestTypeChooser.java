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
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

public class ActivityRestTypeChooser extends Activity {
    public static final String INTENT_EXTRA_CURRENT_EVENT_TYPE = "current event type";
    public static final String TAG = "ActivityRestTypeChooser ";

    public static void showChooseRestTypeDialog(final Context c, int currentEventType, final MaterialDialog.ListCallback callback) {
        new MaterialDialog.Builder(c)
                .items(getRestTimeTypeArray(c.getResources(), currentEventType))
                .itemsCallbackSingleChoice(currentEventType == -1 ? 0 : getRestingSelectedOption(currentEventType), new MaterialDialog.ListCallbackSingleChoice() {

                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        callback.onSelection(dialog, itemView, which, text);
                        if (c instanceof Activity && !(c instanceof AppCompatActivity)) {
                            ((Activity) c).finish();
                        }
                        return true;
                    }
                })
                .negativeText(R.string.action_cancel)
                .positiveText(R.string.action_ok)
                .show();
    }

    public static String[] getRestTimeTypeArray(Resources res, int currentAction) {
        return new String[]{
                currentAction == Event.EVENT_TYPE_WEEKLY_REST ? res.getString(R.string.menu_stop_weekly_rest)
                        : res.getString(R.string.menu_start_weekly_rest),
                currentAction == Event.EVENT_TYPE_DAILY_REST ? res.getString(R.string.menu_stop_daily_rest)
                        : res.getString(R.string.menu_start_daily_rest),
                (currentAction == Event.EVENT_TYPE_NORMAL_BREAK) ? res.getString(R.string.menu_stop_break)
                        : res.getString(R.string.menu_start_break)};
    }

    public static int getRestingSelectedOption(int currentAction) {
        if (currentAction == Event.EVENT_TYPE_WEEKLY_REST) {
            return 0;
        } else if (currentAction == Event.EVENT_TYPE_DAILY_REST) {
            return 1;
        } else {
            return 2;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            int type = getIntent().getIntExtra(INTENT_EXTRA_CURRENT_EVENT_TYPE, -1);

            showChooseRestTypeDialog(this, type, new MaterialDialog.ListCallback() {

                @Override
                public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                    if (which == 0) {
                        EventRecorder.INSTANCE.startRecordingEvent(Event.EVENT_TYPE_WEEKLY_REST, System.currentTimeMillis());
                    } else if (which == 1) {
                        EventRecorder.INSTANCE.startRecordingEvent(Event.EVENT_TYPE_DAILY_REST, System.currentTimeMillis());
                    } else if (which == 2) {
                        EventRecorder.INSTANCE.startRecordingEvent(Event.EVENT_TYPE_NORMAL_BREAK, System.currentTimeMillis());
                    }
                    finish();
                }
            });

        }
    }

}
