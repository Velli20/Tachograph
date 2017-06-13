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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.afollestad.materialdialogs.MaterialDialog;
import com.velli20.tachograph.ExportEvents.OnFileSavedListener;
import com.velli20.tachograph.collections.ListAdapterFragmentLog;
import com.velli20.tachograph.collections.ListItemLogGroup;
import com.velli20.tachograph.collections.ListAdapterFragmentLog.OnEventSelectedListener;
import com.velli20.tachograph.database.DataBaseHandler;
import com.velli20.tachograph.database.DataBaseHandlerConstants;
import com.velli20.tachograph.database.DataBaseHandler.OnDatabaseEditedListener;
import com.velli20.tachograph.database.DataBaseHandler.OnGetSortedLogListener;
import com.velli20.tachograph.database.DatabaseEventQueryBuilder;
import com.velli20.tachograph.filepicker.ActivityFilePicker;
import com.velli20.tachograph.views.ListCircle;
import com.velli20.tachograph.views.RobotoLightTextView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

public class FragmentLog extends Fragment implements OnChildClickListener, OnDatabaseEditedListener, OnClickListener, OnEventSelectedListener, OnGetSortedLogListener, OnItemLongClickListener, OnFileSavedListener, OnSharedPreferenceChangeListener {
    public static final String tag = "LogFragment ";
    public static final String KEY_DELETED_ROW_ID = "deleted rowid";
    public static final int REQUEST_CODE_DELETE = 40;
    public static final int REQUEST_CODE_EXPORT = 50;

    private static final String BUNDLE_KEY_LIST_STATE = "list state";
    private static final String BUNDLE_KEY_LIST_FIRST_VISIBLE_POS = "list first visible pos";
    private static final String BUNDLE_KEY_LIST_FIRST_VISIBLE_POS_TOP = "list first visible pos top";
    private static final String BUNDLE_KEY_IS_LIST_ITEMS_SELECTED = "is list items selected";

    private ExpandableListView mLogListView;
    private ContextualToolbarCallback mContextualCallback;
    private RobotoLightTextView mNoItemsText;
    private ActionMode mActionMode;

    private Parcelable mLogListState;

    private int mSortBy = 2;
    private int mPendingRowIds[];

    private ListAdapterFragmentLog mListAdapter;

    private boolean mSortAscending = false;
    private boolean mShowDrivingEvents = true;
    private boolean mShowRestingEvents = true;
    private boolean mShowOtherWorkEvents = true;
    private boolean mShowPoaEvents = true;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            boolean listItemsSelected = savedInstanceState.getBoolean(BUNDLE_KEY_IS_LIST_ITEMS_SELECTED, false);
            int firstVisiblePosition = savedInstanceState.getInt(BUNDLE_KEY_LIST_FIRST_VISIBLE_POS, -1);
            int firstVisiblePositionTop = savedInstanceState.getInt(BUNDLE_KEY_LIST_FIRST_VISIBLE_POS_TOP, 0);

            if (listItemsSelected && mListAdapter != null) {
                mContextualCallback = new ContextualToolbarCallback();
                mActionMode = ((ActivityMain) getActivity()).getToolbar().startActionMode(mContextualCallback);
            }

            if (firstVisiblePosition != -1) {
                mLogListView.setSelectionFromTop(firstVisiblePosition, firstVisiblePositionTop);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mSortBy = Integer.parseInt(prefs.getString(getString(R.string.preference_key_log_order_day_week_month), "2"));
        mSortAscending = prefs.getBoolean(getString(R.string.preference_key_log_order_asc_desc), false);
        prefs.registerOnSharedPreferenceChangeListener(this);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View mView = inflater.inflate(R.layout.fragment_event_log, container, false);

        mLogListView = (ExpandableListView) mView.findViewById(R.id.log_fragment_list_view);
        mLogListView.setOnChildClickListener(this);
        mLogListView.setOnItemLongClickListener(this);
        mLogListView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);

        FloatingActionButton mFab = (FloatingActionButton) mView.findViewById(R.id.add_new_event_fab);
        mFab.setOnClickListener(this);

        mNoItemsText = (RobotoLightTextView) mView.findViewById(R.id.working_days_fragment_no_items_text);
        if (savedInstanceState != null) {
            mLogListState = savedInstanceState.getParcelable(BUNDLE_KEY_LIST_STATE);
        }

        if (mListAdapter != null) {
            mLogListView.setAdapter(mListAdapter);
            mListAdapter.setOnEventSelectedListener(this);

            if (!mListAdapter.getListItems().isEmpty()) {
                mNoItemsText.setVisibility(View.GONE);
            }

            if (mLogListState != null) {
                mLogListView.onRestoreInstanceState(mLogListState);
            }

        } else {
            mListAdapter = null;
            getEventsByCategory();
        }
        DataBaseHandler.getInstance().registerOnDatabaseEditedListener(this);

