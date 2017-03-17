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

package com.velli.tachograph.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.velli.tachograph.views.DayView.AnimateEventRect;

public class AnimationUtils {
	public static final int PULSE_ANIMATOR_DURATION = 544;
	public static final int TRANSITION_ANIMATOR_DURATION = 300;

	public static ObjectAnimator getPulseAnimator(
			AnimateEventRect eventToAnimate, float decreaseRatio,
			float increaseRatio) {
		Keyframe k0 = Keyframe.ofFloat(0f, 1f);
		Keyframe k1 = Keyframe.ofFloat(0.275f, decreaseRatio);
		Keyframe k2 = Keyframe.ofFloat(0.69f, increaseRatio);
		Keyframe k3 = Keyframe.ofFloat(1f, 1f);

		PropertyValuesHolder scaleY = PropertyValuesHolder.ofKeyframe("scaleY",
				k0, k1, k2, k3);
		ObjectAnimator pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
				eventToAnimate, scaleY);
		pulseAnimator.setDuration(PULSE_ANIMATOR_DURATION);

		return pulseAnimator;
	}

	public static ObjectAnimator getEventProgressAnimator(
			AnimateEventRect eventToAnimate, float startProg, float finalProg) {
		PropertyValuesHolder progAnimHolder = PropertyValuesHolder.ofFloat(
				"progress", startProg, finalProg);

		ObjectAnimator progAnim = ObjectAnimator.ofPropertyValuesHolder(
				eventToAnimate, progAnimHolder);
		progAnim.setInterpolator(new AccelerateDecelerateInterpolator());
		progAnim.setDuration(TRANSITION_ANIMATOR_DURATION);
		return progAnim;
	}
	
	public static class HardwareAccelerateListener extends AnimatorListenerAdapter {
		private View mView;
		
	    public HardwareAccelerateListener(View v) {
	    	mView = v;
	    }

	    @Override
	    public void onAnimationStart(Animator animation) {
	        if (mView != null) {
	        	mView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
	        }
	    }

	    @Override
	    public void onAnimationEnd(Animator animation) {         
	        if (mView != null) {
	        	mView.setLayerType(View.LAYER_TYPE_NONE, null);
	        }
	        mView = null;
	    }
	}
}
