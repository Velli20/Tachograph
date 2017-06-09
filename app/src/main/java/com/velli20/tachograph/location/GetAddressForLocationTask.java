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

package com.velli20.tachograph.location;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.TextView;

public class GetAddressForLocationTask extends AsyncTask<Void, Void, Address>{
	private WeakReference<OnGetAddressListener> mListener;
	private double mLatitude;
	private double mLongitude;
	private WeakReference<Context> mContext;

    private WeakReference<TextView> mTextToSet;
    private String mDefaultText;
	
	public interface OnGetAddressListener {
		void onAddressReceived(String address);
	}
	
	public GetAddressForLocationTask(Context context, OnGetAddressListener l, double latitude, double longitude){
		mListener = new WeakReference<>(l);
		mLatitude = latitude;
		mLongitude = longitude;
		mContext = new WeakReference<>(context);
	}

    public GetAddressForLocationTask setOnGetAddressListener(OnGetAddressListener l) {
        mListener = new WeakReference<>(l);
        return this;
    }

    public GetAddressForLocationTask setTextViewToSet(TextView textView, String defaultText) {
        mTextToSet = new WeakReference<>(textView);
        mDefaultText = defaultText;

        return this;
    }
	
	@Override
	protected Address doInBackground(Void... params) {
		if(mContext.get() == null){
			return null;
		}
		
		try {

	        Geocoder geo = new Geocoder(mContext.get(), Locale.getDefault());
	        List<Address> addresses = geo.getFromLocation(mLatitude, mLongitude, 1);
	        if (addresses.isEmpty()) {
	        	return null;
	        }
	        else {
	            if(addresses.size() > 0) {
	            	return addresses.get(0);
	            }
	        }
	    }
	    catch (Exception e) {}
		return null;
	}
	
	@Override
	protected void onPostExecute(Address address){
		if(mListener.get() != null){
			if(address == null){
				mListener.get().onAddressReceived(null);
			} else {
				mListener.get().onAddressReceived(address.getAddressLine(1));
			}
		}
        if(mTextToSet.get() != null) {
            if(address == null || address.getAddressLine(1) == null || address.getAddressLine(1).isEmpty()) {
                mTextToSet.get().setText(mDefaultText);
            } else {
                mTextToSet.get().setText(address.getAddressLine(1));
            }
        }
	}
	
}
