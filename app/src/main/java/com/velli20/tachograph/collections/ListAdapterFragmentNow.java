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

package com.velli20.tachograph.collections;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.velli20.tachograph.App;
import com.velli20.tachograph.CardsToShowInFragmentNow;
import com.velli20.tachograph.Event;
import com.velli20.tachograph.R;
import com.velli20.tachograph.views.ListItemActivityChooser;
import com.velli20.tachograph.views.ListItemGpsLogger;
import com.velli20.tachograph.views.ListItemRegulation;

import java.util.ArrayList;


public class ListAdapterFragmentNow extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_REGULATION = 1;
    public static final int VIEW_TYPE_ACTIVITY_CHOOSER = 2;
    public static final int VIEW_TYPE_GPS_STATUS = 3;
    public static final int VIEW_TYPE_ERROR = -1;
    private static final String Tag = "ListAdapterFragmentNow ";
    private final LayoutInflater mInflater;
    private float mCardElevation = 0;

    private ArrayList<ListItemRegulationToShow> mItemsToShow;
    private ListItemGpsLogger.OnPermissionRequestButtonClickedListener mPermissionRequestListener;

    private boolean mShowTimesCountDown = false;
    private boolean mShowBigActivityCards = true;

    private Event mCurrentEvent;
    private View.OnClickListener mClickListener;

    public ListAdapterFragmentNow(Context c, ArrayList<ListItemRegulationToShow> items,
                                  boolean showBigActivityCards, ListItemGpsLogger.OnPermissionRequestButtonClickedListener l) {
        mInflater = LayoutInflater.from(c);
        mCardElevation = (c.getResources().getDimension(R.dimen.card_elevation));
        mItemsToShow = items;
        mShowBigActivityCards = showBigActivityCards;
        mPermissionRequestListener = l;
        setHasStableIds(true);
    }


    @Override
    public int getItemCount() {
        if (mItemsToShow == null) {
            return 0;
        }
        return mItemsToShow.size();
    }


    @Override
    public long getItemId(int position) {
        if (mItemsToShow.get(position) != null) {
            return mItemsToShow.get(position).mId;
        }
        return VIEW_TYPE_ERROR;
    }

    public void setCurrentEvent(Event ev) {
        mCurrentEvent = ev;
        notifyItemChanged(0);
    }

    public void setShowBigActivityCards(boolean showBigCards) {
        mShowBigActivityCards = showBigCards;
        notifyItemRangeChanged(1, getItemCount() - 1, null);
    }

    public void setShowTimesCountDown(boolean showCountDown) {
        mShowTimesCountDown = showCountDown;
    }

    public void setItemsToShow(ArrayList<ListItemRegulationToShow> list) {
        mItemsToShow = list;
        notifyDataSetChanged();
    }

    public void setListeners(View.OnClickListener clickListener) {
        mClickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (mItemsToShow.get(position) != null) {
            return mItemsToShow.get(position).mViewType;
        } else {
            return VIEW_TYPE_ERROR;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);

        if (viewType == VIEW_TYPE_REGULATION) {
            ((ViewHolderWorkLimit) holder).mLimit.setShowCountDown(mShowTimesCountDown);
            ((ViewHolderWorkLimit) holder).mLimit.setType(mItemsToShow.get(position).mRegulationType);
            ((ViewHolderWorkLimit) holder).mLimit.showProgressInPieChart(mShowBigActivityCards);

        } else if (viewType == VIEW_TYPE_ACTIVITY_CHOOSER) {
            ((ViewHolderActivityChooser) holder).mActivityChooser.setEvent(mCurrentEvent);
            if (mClickListener != null) {
                ((ViewHolderActivityChooser) holder).mActivityChooser.setOnClickListener(mClickListener);
            }
        } else if (viewType == VIEW_TYPE_ERROR) {
            ((ViewHolderError) holder).mFix.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CardsToShowInFragmentNow.restoreDefaults(App.get().getApplicationContext());
                }
            });
        } else if (viewType == VIEW_TYPE_GPS_STATUS) {
            ((ListItemGpsLogger) (((ViewHolderGpsStatus) holder).itemView)).setOnPermissionRequestButtonClickedListener(mPermissionRequestListener);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
        final View itemView;

        if (viewType == VIEW_TYPE_REGULATION) {
            itemView = mInflater.inflate(R.layout.list_item_activity_progress, root, false);
            ((CardView) itemView).setCardElevation(mCardElevation);
            return new ViewHolderWorkLimit(itemView);
        } else if (viewType == VIEW_TYPE_ACTIVITY_CHOOSER) {
            itemView = mInflater.inflate(R.layout.list_item_activity_chooser, root, false);
            ((CardView) itemView).setCardElevation(mCardElevation);
            return new ViewHolderActivityChooser(itemView);
        } else if (viewType == VIEW_TYPE_GPS_STATUS) {
            itemView = mInflater.inflate(R.layout.list_item_gps_logger_status, root, false);
            ((CardView) itemView).setCardElevation(mCardElevation);
            return new ViewHolderGpsStatus(itemView);
        }

        return new ViewHolderError(mInflater.inflate(R.layout.list_item_error, root, false));
    }

    private class ViewHolderWorkLimit extends RecyclerView.ViewHolder {
        private ListItemRegulation mLimit;

        private ViewHolderWorkLimit(View itemView) {
            super(itemView);
            mLimit = (ListItemRegulation) itemView;
        }
    }

    private class ViewHolderActivityChooser extends RecyclerView.ViewHolder {
        private ListItemActivityChooser mActivityChooser;

        private ViewHolderActivityChooser(View itemView) {
            super(itemView);

            mActivityChooser = (ListItemActivityChooser) itemView;
        }
    }

    private class ViewHolderGpsStatus extends RecyclerView.ViewHolder {

        private ViewHolderGpsStatus(View itemView) {
            super(itemView);

        }
    }

    private class ViewHolderError extends RecyclerView.ViewHolder {
        private AppCompatButton mFix;

        private ViewHolderError(View itemView) {
            super(itemView);
            mFix = (AppCompatButton) itemView.findViewById(R.id.list_item_error_action_button);

        }
    }

}
