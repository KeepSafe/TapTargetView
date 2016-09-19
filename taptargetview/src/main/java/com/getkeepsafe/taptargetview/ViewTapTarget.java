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

import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.view.View;

public class ViewTapTarget extends TapTarget {
    final View view;

    protected ViewTapTarget(View view, CharSequence title, @Nullable CharSequence description) {
        super(title, description);
        this.view = view;
    }

    @Override
    public void onReady(final Runnable runnable) {
        ViewUtil.onLaidOut(view, new Runnable() {
            @Override
            public void run() {
                // Cache bounds
                final int[] location = new int[2];
                view.getLocationOnScreen(location);
                bounds = new Rect(location[0], location[1],
                        location[0] + view.getWidth(), location[1] + view.getHeight());

                view.setDrawingCacheEnabled(true);
                icon = new BitmapDrawable(view.getContext().getResources(), view.getDrawingCache());
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());

                runnable.run();
            }
        });
    }
}
