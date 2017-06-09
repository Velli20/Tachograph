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

package com.velli20.tachograph.filepicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.afollestad.materialdialogs.Theme;
import com.velli20.tachograph.R;
import com.velli20.tachograph.views.RobotoLightTextView;

import java.io.File;
import java.util.ArrayList;


public class FileNavigationSpinnerAdapter extends BaseAdapter {
    private final ArrayList<String> mPaths = new ArrayList<>();
    private final LayoutInflater mInflater;
    private final Resources mRes;

    private ColorStateList mTextColorDark;
    private ColorStateList mTextColorLight;

    private Theme mTheme;
    private Drawable mUp;

    public FileNavigationSpinnerAdapter(Context c, Theme theme) {
        mInflater = LayoutInflater.from(c);
        mRes = c.getResources();
        mTheme = theme;

        mTextColorDark = mRes.getColorStateList(R.color.dialog_timepicker_dark);
        mTextColorLight = mRes.getColorStateList(R.color.dialog_timepicker_light);

        mUp = mRes.getDrawable(R.drawable.ic_filepicker_return);
        mUp.setBounds(0, 0, mUp.getIntrinsicWidth(), mUp.getIntrinsicHeight());
        mUp.mutate();
        mUp.setColorFilter(mTheme == Theme.DARK ? mTextColorLight.getDefaultColor() : mTextColorDark.getDefaultColor(), PorterDuff.Mode.SRC_IN);
    }

    public void addPath(String path) {
        if(!mPaths.contains(path)) {
            mPaths.add(path);
            notifyDataSetChanged();
        }
    }

    public void removePathStartingAt(int index) {
        if(index < mPaths.size()) {
            for(int i = index; i < mPaths.size(); i++) {
                mPaths.remove(i);
            }

            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mPaths.size();
    }

    @Override
    public String getItem(int position) {
        return mPaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.nav_spinner_item, parent, false);

            holder = new ViewHolder();
            holder.mTitle = (RobotoLightTextView) convertView.findViewById(R.id.spinner_item_title);
            holder.mTitle.setTextColor(mTheme == Theme.DARK ? mTextColorLight : mTextColorDark);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(mPaths.get(position).equals(Environment.getExternalStorageDirectory().getPath())) {
            holder.mTitle.setText("External storage");
        } else {
            holder.mTitle.setText(new File(mPaths.get(position)).getName());
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolderDropDown holder;

        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.nav_spinner_item_dropdown, parent, false);

            holder = new ViewHolderDropDown();
            holder.mTitle = (RobotoLightTextView) convertView.findViewById(R.id.spinner_item_title);
            holder.mTitle.setTextColor(mTheme == Theme.DARK ? mTextColorLight : mTextColorDark);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolderDropDown) convertView.getTag();
        }

        if(mPaths.get(position).equals(Environment.getExternalStorageDirectory().getPath())) {
            holder.mTitle.setText("External storage");
        } else {
            holder.mTitle.setText(new File(mPaths.get(position)).getName());
        }
        holder.mTitle.setCompoundDrawables(position == 0 ? null : mUp, null, null, null);
        return convertView;
    }

    class ViewHolder {
        RobotoLightTextView mTitle;
    }

    class ViewHolderDropDown {
        RobotoLightTextView mTitle;
    }
}
