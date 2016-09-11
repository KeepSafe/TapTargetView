/**
 * Copyright 2016 Keepsafe Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.getkeepsafe.taptargetview;

import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

class ViewUtil {
    ViewUtil() {}

    /** Returns whether or not the view has been laid out **/
    static boolean isLaidOut(View view) {
        if (Build.VERSION.SDK_INT >= 19) {
            return view.isLaidOut();
        }

        return view.getWidth() > 0 && view.getHeight() > 0;
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

                if (Build.VERSION.SDK_INT >= 16) {
                    trueObserver.removeOnGlobalLayoutListener(this);
                } else {
                    //noinspection deprecation
                    trueObserver.removeGlobalOnLayoutListener(this);
                }

                runnable.run();
            }
        });
    }
}
