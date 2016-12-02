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

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
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
    int targetRadius = 44;

    Rect bounds;
    Drawable icon;
    Typeface typeface;

    @ColorRes private int outerCircleColorRes = -1;
    @ColorRes private int targetCircleColorRes = -1;
    @ColorRes private int dimColorRes = -1;
    @ColorRes private int titleTextColorRes = -1;
    @ColorRes private int descriptionTextColorRes = -1;

    private int outerCircleColor = -1;
    private int targetCircleColor = -1;
    private int dimColor = -1;
    private int titleTextColor = -1;
    private int descriptionTextColor = -1;

    @DimenRes private int titleTextDimen = -1;
    @DimenRes private int descriptionTextDimen = -1;

    private int titleTextSize = 20;
    private int descriptionTextSize = 18;
    int id = -1;

    boolean drawShadow = false;
    boolean cancelable = true;
    boolean tintTarget = true;
    boolean transparentTarget = false;

    /**
     * Return a tap target for the overflow button from the given toolbar
     * <p>
     * <b>Note:</b> This is currently experimental, use at your own risk
     */
    public static ToolbarTapTarget forToolbarOverflow(Toolbar toolbar, CharSequence title) {
        return forToolbarOverflow(toolbar, title, null);
    }

    /** Return a tap target for the overflow button from the given toolbar
     * <p>
     * <b>Note:</b> This is currently experimental, use at your own risk
     */
    public static ToolbarTapTarget forToolbarOverflow(Toolbar toolbar, CharSequence title,
                                                      @Nullable CharSequence description) {
        return new ToolbarTapTarget(toolbar, false, title, description);
    }

    /** Return a tap target for the overflow button from the given toolbar
     * <p>
     * <b>Note:</b> This is currently experimental, use at your own risk
     */
    public static ToolbarTapTarget forToolbarOverflow(android.widget.Toolbar toolbar, CharSequence title) {
        return forToolbarOverflow(toolbar, title, null);
    }

    /** Return a tap target for the overflow button from the given toolbar
     * <p>
     * <b>Note:</b> This is currently experimental, use at your own risk
     */
    public static ToolbarTapTarget forToolbarOverflow(android.widget.Toolbar toolbar, CharSequence title,
                                                      @Nullable CharSequence description) {
        return new ToolbarTapTarget(toolbar, false, title, description);
    }

    /** Return a tap target for the navigation button (back, up, etc) from the given toolbar **/
    public static ToolbarTapTarget forToolbarNavigationIcon(Toolbar toolbar, CharSequence title) {
        return forToolbarNavigationIcon(toolbar, title, null);
    }

    /** Return a tap target for the navigation button (back, up, etc) from the given toolbar **/
    public static ToolbarTapTarget forToolbarNavigationIcon(Toolbar toolbar, CharSequence title,
                                                            @Nullable CharSequence description) {
        return new ToolbarTapTarget(toolbar, true, title, description);
    }

    /** Return a tap target for the navigation button (back, up, etc) from the given toolbar **/
    public static ToolbarTapTarget forToolbarNavigationIcon(android.widget.Toolbar toolbar, CharSequence title) {
        return forToolbarNavigationIcon(toolbar, title, null);
    }

    /** Return a tap target for the navigation button (back, up, etc) from the given toolbar **/
    public static ToolbarTapTarget forToolbarNavigationIcon(android.widget.Toolbar toolbar, CharSequence title,
                                                            @Nullable CharSequence description) {
        return new ToolbarTapTarget(toolbar, true, title, description);
    }

    /** Return a tap target for the menu item from the given toolbar **/
    public static ToolbarTapTarget forToolbarMenuItem(Toolbar toolbar, @IdRes int menuItemId,
                                                      CharSequence title) {
        return forToolbarMenuItem(toolbar, menuItemId, title, null);
    }

    /** Return a tap target for the menu item from the given toolbar **/
    public static ToolbarTapTarget forToolbarMenuItem(Toolbar toolbar, @IdRes int menuItemId,
                                                      CharSequence title, @Nullable CharSequence description) {
        return new ToolbarTapTarget(toolbar, menuItemId, title, description);
    }

    /** Return a tap target for the menu item from the given toolbar **/
    public static ToolbarTapTarget forToolbarMenuItem(android.widget.Toolbar toolbar, @IdRes int menuItemId,
                                                      CharSequence title) {
        return forToolbarMenuItem(toolbar, menuItemId, title, null);
    }

    /** Return a tap target for the menu item from the given toolbar **/
    public static ToolbarTapTarget forToolbarMenuItem(android.widget.Toolbar toolbar, @IdRes int menuItemId,
                                                      CharSequence title, @Nullable CharSequence description) {
        return new ToolbarTapTarget(toolbar, menuItemId, title, description);
    }

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

    /** Specify whether the target should be transparent **/
    public TapTarget transparentTarget(boolean transparent) {
        this.transparentTarget = transparent;
        return this;
    }

    /** Specify the color resource for the outer circle **/
    public TapTarget outerCircleColor(@ColorRes int color) {
        this.outerCircleColorRes = color;
        return this;
    }

    /** Specify the color value for the outer circle **/
    // TODO(Hilal): In v2, this API should be cleaned up / torched
    public TapTarget outerCircleColorInt(@ColorInt int color) {
        this.outerCircleColor = color;
        return this;
    }

    /** Specify the color resource for the target circle **/
    public TapTarget targetCircleColor(@ColorRes int color) {
        this.targetCircleColorRes = color;
        return this;
    }

    /** Specify the color value for the target circle **/
    // TODO(Hilal): In v2, this API should be cleaned up / torched
    public TapTarget targetCircleColorInt(@ColorInt int color) {
        this.targetCircleColor = color;
        return this;
    }

    /** Specify the color resource for all text **/
    public TapTarget textColor(@ColorRes int color) {
        this.titleTextColorRes = color;
        this.descriptionTextColorRes = color;
        return this;
    }

    /** Specify the color value for all text **/
    // TODO(Hilal): In v2, this API should be cleaned up / torched
    public TapTarget textColorInt(@ColorInt int color) {
        this.titleTextColor = color;
        this.descriptionTextColor = color;
        return this;
    }

    /** Specify the color resource for the title text **/
    public TapTarget titleTextColor(@ColorRes int color) {
        this.titleTextColorRes = color;
        return this;
    }

    /** Specify the color value for the title text **/
    // TODO(Hilal): In v2, this API should be cleaned up / torched
    public TapTarget titleTextColorInt(@ColorInt int color) {
        this.titleTextColor = color;
        return this;
    }

    /** Specify the color resource for the description text **/
    public TapTarget descriptionTextColor(@ColorRes int color) {
        this.descriptionTextColorRes = color;
        return this;
    }

    /** Specify the color value for the description text **/
    // TODO(Hilal): In v2, this API should be cleaned up / torched
    public TapTarget descriptionTextColorInt(@ColorInt int color) {
        this.descriptionTextColor = color;
        return this;
    }

    /** Specify the typeface for all text **/
    public TapTarget textTypeface(Typeface typeface) {
        if (typeface == null) throw new IllegalArgumentException("Cannot use a null typeface");
        this.typeface = typeface;
        return this;
    }

    /** Specify the text size for the title in SP **/
    public TapTarget titleTextSize(int sp) {
        if (sp < 0) throw new IllegalArgumentException("Given negative text size");
        this.titleTextSize = sp;
        return this;
    }

    /** Specify the text size for the description in SP **/
    public TapTarget descriptionTextSize(int sp) {
        if (sp < 0) throw new IllegalArgumentException("Given negative text size");
        this.descriptionTextSize = sp;
        return this;
    }

    /**
     * Specify the text size for the title via a dimen resource
     * <p>
     * Note: If set, this value will take precedence over the specified sp size
     */
    public TapTarget titleTextDimen(@DimenRes int dimen) {
        this.titleTextDimen = dimen;
        return this;
    }

    /**
     * Specify the text size for the description via a dimen resource
     * <p>
     * Note: If set, this value will take precedence over the specified sp size
     */
    public TapTarget descriptionTextDimen(@DimenRes int dimen) {
        this.descriptionTextDimen = dimen;
        return this;
    }

    /**
     * Specify the color resource to use as a dim effect
     * <p>
     * <b>Note:</b> The given color will have its opacity modified to 30% automatically
     */
    public TapTarget dimColor(@ColorRes int color) {
        this.dimColorRes = color;
        return this;
    }

    /**
     * Specify the color value to use as a dim effect
     * <p>
     * <b>Note:</b> The given color will have its opacity modified to 30% automatically
     */
    // TODO(Hilal): In v2, this API should be cleaned up / torched
    public TapTarget dimColorInt(@ColorInt int color) {
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

    /** Specify a unique identifier for this target. **/
    public TapTarget id(int id) {
        this.id = id;
        return this;
    }

    /** Specify the target radius in dp. **/
    public TapTarget targetRadius(int targetRadius) {
        this.targetRadius = targetRadius;
        return this;
    }


    /** Return the id associated with this tap target **/
    public int id() {
        return id;
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

    int outerCircleColorInt(Context context) {
        return colorResOrInt(context, outerCircleColor, outerCircleColorRes);
    }

    int targetCircleColorInt(Context context) {
        return colorResOrInt(context, targetCircleColor, targetCircleColorRes);
    }

    int dimColorInt(Context context) {
        return colorResOrInt(context, dimColor, dimColorRes);
    }

    int titleTextColorInt(Context context) {
        return colorResOrInt(context, titleTextColor, titleTextColorRes);
    }

    int descriptionTextColorInt(Context context) {
        return colorResOrInt(context, descriptionTextColor, descriptionTextColorRes);
    }

    int titleTextSizePx(Context context) {
        return dimenOrSize(context, titleTextSize, titleTextDimen);
    }

    int descriptionTextSizePx(Context context) {
        return dimenOrSize(context, descriptionTextSize, descriptionTextDimen);
    }

    private int colorResOrInt(Context context, int value, @ColorRes int resource) {
        if (resource != -1) {
            return UiUtil.color(context, resource);
        }

        return value;
    }

    private int dimenOrSize(Context context, int size, @DimenRes int dimen) {
        if (dimen != -1) {
            return UiUtil.dimen(context, dimen);
        }

        return UiUtil.sp(context, size);
    }
}