        return mView;
    }


    @Override
    public void onDatabaseEdited(int action, int rowId) {
        getEventsByCategory();
    }

    public void getEventsByCategory() {

        if (!mShowDrivingEvents && !mShowRestingEvents && !mShowOtherWorkEvents && !mShowPoaEvents) {
            onTaskCompleted(null);
        } else {
            DatabaseEventQueryBuilder query = new DatabaseEventQueryBuilder();
            query.fromTable(DataBaseHandlerConstants.TABLE_EVENTS)
                    .selectAllColumns()
                    .orderByKey(DataBaseHandlerConstants.KEY_EVENT_START_DATE, mSortAscending);


            if (mShowDrivingEvents) {
                query.whereEventTypeIs(Event.EVENT_TYPE_DRIVING);
            }
            if (mShowRestingEvents) {
                query.whereEventTypeIs(Event.EVENT_TYPE_NORMAL_BREAK);
                query.whereEventTypeIs(Event.EVENT_TYPE_WEEKLY_REST);
                query.whereEventTypeIs(Event.EVENT_TYPE_DAILY_REST);
            }
            if (mShowOtherWorkEvents) {
                query.whereEventTypeIs(Event.EVENT_TYPE_OTHER_WORK);
            }
            if (mShowPoaEvents) {
                query.whereEventTypeIs(Event.EVENT_TYPE_POA);
            }

            DataBaseHandler.getInstance().getSortedLog(query.buildQuery(), getResources().getStringArray(R.array.months),
                    getString(R.string.title_week), mSortBy, true, this);
        }

    }

    private void sortBy() {
        new MaterialDialog.Builder(getActivity())
                .items(getResources().getStringArray(R.array.preference_entries_sort_by_asc_desc))
                .title(getResources().getString(R.string.menu_sort))
                .positiveText(getResources().getString(R.string.action_ok))
                .negativeText(getResources().getString(R.string.action_cancel))
                .itemsCallbackSingleChoice(mSortAscending ? 1 : 0, new MaterialDialog.ListCallbackSingleChoice() {

                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        prefs.edit().putBoolean(getResources().getString(R.string.preference_key_log_order_asc_desc), which == 1).apply();
                        return true;
                    }
                }).show();
    }

    @Override
    public void onTaskCompleted(ArrayList<ListItemLogGroup> list) {
        mNoItemsText.setVisibility((list == null || list.isEmpty()) ? View.VISIBLE : View.GONE);
        if (mListAdapter == null) {
            mListAdapter = new ListAdapterFragmentLog(getActivity(), list);
            mListAdapter.setOnEventSelectedListener(FragmentLog.this);
            mLogListView.setAdapter(mListAdapter);
        } else {
            mListAdapter.setItemsList(list);
            if (mActionMode != null) {
                mActionMode.invalidate();
            }
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_log, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mLogListState = mLogListView.onSaveInstanceState();
        DataBaseHandler.getInstance().unregisterOnDatabaseEditedListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_new_event_fab) {
            Intent intent = new Intent(getActivity(), ActivityAddNewEvent.class);
            intent.putExtra(ActivityAddNewEvent.INTENT_EXTRA_DISPLAY_MODE, ActivityAddNewEvent.DISPLAY_MODE_CREATE_NEW);
            getActivity().startActivity(intent);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_sort_by_category_drive:
                mShowDrivingEvents = !mShowDrivingEvents;
                item.setChecked(mShowDrivingEvents);
                getEventsByCategory();
                return true;
            case R.id.action_sort_by_category_other_work:
                mShowOtherWorkEvents = !mShowOtherWorkEvents;
                item.setChecked(mShowOtherWorkEvents);
                getEventsByCategory();
                return true;
            case R.id.action_sort_by_category_poa:
                mShowPoaEvents = !mShowPoaEvents;
                item.setChecked(mShowPoaEvents);
                getEventsByCategory();
                return true;
            case R.id.action_sort_by_category_rest:
                mShowRestingEvents = !mShowRestingEvents;
                item.setChecked(mShowRestingEvents);
                getEventsByCategory();
                return true;

            case R.id.menu_sort:
                sortBy();
                return true;
        }

        return false;
    }


    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Event ev = mListAdapter.getListItems().get(groupPosition).getChildList().get(childPosition);

        Intent intent = new Intent(getActivity(), ev.getRowId() == -1 ? ActivityAddNewEvent.class : ActivityEventDetails.class);
        if (ev.getRowId() == -1) {
            intent.putExtra(ActivityAddNewEvent.INTENT_EXTRA_START_TIME, ev.getStartDateInMillis());
            intent.putExtra(ActivityAddNewEvent.INTENT_EXTRA_END_TIME, ev.getEndDateInMillis());
            intent.putExtra(ActivityAddNewEvent.INTENT_EXTRA_EVENT_TYPE, ev.getEventType());
            intent.putExtra(ActivityAddNewEvent.INTENT_EXTRA_DISPLAY_MODE, ActivityAddNewEvent.DISPLAY_MODE_CREATE_NEW);
        } else {
            intent.putExtra(ActivityEventDetails.INTENT_EXTRA_EVENT_ID, ev.getRowId());
        }
        startActivity(intent);
        return false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            if (view != null) {
                ListCircle lc = (ListCircle) view.findViewById(R.id.list_item_log_child_icon);

                if (lc != null) {
                    if (lc.isSelectable()) {
                        lc.performClick();
                        return true;
                    }
                }
            }


        }
        return false;
    }

    @Override
    public void onEventSelected(int groupPosition, int childPosition, int dbRowID, boolean selected) {

        if (mContextualCallback == null || mActionMode == null) {
            mContextualCallback = new ContextualToolbarCallback();
            mActionMode = ((ActivityMain) getActivity()).getToolbar().startActionMode(mContextualCallback);
        } else if (!selected && mListAdapter.getSelectedItems().size() == 0 && mActionMode != null) {
            mActionMode.finish();
        } else {
            mActionMode.invalidate();
        }
        //long position = ExpandableListView.getPackedPositionForChild(groupPosition, childPosition);
        //mLogListView.setItemChecked(mLogListView.getFlatListPosition(position), selected);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_DELETE && resultCode == Activity.RESULT_OK && data != null) {

            int rowid = data.getIntExtra(KEY_DELETED_ROW_ID, -1);
            if (mActionMode != null) {
                mListAdapter.getSelectedItems().remove((Object) rowid);
                mListAdapter.notifyDataSetChanged();

                if (mListAdapter.getSelectedItems().size() == 0) {
                    mActionMode.finish();
                } else {
                    mActionMode.invalidate();
                }
            }
        } else if (requestCode == REQUEST_CODE_EXPORT && resultCode == Activity.RESULT_OK) {

            if (data != null && mPendingRowIds != null) {
                String uri = data.getStringExtra(ActivityFilePicker.INTENT_EXTRA_FILEPATH);
                if (uri != null) {
                    File file = new File(uri);

                    ExportEvents expotevents = new ExportEvents();
                    expotevents.setFile(file);
                    expotevents.setOnFileSavedListener(this);
                    expotevents.write(getActivity().getApplicationContext(), mPendingRowIds);
                }
            }
        }
    }

    private class ContextualToolbarCallback implements ActionMode.Callback {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_item_discard) {
                DataBaseHandler.getInstance().deleteEvents(new ArrayList<>(mListAdapter.getSelectedItems()));
                mActionMode.finish();
            } else if (item.getItemId() == R.id.menu_item_export) {
                mPendingRowIds = convertIntegers(new ArrayList<>(mListAdapter.getSelectedItems()));
                mActionMode.finish();

                Intent i = new Intent(getActivity(), ActivityFilePicker.class);
                i.putExtra(ActivityFilePicker.INTENT_EXTRA_FILE_EXTENSION, ".xls");
                i.putExtra(ActivityFilePicker.INTENT_EXTRA_MODE, ActivityFilePicker.MODE_CREATE_FILE);
                i.putExtra(ActivityFilePicker.INTENT_EXTRA_FILENAME, getString(R.string.app_name) + "_" + DateUtils.getFileDateName());

                startActivityForResult(i, REQUEST_CODE_EXPORT);
            }
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_fragment_log_contextual, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode arg0) {
            mLogListView.clearChoices();
            if (mListAdapter.getSelectedItems().size() > 0) {
                mListAdapter.deselectAllRows();
            }
            mContextualCallback = null;
            mActionMode = null;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu arg1) {
            mode.setTitle(String.valueOf(mListAdapter.getSelectedItems().size()));
            return false;
        }

        public int[] convertIntegers(List<Integer> integers) {
            int[] ret = new int[integers.size()];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = integers.get(i);
            }
            return ret;
        }


    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mLogListView != null) {
            View v = mLogListView.getChildAt(0);

            outState.putParcelable(BUNDLE_KEY_LIST_STATE, mLogListView.onSaveInstanceState());
            outState.putInt(BUNDLE_KEY_LIST_FIRST_VISIBLE_POS, mLogListView.getFirstVisiblePosition());
            outState.putInt(BUNDLE_KEY_LIST_FIRST_VISIBLE_POS_TOP, (v == null) ? 0 : (v.getTop()));

            if (mActionMode != null && !mListAdapter.getSelectedItems().isEmpty()) {
                outState.putBoolean(BUNDLE_KEY_IS_LIST_ITEMS_SELECTED, true);
            }
        }
    }


    @Override
    public void onFileSaved(File file) {
        if (file.exists()) {
            FragmentSettings.createFileSavedNotification(getActivity(), file);
        }

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mSortBy = Integer.parseInt(sharedPreferences.getString(getResources().getString(R.string.preference_key_log_order_day_week_month), "2"));
        mSortAscending = sharedPreferences.getBoolean(getResources().getString(R.string.preference_key_log_order_asc_desc), false);

        getEventsByCategory();
    }


}
