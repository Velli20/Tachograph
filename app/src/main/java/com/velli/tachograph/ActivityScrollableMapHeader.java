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

import java.util.ArrayList;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class ActivityScrollableMapHeader extends AppCompatActivity implements ObservableScrollViewCallbacks, OnMapReadyCallback {
	
	private MapView mMapView;
    private GoogleMap mMap;
    private Toolbar mToolbar;
    
    private View mOverlayView;
    private View mToolbarShadow;
    private View mPaddingView;
    private TextView mTitleView;
    private ObservableScrollView mScrollView;
    
    
    private int mActionBarSize;
    private int mActionBarHeight;
    private int mFlexibleSpaceImageHeight;
    private int mOverlayColor;
    private int mTextColorExpanded;
    private int mTextColor;
    private int mCurrentMenuItemTintColor;
    
    private boolean mMapVisible = true;
    private ArrayList<MenuItem> mMenuItems = new ArrayList<>();
    private PolylineOptions mLine;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrollable_map_header);
        MapsInitializer.initialize(this);
        
        final Resources res = getResources();
        mFlexibleSpaceImageHeight = res.getDimensionPixelSize(R.dimen.toolbar_title_conatiner_height);
        mActionBarHeight = res.getDimensionPixelSize(R.dimen.toolbar_height);
        
        mTextColorExpanded = Color.GRAY;
        mTextColor = Color.WHITE;
        
        mActionBarSize = getActionBarSize();

        mScrollView = (ObservableScrollView) findViewById(R.id.activity_scrollable_map_header_scrollview);
        mScrollView.setScrollViewCallbacks(this);
        
        
        mMapView = (MapView)findViewById(R.id.activity_scrollable_map_header_map);
		mMapView.setClickable(true);
		mMapView.onCreate(null);
		mMapView.getMapAsync(this);
		
        mOverlayView = findViewById(R.id.activity_scrollable_map_header_overlay);
        mToolbarShadow = findViewById(R.id.activity_scrollable_map_header_toolbar_shadow);
        mPaddingView = findViewById(R.id.activity_scrollable_map_header_blank_view);
        
        mTitleView = (TextView) findViewById(R.id.activity_scrollable_map_header_title);
        mTitleView.setText(getTitle());
        
        mToolbar = (Toolbar) findViewById(R.id.activity_scrollable_map_header_toolbar);
        setSupportActionBar(mToolbar);
        
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        
        if(mMapVisible) {
        	onScrollChanged(0, true, false);
        }
    }

    
    public void addMenuItemToTint(MenuItem itemToTint) {
    	if(!mMenuItems.contains(itemToTint)) {
    		mMenuItems.add(itemToTint);
    		setMenuItemTint(itemToTint, mCurrentMenuItemTintColor);
    	}
    }
    
    
	@Override
	public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
		if(!mMapVisible) {
			return;
		}
		// Translate overlay and image
        float flexibleRange = mFlexibleSpaceImageHeight - mActionBarSize;
        int minOverlayTransitionY = mActionBarSize - mOverlayView.getHeight();
        float overlayTranslation = ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0);
        
        mOverlayView.setTranslationY(overlayTranslation);
        mToolbarShadow.setTranslationY(overlayTranslation);
        mMapView.setTranslationY(ScrollUtils.getFloat(-scrollY / 2, minOverlayTransitionY, 0));

        // Change alpha of overlay
        setOverlayAlpha(flexibleRange, scrollY);
        setMenuItemsTint(scrollY, flexibleRange);
       

        // Translate title text
        mTitleView.setTranslationY(ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0) + (mFlexibleSpaceImageHeight - mActionBarHeight));
     
		
	}

	@Override
	public void onDownMotionEvent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpOrCancelMotionEvent(ScrollState scrollState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		mMap.getUiSettings().setMapToolbarEnabled(false);
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		
		if(mLine != null) {
			setMapPolyLine(mLine);
		}
	}
	

	private  int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }


	public void setContent(int resource) {
		setContent(View.inflate(this, resource, null));
	}
	
	public void setContent(View v) {
    	if(v != null) {
    		v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    		((LinearLayout)findViewById(R.id.activity_scrollable_map_header_content)).addView(v);
    	}
    }
    
    public void setTitle(String title) {
    	mTitleView.setText(title);
    }
    
    public void setOverlayColor(int color) {
    	mOverlayColor = color;
    	
    	if(mMapVisible) {
    		float flexibleRange = mFlexibleSpaceImageHeight - mActionBarSize;
    	    setOverlayAlpha(flexibleRange, mScrollView.getScrollY());
    	} else {
    		mOverlayView.setBackgroundColor(mOverlayColor);
    	}
    }
    
    public void setOverlayAlpha(float range, float scrollY) {
       	float alpha = ScrollUtils.getFloat(scrollY / range, 0, 1);
    	
        mOverlayView.setBackgroundColor(adjustAlpha(mOverlayColor, alpha));
    }
    
    public void setMapPolyLine(PolylineOptions line) {
    	if(mMap != null) {
    		mMap.addPolyline(line);
    		
    		final LatLngBounds.Builder builder = new LatLngBounds.Builder();
    		builder.include(line.getPoints().get(0));
    		builder.include(line.getPoints().get(line.getPoints().size() - 1));
    		LatLngBounds bounds = builder.build();
    		
    		mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
    	} else {
    		mLine = line;
    	}
    }
    
    public void setMapVisibility(boolean visible) {
    	mMapVisible = visible;
    	
    	mMapView.setVisibility(visible ? View.VISIBLE : View.GONE);
    	mPaddingView.setVisibility(visible ? View.VISIBLE : View.GONE);
    	if(visible) {
    		setOverlayColor(mOverlayColor);
    		setMenuItemsTint(Color.DKGRAY);
    		
    		mOverlayView.setTranslationY(0);
    		mToolbarShadow.setTranslationY(0);
    		mScrollView.setTranslationY(0);
    	} else {
    		mOverlayView.setBackgroundColor(mOverlayColor);
    		setMenuItemsTint(Color.WHITE);
    		mOverlayView.setTranslationY(mActionBarSize - mFlexibleSpaceImageHeight);
    		mToolbarShadow.setTranslationY(mActionBarSize - mFlexibleSpaceImageHeight);
    		mScrollView.setTranslationY(mActionBarSize);
    		mTitleView.setTranslationY(0);
    	}
    	
    	
    }
    
    private void setMenuItemsTint(float scrollY, float range) {
    	float ratio = ScrollUtils.getFloat(scrollY / range, 0, 1);
    	int tint = blendColors(mTextColor, mTextColorExpanded, ratio);
    	setMenuItemsTint(tint);
    	
    }
    
    private void setMenuItemsTint(int tint) {  
    	mCurrentMenuItemTintColor = tint;
    	mTitleView.setTextColor(tint);
    	
    	Drawable upArrow;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material, null);
		} else {
			upArrow =  getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
		}

    	
    	if(upArrow != null && getSupportActionBar() != null) {
    		upArrow.mutate();
    		upArrow.setColorFilter(tint, Mode.SRC_IN);
    		getSupportActionBar().setHomeAsUpIndicator(upArrow); 
    	}
    	
    	for(MenuItem item : mMenuItems) {
    		setMenuItemTint(item, tint);
    	}
    }
    
    private void setMenuItemTint(MenuItem item, int tint) {
    	if (item != null) {
			Drawable drawable = item.getIcon();
			if (drawable != null) {
				drawable.mutate();
				drawable.setColorFilter(tint, Mode.SRC_ATOP);
			}
		}
    }
    
    public int adjustAlpha(int color, float factor) {
	    int alpha = Math.round(Color.alpha(color) * factor);
	    int red = Color.red(color);
	    int green = Color.green(color);
	    int blue = Color.blue(color);
	    return Color.argb(alpha, red, green, blue);
	}
	
	private static int blendColors(int color1, int color2, float ratio) {
	    final float inverseRation = 1f - ratio;
	    float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
	    float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
	    float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
	    return Color.rgb((int) r, (int) g, (int) b);
	}

}
