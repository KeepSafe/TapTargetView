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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.Dimension;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;

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
  private final Rect bounds;

  protected final Parameters param;

  private @Nullable TapTargetView parent;

  TapTarget(Parameters parameters) {
    // TODO: Ensure required params
    this.param = parameters;
    this.bounds = new Rect();
  }

  @UiThread
  public void setBounds(Rect bounds) {
    this.bounds.set(bounds);
    if (!isReady()) {
      return;
    }
    if (parent != null) {
      parent.onNewTargetBounds(bounds);
    }
  }

  @CallSuper
  @UiThread
  protected void attach(TapTargetView parent) {
    if (parent == null) {
      throw new IllegalArgumentException("Cannot attach to null parent");
    }
    if (this.parent != null) {
      detach();
    }
    this.parent = parent;
  }

  @CallSuper
  @UiThread
  protected void detach() {
    this.parent = null;
  }

  public @Nullable String id() {
    return param.id;
  }

  public boolean isReady() {
    return true;
  }

  protected static class TextParameters {
    public @Nullable CharSequence text;
    public Typeface typeface;
    public @ColorInt int color;
    public int size;
  }

  protected static class CircleParameters {
    public @ColorInt int color;
  }

  protected static class Parameters {
    public TextParameters title;
    public TextParameters description;

    public CircleParameters outerCircle;
    public CircleParameters targetCircle;

    public @Nullable String id;
    public @Nullable Drawable icon;

    public @ColorInt int dimColor;

    public boolean shadow;
    public boolean cancelable;
    public boolean tint;
    public boolean targetCircleTransparent;
  }

  public static class Builder {
    private final Context context;
    protected final Parameters parameters;

    Builder(Context context) {
      if (context == null) {
        throw new IllegalArgumentException("Given null Context");
      }
      this.context = context;

      // Setup defaults
      final boolean isDark = UiUtil.isDarkTheme(context);
      parameters = new Parameters();
      parameters.title = new TextParameters();
      parameters.title.color = isDark ? Color.BLACK : Color.WHITE;
      parameters.title.size = UiUtil.sp(context, 20);
      parameters.title.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);

      parameters.description = new TextParameters();
      parameters.description.color = UiUtil.setAlpha(isDark ? Color.BLACK : Color.WHITE, 0.54f);
      parameters.description.size = UiUtil.sp(context, 18);
      parameters.description.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

      final Resources.Theme theme = context.getTheme();
      final int outerCircleColor =
          (theme != null) ? UiUtil.themeIntAttr(context, "colorPrimary") : Color.WHITE;
      parameters.outerCircle = new CircleParameters();
      parameters.outerCircle.color = UiUtil.setAlpha(outerCircleColor, 0.96f);

      parameters.targetCircle= new CircleParameters();
      parameters.targetCircle.color = isDark ? Color.BLACK : Color.WHITE;

      parameters.dimColor = UiUtil.setAlpha(Color.BLACK, 0.3f);
      parameters.shadow = false;
      parameters.cancelable = true;
      parameters.tint = true;
      parameters.targetCircleTransparent = false;
    }

    /** Specify the color resource for the outer circle */
    public Builder outerCircleColorRes(@ColorRes int colorRes) {
      return outerCircleColor(ContextCompat.getColor(context, colorRes));
    }

    /** Specify the color value for the outer circle */
    public Builder outerCircleColor(@ColorInt int color) {
      parameters.outerCircle.color = color;
      return this;
    }

    /** Specify the color resource for the target circle */
    public Builder targetCircleColorRes(@ColorRes int colorRes) {
      return targetCircleColor(ContextCompat.getColor(context, colorRes));
    }

    /** Specify the color value for the target circle */
    public Builder targetCircleColor(@ColorInt int color) {
      parameters.targetCircle.color = color;
      return this;
    }

    /** Specify whether the target circle should be transparent */
    public Builder targetCircleIsTransparent(boolean status) {
      parameters.targetCircleTransparent = status;
      return this;
    }

    /** Specify the title text */
    public Builder titleText(CharSequence title) {
      parameters.title.text = title;
      return this;
    }

    /** Specify the color resource for the title text */
    public Builder titleTextColorRes(@ColorRes int colorRes) {
      return titleTextColor(ContextCompat.getColor(context, colorRes));
    }

    /** Specify the color value for the title text */
    public Builder titleTextColor(@ColorInt int color) {
      parameters.title.color = color;
      return this;
    }

    /** Specify the typeface for title text */
    public Builder titleTextTypeface(Typeface typeface) {
      checkNotNull(typeface, "title typeface");
      parameters.title.typeface = typeface;
      return this;
    }

    /** Specify the text size for the title from a dimension resource */
    public Builder titleTextSizeDimen(@DimenRes int dimen) {
      return titleTextSize(context.getResources().getDimensionPixelSize(dimen));
    }

    /** Specify the text size for the title in SP */
    public Builder titleTextSizeSp(@Dimension(unit = Dimension.SP) int sp) {
      return titleTextSize(UiUtil.sp(context, sp));
    }

    /** Specify the text size for the title in pixels */
    public Builder titleTextSize(@Px int px) {
      checkTextSize(px);
      parameters.title.size = px;
      return this;
    }

    /** Specify the description text */
    public Builder descriptionText(CharSequence description) {
      parameters.description.text = description;
      return this;
    }

    /** Specify the color resource for the description text */
    public Builder descriptionTextColorRes(@ColorRes int colorRes) {
      return descriptionTextColor(ContextCompat.getColor(context, colorRes));
    }

    /** Specify the color value for the description text */
    public Builder descriptionTextColor(@ColorInt int color) {
      parameters.description.color = color;
      return this;
    }

    /** Specify the typeface for description text */
    public Builder descriptionTextTypeface(Typeface typeface) {
      checkNotNull(typeface, "description typeface");
      parameters.description.typeface = typeface;
      return this;
    }

    /** Specify the text size for the description from a dimension resource */
    public Builder descriptionTextSizeDimen(@DimenRes int dimen) {
      return descriptionTextSize(context.getResources().getDimensionPixelSize(dimen));
    }

    /** Specify the text size for the description in SP */
    public Builder descriptionTextSizeSp(@Dimension(unit = Dimension.SP) int sp) {
      return descriptionTextSize(UiUtil.sp(context, sp));
    }

    /** Specify the text size for the description in pixels */
    public Builder descriptionTextSize(@Px int px) {
      checkTextSize(px);
      parameters.description.size = px;
      return this;
    }

    /** Specify the color resource to use as a dim effect */
    public Builder dimColorRes(@ColorRes int colorRes) {
      return dimColor(ContextCompat.getColor(context, colorRes));
    }

    /** Specify the color value to use as a dim effect */
    public Builder dimColor(@ColorInt int color) {
      parameters.dimColor = color;
      return this;
    }

    public Builder shadow(boolean status) {
      parameters.shadow = status;
      return this;
    }

    public Builder cancelable(boolean status) {
      parameters.cancelable = status;
      return this;
    }

    public Builder tintTarget(boolean status) {
      parameters.tint = status;
      return this;
    }

    /** Specify the icon resource that will be drawn in the center of the target bounds */
    public Builder iconRes(@DrawableRes int drawableRes) {
      return icon(ContextCompat.getDrawable(context, drawableRes));
    }

    /** Specify the icon that will be drawn in the center of the target bounds */
    public Builder icon(Drawable icon) {
      return icon(icon, false);
    }

    /**
     * Specify the icon that will be drawn in the center of the target bounds
     * @param hasSetBounds Whether the drawable already has its bounds correctly set. If the
     *                     drawable does not have its bounds set, then the following bounds will
     *                     be applied: <code>(0, 0, intrinsic-width, intrinsic-height)</code>
     */
    public Builder icon(Drawable icon, boolean hasSetBounds) {
      checkNotNull(icon, "icon");
      icon.mutate();
      if (!hasSetBounds) {
        icon.setBounds(new Rect(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight()));
      }

      parameters.icon = icon;
      return this;
    }

    /** Specify a unique identifier for this target */
    public Builder id(String id) {
      parameters.id = id;
      return this;
    }

    void checkNotNull(Object object, String name) {
      if (object == null) throw new IllegalArgumentException("Given " + name + " is null");
    }

    void checkTextSize(int size) {
      if (size < 0) throw new IllegalArgumentException("Given negative text size");
    }

    public TapTarget build() {
      return new TapTarget(parameters);
    }
  }
}
