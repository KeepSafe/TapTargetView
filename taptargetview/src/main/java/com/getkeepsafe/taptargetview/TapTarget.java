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
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Describes the properties and options for a {@link TapTargetView}.
 * <p>
 * Each tap target describes a target via a pair of bounds and icon. The bounds dictate the
 * location and touch area of the target, where the icon is what will be drawn within the center of
 * the bounds.
 * <p>
 * This class can be extended to support various target types.
 *
 * @see ViewTapTarget ViewTapTarget for targeting standard Android views
 */
public class TapTarget {
    final CharSequence title;
    @Nullable final CharSequence description;

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

    /** Return a tap target for the specified view **/
    public static ViewTapTarget forView(View view, CharSequence title) {
        return forView(view, title, null);
    }

    /** Return a tap target for the specified view **/
    public static ViewTapTarget forView(View view, CharSequence title, @Nullable CharSequence description) {
        return new ViewTapTarget(view, title, description);
    }

    /** Return a tap target for the specified bounds **/
    public static TapTarget forBounds(Rect bounds, CharSequence title) {
        return forBounds(bounds, title, null);
    }

    /** Return a tap target for the specified bounds **/
    public static TapTarget forBounds(Rect bounds, CharSequence title, @Nullable CharSequence description) {
        return new TapTarget(bounds, title, description);
    }

    protected TapTarget(Rect bounds, CharSequence title, @Nullable CharSequence description) {
        this(title, description);
        if (bounds == null) {
            throw new IllegalArgumentException("Cannot pass null bounds or title");
        }

        this.bounds = bounds;
    }

    protected TapTarget(CharSequence title, @Nullable CharSequence description) {
        if (title == null) {
            throw new IllegalArgumentException("Cannot pass null title");
        }

        this.title = title;
        this.description = description;
    }

    /** Specify the color resource for the outer circle **/
    public TapTarget outerCircleColor(@ColorRes int color) {
        this.outerCircleColor = color;
        return this;
    }

    /** Specify the color resource for the target circle **/
    public TapTarget targetCircleColor(@ColorRes int color) {
        this.targetCircleColor = color;
        return this;
    }

    /** Specify the color resource for all text **/
    public TapTarget textColor(@ColorRes int color) {
        this.textColor = color;
        return this;
    }

    /** Specify the typeface for all text **/
    public TapTarget textTypeface(Typeface typeface) {
        if (typeface == null) throw new IllegalArgumentException("Cannot use a null typeface");
        this.typeface = typeface;
        return this;
    }

    /**
     * Specify the color resource to use as a dim effect
     * <p>
     * <b>Note:</b> The given color will have its opacity modified to 30% automatically
     */
    public TapTarget dimColor(@ColorRes int color) {
        this.dimColor = color;
        return this;
    }

    /** Specify whether or not to draw a drop shadow around the outer circle **/
    public TapTarget drawShadow(boolean draw) {
        this.drawShadow = draw;
        return this;
    }

    /** Specify whether or not the target should be cancelable **/
    public TapTarget cancelable(boolean status) {
        this.cancelable = status;
        return this;
    }

    /** Specify whether to tint the target's icon with the outer circle's color **/
    public TapTarget tintTarget(boolean tint) {
        this.tintTarget = tint;
        return this;
    }

    /** Specify the icon that will be drawn in the center of the target bounds **/
    public TapTarget icon(Drawable icon) {
        return icon(icon, false);
    }

    /**
     * Specify the icon that will be drawn in the center of the target bounds
     * @param hasSetBounds Whether the drawable already has its bounds correctly set. If the
     *                     drawable does not have its bounds set, then the following bounds will
     *                     be applied: <br/>
     *                      <code>(0, 0, intrinsic-width, intrinsic-height)</code>
     */
    public TapTarget icon(Drawable icon, boolean hasSetBounds) {
        if (icon == null) throw new IllegalArgumentException("Cannot use null drawable");
        this.icon = icon;

        if (!hasSetBounds) {
            this.icon.setBounds(new Rect(0, 0, this.icon.getIntrinsicWidth(), this.icon.getIntrinsicHeight()));
        }

        return this;
    }

    /**
     * In case your target needs time to be ready (laid out in your view, not created, etc), the
     * runnable passed here will be invoked when the target is ready.
     */
    public void onReady(Runnable runnable) {
        runnable.run();
    }

    /**
     * Returns the target bounds. Throws an exception if they are not set
     * (target may not be ready)
     * <p>
     * This will only be called internally when {@link #onReady(Runnable)} invokes its runnable
     */
    public Rect bounds() {
        if (bounds == null) {
            throw new IllegalStateException("Requesting bounds that are not set! Make sure your target is ready");
        }
        return bounds;
    }
}
