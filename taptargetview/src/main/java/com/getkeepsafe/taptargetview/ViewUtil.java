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

import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewManager;
import android.view.ViewTreeObserver;

final class ViewUtil {
  ViewUtil() {
  }

  /** Returns whether or not the view has been laid out **/
  static boolean isLaidOut(View view) {
    return ViewCompat.isLaidOut(view) && view.getWidth() > 0 && view.getHeight() > 0;
  }

  /** Executes the given {@link java.lang.Runnable} when the view is laid out **/
  static void onLaidOut(final View view, final Runnable runnable) {
    if (isLaidOut(view)) {
      runnable.run();
      return;
    }

    final ViewTreeObserver observer = view.getViewTreeObserver();
    observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        final ViewTreeObserver trueObserver;

        if (observer.isAlive()) {
          trueObserver = observer;
        } else {
          trueObserver = view.getViewTreeObserver();
        }

        removeOnGlobalLayoutListener(trueObserver, this);

        runnable.run();
      }
    });
  }

  @SuppressWarnings("deprecation")
  static void removeOnGlobalLayoutListener(
      ViewTreeObserver observer, ViewTreeObserver.OnGlobalLayoutListener listener) {
    if (Build.VERSION.SDK_INT >= 16) {
      observer.removeOnGlobalLayoutListener(listener);
    } else {
      observer.removeGlobalOnLayoutListener(listener);
    }
  }

  static void removeView(ViewManager parent, View child) {
    if (parent == null || child == null) {
      return;
    }

    try {
      parent.removeView(child);
    } catch (Exception ignored) {
      // This catch exists for modified versions of Android that have a buggy ViewGroup
      // implementation. See b.android.com/77639, #121 and #49
    }
  }
}
