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
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.view.View;

public class TapTarget {
    final String title;
    final String description;

    Rect bounds;
    Drawable icon;
    Typeface typeface;

    @ColorRes int outerCircleColor = -1;
    @ColorRes int targetCircleColor = -1;
    @ColorRes int dimColor = -1;
    @ColorRes int textColor = -1;

    boolean drawShadow = true;
    boolean cancelable = true;
    boolean tintTarget = true;

    public static ViewTapTarget forView(View view, String title, String description) {
        return new ViewTapTarget(view, title, description);
    }

    public static TapTarget forBounds(Rect bounds, String title, String description) {
        return new TapTarget(bounds, title, description);
    }

    protected TapTarget(Rect bounds, String title, String description) {
        this(title, description);
        if (bounds == null) {
            throw new IllegalArgumentException("Cannot pass null bounds, title or description");
        }

        this.bounds = bounds;
    }

    protected TapTarget(String title, String description) {
        if (title == null || description == null) {
            throw new IllegalArgumentException("Cannot pass null title or description");
        }

        this.title = title;
        this.description = description;
    }

    public TapTarget outerCircleColor(@ColorRes int color) {
        this.outerCircleColor = color;
        return this;
    }

    public TapTarget targetCircleColor(@ColorRes int color) {
        this.targetCircleColor = color;
        return this;
    }

    public TapTarget textColor(@ColorRes int color) {
        this.textColor = color;
        return this;
    }

    public TapTarget textTypeface(Typeface typeface) {
        if (typeface == null) throw new IllegalArgumentException("Cannot use a null typeface");
        this.typeface = typeface;
        return this;
    }

    public TapTarget dimColor(@ColorRes int color) {
        this.dimColor = color;
        return this;
    }

    public TapTarget drawShadow(boolean draw) {
        this.drawShadow = draw;
        return this;
    }

    public TapTarget cancelable(boolean status) {
        this.cancelable = status;
        return this;
    }

    public TapTarget tintTarget(boolean tint) {
        this.tintTarget = tint;
        return this;
    }

    public TapTarget icon(Drawable icon) {
        if (icon == null) throw new IllegalArgumentException("Cannot use null drawable");
        this.icon = icon;
        return this;
    }

    public void onReady(Runnable runnable) {
        runnable.run();
    }

    public Rect bounds() {
        if (bounds == null) {
            throw new IllegalStateException("Requesting bounds that are not set! Make sure your target is ready");
        }
        return bounds;
    }
}
