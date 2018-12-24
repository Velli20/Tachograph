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
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.velli20.materialunixgraph.LineGraph;
import com.velli20.tachograph.App;
import com.velli20.tachograph.DateUtils;
import com.velli20.tachograph.Event;
import com.velli20.tachograph.R;
import com.velli20.tachograph.location.GetAddressForLocationTask;
import com.velli20.tachograph.location.LoggedRoute;
import com.velli20.tachograph.views.RobotoLightTextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;


public class ListAdapterEventDetails extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int CARD_EVENT_DETAILS = 0;
    public static final int CARD_EVENT_LOGGED_ROUTE = 1;
    public static final int CARD_EVENT_SPEED_CHART = 2;

    private LayoutInflater mInflater;
    private Event mEvent;
    private LoggedRoute mRoute;
    private Resources mRes;
    private View.OnClickListener mListener;

    private RobotoLightTextView mStartLocation;
    private RobotoLightTextView mEndLocation;

    private int mColorBlue;
    private float mDensity;

    public ListAdapterEventDetails(Context context) {
        mInflater = LayoutInflater.from(context);
        mRes = context.getResources();

        mDensity = mRes.getDisplayMetrics().density;
        mColorBlue = mRes.getColor(R.color.color_primary);

    }

    private static String formatValueMileage(int value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator(' ');

        DecimalFormat formatter = new DecimalFormat("###,###.##", symbols);
        return formatter.format(value);
    }

    private static void setMapPolyLine(GoogleMap map, PolylineOptions line) {
        if (map != null) {
            map.addPolyline(line);

            final LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(line.getPoints().get(0));
            builder.include(line.getPoints().get(line.getPoints().size() - 1));
            LatLngBounds bounds = builder.build();

            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
        }
    }

    public void setEvent(Event event) {
        if (mEvent == null) {
            mEvent = event;
            notifyItemInserted(0);
        } else {
            mEvent = event;
            notifyItemChanged(0);
        }

    }

    public void setLoggedRoute(LoggedRoute route) {
        if (mRoute == null && route != null) {
            mRoute = route;
            if (mEndLocation != null) {
                loadEndAddressOverInternet(mEndLocation);
            }
            if (mStartLocation != null) {
                loadStartAddressOverInternet(mStartLocation);
            }
            notifyItemRangeInserted(1, 2);
        } else if (mRoute != null && route == null) {
            mRoute = route;
            notifyItemRangeRemoved(1, 2);
        } else {
            mRoute = route;
            notifyDataSetChanged();
        }
    }

    public void setOnClickListener(View.OnClickListener l) {
        mListener = l;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case CARD_EVENT_DETAILS:
                return new ViewHolderEventDetails(mInflater.inflate(R.layout.list_item_event_details, parent, false));
            case CARD_EVENT_LOGGED_ROUTE:
                return new ViewHolderMap(mInflater.inflate(R.layout.list_item_event_map, parent, false));
            case CARD_EVENT_SPEED_CHART:
                return new ViewHolderSpeedChart(mInflater.inflate(R.layout.list_item_event_speed_chart, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        if (viewType == CARD_EVENT_DETAILS) {
            setEventDetailsData((ViewHolderEventDetails) holder);
        } else if (viewType == CARD_EVENT_LOGGED_ROUTE) {
            setMapData((ViewHolderMap) holder);
        } else if (viewType == CARD_EVENT_SPEED_CHART) {
            setChartData((ViewHolderSpeedChart) holder);
        }
    }

    private void setEventDetailsData(ViewHolderEventDetails holder) {
        if (holder == null || mEvent == null) {
            return;
        }
        holder.mDate.setText(DateUtils.createDateTimeString(mEvent.getStartDateInMillis(), mEvent.getEndDateInMillis()));
        holder.mDuration.setText(DateUtils.convertDatesInHours(mEvent.getStartDateInMillis(), mEvent.getEndDateInMillis()));

        if ((mEvent.getStartLocation() != null && !mEvent.getStartLocation().isEmpty()) || mEvent.hasLoggedRoute()) {
            if (mEvent.getStartLocation() == null) {
                loadStartAddressOverInternet(holder.mStartLocation);
            } else {
                holder.mStartLocation.setText(mEvent.getStartLocation());
            }
            holder.mStartLocation.setVisibility(View.VISIBLE);
        } else {
            holder.mStartLocation.setVisibility(View.GONE);
        }

        if ((mEvent.getEndLocation() != null && !mEvent.getEndLocation().isEmpty()) || (mEvent.hasLoggedRoute() && !mEvent.isRecordingEvent())) {
            if (mEvent.getEndLocation() == null) {
                loadEndAddressOverInternet(holder.mEndLocation);
            } else {
                holder.mEndLocation.setText(mEvent.getEndLocation());
            }
            holder.mEndLocation.setVisibility(View.VISIBLE);
        } else {
            holder.mEndLocation.setVisibility(View.GONE);
        }

        if (mEvent.getNote() != null && !mEvent.getNote().isEmpty()) {
            holder.mNote.setText(mEvent.getNote());
            holder.mNote.setVisibility(View.VISIBLE);
        } else {
            holder.mNote.setVisibility(View.GONE);
        }

        if (mEvent.getMileageStart() == 0 || mEvent.getMileageEnd() == 0 || mEvent.getEventType() != Event.EVENT_TYPE_DRIVING) {
            holder.mMileage.setVisibility(View.GONE);
        } else {
            holder.mMileage.setVisibility(View.VISIBLE);
            holder.mMileage.setText(formatValueMileage(mEvent.getMileageStart()) + " km -\n" + formatValueMileage(mEvent.getMileageEnd()) + " km");
        }
    }

    private void loadStartAddressOverInternet(RobotoLightTextView startLocation) {
        if (startLocation != null) {
            startLocation.setText(R.string.title_loading_address);
        }

        if (mRoute != null) {
            new GetAddressForLocationTask(App.get(), null, mRoute.getStartLatitude(), mRoute.getStartLongitude())
                    .setTextViewToSet(startLocation, mRes.getString(R.string.title_address_not_available))
                    .execute();
        } else {
            mStartLocation = startLocation;
        }
    }

    private void loadEndAddressOverInternet(RobotoLightTextView endLocation) {
        if (endLocation != null) {
            endLocation.setText(R.string.title_loading_address);
        }
        if (mRoute != null) {
            new GetAddressForLocationTask(App.get(), null, mRoute.getEndLatitude(), mRoute.getEndLongitude())
                    .setTextViewToSet(endLocation, mRes.getString(R.string.title_address_not_available))
                    .execute();
        } else {
            mEndLocation = endLocation;
        }
    }

    private void setMapData(final ViewHolderMap holder) {
        if (holder == null) {
            return;
        }
        if (!holder.mMapReady && holder.mMapView != null) {
            holder.mMapView.setClickable(false);
            holder.mMapView.onCreate(null);
            holder.mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    holder.mMapReady = true;

                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);

                    if (mRoute != null && mRoute.getMapPolyline() != null) {
                        PolylineOptions line = mRoute.getMapPolyline();
                        line.color(mColorBlue);
                        line.width((int) mDensity * 2);
                        setMapPolyLine(googleMap, mRoute.getMapPolyline());
                    }
                }
            });
        }
    }

    private void setChartData(ViewHolderSpeedChart holder) {
        if (holder == null) {
            return;
        }
        if (mRoute != null && mRoute.getSpeedGraphLine() != null) {
            mRoute.getSpeedGraphLine().setFillLine(true);
            mRoute.getSpeedGraphLine().setFillAlpha(60);
            mRoute.getSpeedGraphLine().setLineStrokeWidth(2.5f);

            holder.mChart.setDrawUserTouchPointEnabled(false);
            holder.mChart.addLine(mRoute.getSpeedGraphLine());
            holder.mChart.setMaxVerticalAxisValue(140);
            holder.mChart.setUnitLabel(" km/h");
            holder.mAverageSpeed.setText(mRes.getString(R.string.title_average_speed) + " " + String.valueOf((int) mRoute.getAverageSpeed()) + " km/h");
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (mEvent != null) {
            count += 1;
        }
        if (mRoute != null) {
            count += 2;
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    private class ViewHolderEventDetails extends RecyclerView.ViewHolder {
        private RobotoLightTextView mDate;
        private RobotoLightTextView mDuration;
        private RobotoLightTextView mMileage;
        private RobotoLightTextView mStartLocation;
        private RobotoLightTextView mEndLocation;
        private RobotoLightTextView mNote;

        private ViewHolderEventDetails(View itemView) {
            super(itemView);

            mDate = (RobotoLightTextView) itemView.findViewById(R.id.list_item_event_details_date);
            mDuration = (RobotoLightTextView) itemView.findViewById(R.id.list_item_event_details_duration);
            mMileage = (RobotoLightTextView) itemView.findViewById(R.id.list_item_event_details_mileage);
            mStartLocation = (RobotoLightTextView) itemView.findViewById(R.id.list_item_event_details_start_location);
            mEndLocation = (RobotoLightTextView) itemView.findViewById(R.id.list_item_event_details_end_location);
            mNote = (RobotoLightTextView) itemView.findViewById(R.id.list_item_event_details_note);
        }
    }

    private class ViewHolderMap extends RecyclerView.ViewHolder {
        private MapView mMapView;
        private ImageButton mOverFlowButton;
        private boolean mMapReady;

        private ViewHolderMap(View itemView) {
            super(itemView);

            mMapView = (MapView) itemView.findViewById(R.id.list_item_event_map);
            mOverFlowButton = (ImageButton) itemView.findViewById(R.id.list_item_event_map_overflow_button);
            mOverFlowButton.setOnClickListener(new EventDetailsClickListener());
        }
    }

    private class ViewHolderSpeedChart extends RecyclerView.ViewHolder {
        private LineGraph mChart;
        private RobotoLightTextView mAverageSpeed;

        private ViewHolderSpeedChart(View itemView) {
            super(itemView);

            mChart = (LineGraph) itemView.findViewById(R.id.list_item_event_speed_chart);
            mAverageSpeed = (RobotoLightTextView) itemView.findViewById(R.id.list_item_event_speed_chart_average_speed);
        }
    }

    private class EventDetailsClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                mListener.onClick(view);
            }
        }
    }
}
