/**
 * Copyright 2016 Keepsafe Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.getkeepsafe.taptargetview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;

/** A small wrapper around {@link ValueAnimator} to provide a builder-like interface */
final class FloatValueAnimatorBuilder {
  final ValueAnimator animator;
  EndListener endListener;
  StartListener startListener;

  interface UpdateListener {
    void onUpdate(float lerpTime);
  }

  interface EndListener {
    void onEnd();
  }

  interface StartListener {
    void onStart();
  }

  protected FloatValueAnimatorBuilder() {
    this(false);
  }

  protected FloatValueAnimatorBuilder(boolean reverse) {
    if (reverse) {
      this.animator = ValueAnimator.ofFloat(1.0f, 0.0f);
    } else {
      this.animator = ValueAnimator.ofFloat(0.0f, 1.0f);
    }
  }

  public FloatValueAnimatorBuilder delayBy(long millis) {
    animator.setStartDelay(millis);
    return this;
  }

  public FloatValueAnimatorBuilder duration(long millis) {
    animator.setDuration(millis);
    return this;
  }

  public FloatValueAnimatorBuilder interpolator(TimeInterpolator lerper) {
    animator.setInterpolator(lerper);
    return this;
  }

  public FloatValueAnimatorBuilder repeat(int times) {
    animator.setRepeatCount(times);
    return this;
  }

  public FloatValueAnimatorBuilder onUpdate(final UpdateListener listener) {
    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        listener.onUpdate((float) animation.getAnimatedValue());
      }
    });
    return this;
  }

  public FloatValueAnimatorBuilder onStart(StartListener listener) {
    this.startListener = listener;
    return this;
  }

  public FloatValueAnimatorBuilder onEnd(EndListener listener) {
    this.endListener = listener;
    return this;
  }

  public ValueAnimator build() {
    animator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationStart(Animator animator) {
        if (startListener != null) {
          startListener.onStart();
        }
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        if (endListener != null) {
          endListener.onEnd();
        }
      }
    });

    return animator;
  }
}
