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

package com.velli.tachograph.preferences;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.text.TextUtils.isEmpty;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;


import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.velli.tachograph.R;
import com.velli.tachograph.views.RobotoLightTextView;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class CustomListPreference extends ListPreference {
	private Context context;
	private int mIconResId;
	private Drawable mIcon;

	public RobotoLightTextView mSummary;

	public CustomListPreference(Context context) {
		super(context);
		this.context = context;
	}

	public CustomListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	@Override
	protected void showDialog(Bundle state) {
		int preselect = findIndexOfValue(getValue());
		MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
				.title(getDialogTitle())
				.content(getDialogMessage())
				.icon(getDialogIcon())
				.theme(Theme.DARK)
				.negativeText(getNegativeButtonText())
				.items(getEntries())
				.itemsCallbackSingleChoice(preselect,
						new MaterialDialog.ListCallbackSingleChoice() {
							@Override
							public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
								onClick(null, DialogInterface.BUTTON_POSITIVE);
								dialog.dismiss();
								if (which >= 0 && getEntryValues() != null) {
									String value = getEntryValues()[which].toString();
									if (callChangeListener(value)
											&& isPersistent())
										setValue(value);
								}
                                return true;
							}

							
						});
		final View contentView = onCreateDialogView();
		if (contentView != null) {
			onBindDialogView(contentView);
			builder.customView(contentView, false);
		} else {
			builder.content(getDialogMessage());
		}
		builder.show();
	}
	
	@Override
	protected View onCreateView(ViewGroup parent) {
		super.onCreateView(parent);
		final LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = layoutInflater.inflate(R.layout.view_preference, parent, false);
		final ViewGroup widgetFrame = (ViewGroup) layout.findViewById(R.id.widget_frame);
		int widgetLayoutResId = getWidgetLayoutResource();
		
		if (widgetLayoutResId != 0) {
			layoutInflater.inflate(widgetLayoutResId, widgetFrame);
		}
		
		widgetFrame.setVisibility(widgetLayoutResId != 0 ? VISIBLE : GONE);
		return layout;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		final CharSequence title = getTitle();
		final CharSequence summary = getSummary();
		final View imageFrame = view.findViewById(R.id.icon_frame);

		RobotoLightTextView mTitle = (RobotoLightTextView) view.findViewById(R.id.title);
		mTitle.setText(title);
		mTitle.setVisibility(!isEmpty(title) ? VISIBLE : GONE);
		
		mSummary = (RobotoLightTextView) view.findViewById(R.id.summary);
		mSummary.setText(summary);
		mSummary.setVisibility(!isEmpty(summary) ? VISIBLE : GONE);
		if (mIcon == null && mIconResId > 0) {
			mIcon = getContext().getResources().getDrawable(mIconResId);
		}
		ImageView mImage = (ImageView) view.findViewById(R.id.icon);
		mImage.setImageDrawable(mIcon);
		mImage.setVisibility(mIcon != null ? VISIBLE : GONE);
		
		imageFrame.setVisibility(mIcon != null ? VISIBLE : GONE);
	}
	
	@Override
	public void setIcon(int iconResId) {
		super.setIcon(iconResId);
		this.mIconResId = iconResId;
	}

	@Override
	public void setIcon(Drawable icon) {
		super.setIcon(icon);
		this.mIcon = icon;
	}
}
