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

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.Nullable;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.getkeepsafe.taptargetview.target.TapTarget;
import com.getkeepsafe.taptargetview.target.TapTargetShapeType;

/**
 * TapTargetView implements a feature discovery paradigm following Google's Material Design
 * guidelines.
 * <p>
 * This class should not be instantiated directly. Instead, please use the
 * {@see TargetViewExtensionsKTX#showGuideView} static factory method instead.
 * <p>
 * More information can be found here:
 * https://material.google.com/growth-communications/feature-discovery.html#feature-discovery-design
 */
@SuppressLint("ViewConstructor")
public class TapTargetView extends View {
  private boolean isDismissed = false;
  private boolean isDismissing = false;
  private boolean isInteractable = true;

  final int TARGET_PADDING;
  final int TEXT_PADDING;
  final int TEXT_SPACING;
  final int TEXT_MAX_WIDTH;
  final int TEXT_POSITIONING_BIAS;
  final int CIRCLE_PADDING;
  final int GUTTER_DIM;
  final int SHADOW_DIM;
  final int SHADOW_JITTER_DIM;

  @Nullable
  final ViewGroup boundingParent;
  final ViewManager parent;
  final TapTarget target;
  final Rect targetBounds;

  final TextPaint titlePaint;
  final TextPaint descriptionPaint;
  final Paint outerCirclePaint;
  final Paint outerCircleShadowPaint;
  final Paint targetCirclePaint;
  final Paint targetCirclePulsePaint;

  CharSequence title;
  @Nullable
  StaticLayout titleLayout;
  @Nullable
  CharSequence description;
  @Nullable
  StaticLayout descriptionLayout;
  boolean isDark;
  boolean debug;
  boolean shouldTintTarget;
  boolean shouldDrawShadow;
  boolean cancelable;
  boolean visible;

  // Debug related variables
  @Nullable
  SpannableStringBuilder debugStringBuilder;
  @Nullable
  DynamicLayout debugLayout;
  @Nullable
  TextPaint debugTextPaint;
  @Nullable
  Paint debugPaint;

  // Drawing properties
  Rect drawingBounds;
  Rect textBounds;

  Path outerCirclePath;
  float outerCircleRadius;
  int calculatedOuterCircleRadius;
  int[] outerCircleCenter;
  int outerCircleAlpha;

  int targetCirclePulseAlpha;

  int targetCircleAlpha;

  int textAlpha;
  int dimColor;

  float lastTouchX;
  float lastTouchY;

  int topBoundary;
  int bottomBoundary;

  Bitmap tintedTarget;

  Listener listener;

  @Nullable
  ViewOutlineProvider outlineProvider;

  public static class Listener {
    /** Signals that the user has clicked inside of the target **/
    public void onTargetClick(TapTargetView view) {
      view.dismiss(true);
    }

    /** Signals that the user has long clicked inside of the target **/
    public void onTargetLongClick(TapTargetView view) {
      onTargetClick(view);
    }

    /** If cancelable, signals that the user has clicked outside of the outer circle **/
    public void onTargetCancel(TapTargetView view) {
      view.dismiss(false);
    }

    /** Signals that the user clicked on the outer circle portion of the tap target **/
    public void onOuterCircleClick(TapTargetView view) {
      // no-op as default
    }

    /**
     * Signals that the tap target has been dismissed
     * @param userInitiated Whether the user caused this action
     */
    public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
    }
  }

  final FloatValueAnimatorBuilder.UpdateListener expandContractUpdateListener = new FloatValueAnimatorBuilder.UpdateListener() {
    @Override
    public void onUpdate(float lerpTime) {
      final float newOuterCircleRadius = calculatedOuterCircleRadius * lerpTime;
      final boolean expanding = newOuterCircleRadius > outerCircleRadius;
      if (!expanding) {
        // When contracting we need to invalidate the old drawing bounds. Otherwise
        // you will see artifacts as the circle gets smaller
        calculateDrawingBounds();
      }

      final float targetAlpha = target.getOuterCircleAlpha() * 255;
      outerCircleRadius = newOuterCircleRadius;
      outerCircleAlpha = (int) Math.min(targetAlpha, (lerpTime * 1.5f * targetAlpha));
      outerCirclePath.reset();
      outerCirclePath.addCircle(outerCircleCenter[0], outerCircleCenter[1], outerCircleRadius, Path.Direction.CW);

      targetCircleAlpha = (int) Math.min(255.0f, (lerpTime * 1.5f * 255.0f));
      getTapType().expandContractChange(lerpTime, expanding);
      textAlpha = (int) (ValueExtensions.getDelayLerp(lerpTime, 0.7f) * 255);

      if (expanding) {
        calculateDrawingBounds();
      }

      invalidateViewAndOutline(drawingBounds);
    }
  };

  final ValueAnimator expandAnimation = new FloatValueAnimatorBuilder()
      .duration(250)
      .delayBy(250)
      .interpolator(new AccelerateDecelerateInterpolator())
      .onUpdate(lerpTime -> expandContractUpdateListener.onUpdate(lerpTime))
      .onEnd(new FloatValueAnimatorBuilder.EndListener() {
        @Override
        public void onEnd() {
          pulseAnimation.start();
          isInteractable = true;
        }
      })
      .build();

  final ValueAnimator pulseAnimation = new FloatValueAnimatorBuilder()
      .duration(1000)
      .repeat(ValueAnimator.INFINITE)
      .interpolator(new AccelerateDecelerateInterpolator())
      .onUpdate(new FloatValueAnimatorBuilder.UpdateListener() {
        @Override
        public void onUpdate(float lerpTime) {
          final float pulseLerp = ValueExtensions.getDelayLerp(lerpTime, 0.5f);
          getTapType().pulseAnimation(lerpTime);
          targetCirclePulseAlpha = (int) ((1.0f - pulseLerp) * 255);
          if (outerCircleRadius != calculatedOuterCircleRadius) {
            outerCircleRadius = calculatedOuterCircleRadius;
          }
          calculateDrawingBounds();
          invalidateViewAndOutline(drawingBounds);
        }
      })
      .build();

  final ValueAnimator dismissAnimation = new FloatValueAnimatorBuilder(true)
      .duration(250)
      .interpolator(new AccelerateDecelerateInterpolator())
      .onUpdate(lerpTime -> expandContractUpdateListener.onUpdate(lerpTime))
      .onEnd(() -> finishDismiss(true))
      .build();

  private final ValueAnimator dismissConfirmAnimation = new FloatValueAnimatorBuilder()
      .duration(250)
      .interpolator(new AccelerateDecelerateInterpolator())
      .onUpdate(new FloatValueAnimatorBuilder.UpdateListener() {
        @Override
        public void onUpdate(float lerpTime) {
          final float spedUpLerp = Math.min(1.0f, lerpTime * 2.0f);
          outerCircleRadius = calculatedOuterCircleRadius * (1.0f + (spedUpLerp * 0.2f));
          outerCircleAlpha = (int) ((1.0f - spedUpLerp) * target.getOuterCircleAlpha() * 255.0f);
          outerCirclePath.reset();
          outerCirclePath.addCircle(outerCircleCenter[0], outerCircleCenter[1], outerCircleRadius, Path.Direction.CW);
          getTapType().dismissConfirmAnimation(lerpTime);
          targetCircleAlpha = (int) ((1.0f - lerpTime) * 255.0f);
          targetCirclePulseAlpha = (int) ((1.0f - lerpTime) * targetCirclePulseAlpha);
          textAlpha = (int) ((1.0f - spedUpLerp) * 255.0f);
          calculateDrawingBounds();
          invalidateViewAndOutline(drawingBounds);
        }
      })
      .onEnd(() -> finishDismiss(true))
      .build();

  private ValueAnimator[] animators = new ValueAnimator[]
      {expandAnimation, pulseAnimation, dismissConfirmAnimation, dismissAnimation};

  private final ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

  /**
   * This constructor should only be used directly for very specific use cases not covered by
   * the static factory methods.
   *
   * @param context The host context
   * @param parent The parent that this TapTargetView will become a child of. This parent should
   *               allow the largest possible area for this view to utilize
   * @param boundingParent Optional. Will be used to calculate boundaries if needed. For example,
   *                       if your view is added to the decor view of your Window, then you want
   *                       to adjust for system ui like the navigation bar or status bar, and so
   *                       you would pass in the content view (which doesn't include system ui)
   *                       here.
   * @param target The {@link TapTarget} to target
   * @param userListener Optional. The {@link Listener} instance for this view
   */
  public TapTargetView(final Context context,
                       final ViewManager parent,
                       @Nullable final ViewGroup boundingParent,
                       final TapTarget target,
                       @Nullable final Listener userListener) {
    super(context);
    if (target == null) throw new IllegalArgumentException("Target cannot be null");

    this.target = target;
    getTapType().initResource(context);
    this.parent = parent;
    this.boundingParent = boundingParent;
    this.listener = userListener != null ? userListener : new Listener();
    this.title = target.getTitle();
    this.description = target.getDescription();

    TARGET_PADDING = getTapType().getTargetPadding();
    CIRCLE_PADDING = getTapType().getCirclePadding();
    TEXT_PADDING = getTapType().getTextPadding();
    TEXT_SPACING = getTapType().getTextSpacing();
    TEXT_MAX_WIDTH = getTapType().getTextMaxWidth();
    TEXT_POSITIONING_BIAS = getTapType().getTextPositionBias();
    GUTTER_DIM = UiUtils.getDp( 88);
    SHADOW_DIM = UiUtils.getDp( 8);
    SHADOW_JITTER_DIM = UiUtils.getDp( 1);

    outerCirclePath = new Path();
    targetBounds = new Rect();
    drawingBounds = new Rect();

    titlePaint = new TextPaint();
    titlePaint.setTextSize(target.titleTextSizePx(context));
    titlePaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    titlePaint.setAntiAlias(true);

    descriptionPaint = new TextPaint();
    descriptionPaint.setTextSize(target.descriptionTextSizePx(context));
    descriptionPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
    descriptionPaint.setAntiAlias(true);
    descriptionPaint.setAlpha((int) (0.54f * 255.0f));

    outerCirclePaint = new Paint();
    outerCirclePaint.setAntiAlias(true);
    outerCirclePaint.setAlpha((int) (target.getOuterCircleAlpha() * 255.0f));

    outerCircleShadowPaint = new Paint();
    outerCircleShadowPaint.setAntiAlias(true);
    outerCircleShadowPaint.setAlpha(50);
    outerCircleShadowPaint.setStyle(Paint.Style.STROKE);
    outerCircleShadowPaint.setStrokeWidth(SHADOW_JITTER_DIM);
    outerCircleShadowPaint.setColor(Color.BLACK);

    targetCirclePaint = new Paint();
    targetCirclePaint.setAntiAlias(true);

    targetCirclePulsePaint = new Paint();
    targetCirclePulsePaint.setAntiAlias(true);

    applyTargetOptions(context);

    final boolean hasKitkat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    final boolean translucentStatusBar;
    final boolean translucentNavigationBar;
    final boolean layoutNoLimits;

    if (context instanceof Activity) {
      Activity activity = (Activity) context;
      final int flags = activity.getWindow().getAttributes().flags;
      translucentStatusBar = hasKitkat && (flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) != 0;
      translucentNavigationBar = hasKitkat && (flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) != 0;
      layoutNoLimits = (flags & WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) != 0;
    } else {
      translucentStatusBar = false;
      translucentNavigationBar = false;
      layoutNoLimits = false;
    }

    globalLayoutListener = () -> {
      if (isDismissing) {
        return;
      }
      updateTextLayouts();
      target.onReady(() -> {
        final int[] offset = new int[2];

        targetBounds.set(target.bounds());

        getLocationOnScreen(offset);
        targetBounds.offset(-offset[0], -offset[1]);

        if (boundingParent != null) {
          final WindowManager windowManager
              = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
          final DisplayMetrics displayMetrics = new DisplayMetrics();
          windowManager.getDefaultDisplay().getMetrics(displayMetrics);

          final Rect rect = new Rect();
          boundingParent.getWindowVisibleDisplayFrame(rect);
          int[] parentLocation = new int[2];
          boundingParent.getLocationInWindow(parentLocation);

          if (translucentStatusBar) {
            rect.top = parentLocation[1];
          }
          if (translucentNavigationBar) {
            rect.bottom = parentLocation[1] + boundingParent.getHeight();
          }

          // We bound the boundaries to be within the screen's coordinates to
          // handle the case where the flag FLAG_LAYOUT_NO_LIMITS is set
          if (layoutNoLimits) {
            topBoundary = Math.max(0, rect.top);
            bottomBoundary = Math.min(rect.bottom, displayMetrics.heightPixels);
          } else {
            topBoundary = rect.top;
            bottomBoundary = rect.bottom;
          }
        }

        drawTintedTarget();
        requestFocus();
        calculateDimensions();

        startExpandAnimation();
      });
    };

    getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);

    setFocusableInTouchMode(true);
    setClickable(true);
    setOnClickListener(v -> {
      if (listener == null || outerCircleCenter == null || !isInteractable) return;

      final boolean clickedInTarget = getTapType().clickInTarget(targetBounds, (int) lastTouchX, (int) lastTouchY);
      final double distanceToOuterCircleCenter = distance(outerCircleCenter[0], outerCircleCenter[1],
          (int) lastTouchX, (int) lastTouchY);
      final boolean clickedInsideOfOuterCircle = distanceToOuterCircleCenter <= outerCircleRadius;

      if (clickedInTarget) {
        isInteractable = false;
        listener.onTargetClick(TapTargetView.this);
      } else if (clickedInsideOfOuterCircle) {
        listener.onOuterCircleClick(TapTargetView.this);
      } else if (cancelable) {
        isInteractable = false;
        listener.onTargetCancel(TapTargetView.this);
      }
    });

    setOnLongClickListener(v -> {
      if (listener == null) return false;

      if (targetBounds.contains((int) lastTouchX, (int) lastTouchY)) {
        listener.onTargetLongClick(TapTargetView.this);
        return true;
      }

      return false;
    });
  }

  private void startExpandAnimation() {
    if (!visible) {
      isInteractable = false;
      expandAnimation.start();
      visible = true;
    }
  }

  protected void applyTargetOptions(Context context) {
    shouldTintTarget = !target.getTransparentTarget() && target.getTintTarget();
    shouldDrawShadow = target.getDrawShadow();
    cancelable = target.getCancelable();

    // We can't clip out portions of a view outline, so if the user specified a transparent
    // target, we need to fallback to drawing a jittered shadow approximation
    if (shouldDrawShadow && Build.VERSION.SDK_INT >= 21 && !target.getTransparentTarget()) {
      outlineProvider = new ViewOutlineProvider() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void getOutline(View view, Outline outline) {
          if (outerCircleCenter == null) return;
          outline.setOval(
              (int) (outerCircleCenter[0] - outerCircleRadius), (int) (outerCircleCenter[1] - outerCircleRadius),
              (int) (outerCircleCenter[0] + outerCircleRadius), (int) (outerCircleCenter[1] + outerCircleRadius));
          outline.setAlpha(outerCircleAlpha / 255.0f);
          if (Build.VERSION.SDK_INT >= 22) {
            outline.offset(0, SHADOW_DIM);
          }
        }
      };

      setOutlineProvider(outlineProvider);
      setElevation(SHADOW_DIM);
    }

    if (shouldDrawShadow && outlineProvider == null && Build.VERSION.SDK_INT < 18) {
      setLayerType(LAYER_TYPE_SOFTWARE, null);
    } else {
      setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    final Resources.Theme theme = context.getTheme();
    isDark = UiUtils.getThemeIneAttr(context, "isLightTheme") == 0;

    final Integer outerCircleColor = target.outerCircleColorInt(context);
    if (outerCircleColor != null) {
      outerCirclePaint.setColor(outerCircleColor);
    } else if (theme != null) {
      outerCirclePaint.setColor(UiUtils.getThemeIneAttr(context, "colorPrimary"));
    } else {
      outerCirclePaint.setColor(Color.WHITE);
    }

    final Integer targetCircleColor = target.targetCircleColorInt(context);
    if (targetCircleColor != null) {
      targetCirclePaint.setColor(targetCircleColor);
    } else {
      targetCirclePaint.setColor(isDark ? Color.BLACK : Color.WHITE);
    }

    if (target.getTransparentTarget()) {
      targetCirclePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    targetCirclePulsePaint.setColor(targetCirclePaint.getColor());

    final Integer targetDimColor = target.dimColorInt(context);
    if (targetDimColor != null) {
      dimColor = UiUtils.setAlpha(targetDimColor, 0.3f);
    } else {
      dimColor = -1;
    }

    final Integer titleTextColor = target.titleTextColorInt(context);
    if (titleTextColor != null) {
      titlePaint.setColor(titleTextColor);
    } else {
      titlePaint.setColor(isDark ? Color.BLACK : Color.WHITE);
    }

    final Integer descriptionTextColor = target.descriptionTextColorInt(context);
    if (descriptionTextColor != null) {
      descriptionPaint.setColor(descriptionTextColor);
    } else {
      descriptionPaint.setColor(titlePaint.getColor());
    }

    if (target.getTitleTypeface() != null) {
      titlePaint.setTypeface(target.getTitleTypeface());
    }

    if (target.getDescriptionTypeface() != null) {
      descriptionPaint.setTypeface(target.getTitleTypeface());
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    onDismiss(false);
  }

  void onDismiss(boolean userInitiated) {
    if (isDismissed) return;

    isDismissing = false;
    isDismissed = true;

    for (final ValueAnimator animator : animators) {
      animator.cancel();
      animator.removeAllUpdateListeners();
    }

    getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
    visible = false;

    if (listener != null) {
      listener.onTargetDismissed(this, userInitiated);
    }
  }

  @Override
  protected void onDraw(Canvas c) {
    if (isDismissed || outerCircleCenter == null) return;

    if (topBoundary > 0 && bottomBoundary > 0) {
      c.clipRect(0, topBoundary, getWidth(), bottomBoundary);
    }

    if (dimColor != -1) {
      c.drawColor(dimColor);
    }

    int saveCount;
    outerCirclePaint.setAlpha(outerCircleAlpha);
    if (shouldDrawShadow && outlineProvider == null) {
      saveCount = c.save();
      {
        c.clipPath(outerCirclePath, Region.Op.DIFFERENCE);
        drawJitteredShadow(c);
      }
      c.restoreToCount(saveCount);
    }
    c.drawCircle(outerCircleCenter[0], outerCircleCenter[1], outerCircleRadius, outerCirclePaint);

    targetCirclePaint.setAlpha(targetCircleAlpha);
    if (targetCirclePulseAlpha > 0) {
      targetCirclePulsePaint.setAlpha(targetCirclePulseAlpha);
      getTapType().drawPulse(c, targetCirclePulseAlpha, targetBounds, targetCirclePulsePaint);
    }
    getTapType().drawTarget(c, targetBounds, targetCirclePaint);
    saveCount = c.save();
    {
      c.translate(textBounds.left, textBounds.top);
      titlePaint.setAlpha(textAlpha);
      if (titleLayout != null) {
        titleLayout.draw(c);
      }

      if (descriptionLayout != null && titleLayout != null) {
        c.translate(0, titleLayout.getHeight() + TEXT_SPACING);
        descriptionPaint.setAlpha((int) (target.getDescriptionTextAlpha() * textAlpha));
        descriptionLayout.draw(c);
      }
    }
    c.restoreToCount(saveCount);

    saveCount = c.save();
    {
      if (tintedTarget != null) {
        c.translate(targetBounds.centerX() - tintedTarget.getWidth() / 2,
            targetBounds.centerY() - tintedTarget.getHeight() / 2);
        c.drawBitmap(tintedTarget, 0, 0, targetCirclePaint);
      } else if (target.getIcon() != null) {
        c.translate(targetBounds.centerX() - target.getIcon().getBounds().width() / 2,
            targetBounds.centerY() - target.getIcon().getBounds().height() / 2);
        target.getIcon().setAlpha(targetCirclePaint.getAlpha());
        target.getIcon().draw(c);
      }
    }
    c.restoreToCount(saveCount);

    if (debug) {
      drawDebugInformation(c);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent e) {
    lastTouchX = e.getX();
    lastTouchY = e.getY();
    return super.onTouchEvent(e);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (isVisible() && cancelable && keyCode == KeyEvent.KEYCODE_BACK) {
      event.startTracking();
      return true;
    }

    return false;
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (isVisible() && isInteractable && cancelable
        && keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) {
      isInteractable = false;

      if (listener != null) {
        listener.onTargetCancel(this);
      } else {
        new Listener().onTargetCancel(this);
      }

      return true;
    }

    return false;
  }

  /**
   * Dismiss this view
   * @param tappedTarget If the user tapped the target or not
   *                     (results in different dismiss animations)
   */
  public void dismiss(boolean tappedTarget) {
    isDismissing = true;
    pulseAnimation.cancel();
    expandAnimation.cancel();
    if (!visible || outerCircleCenter == null) {
      finishDismiss(tappedTarget);
      return;
    }
    if (tappedTarget) {
      dismissConfirmAnimation.start();
    } else {
      dismissAnimation.start();
    }
  }

  private void finishDismiss(boolean userInitiated) {
    onDismiss(userInitiated);
    parent.removeView(TapTargetView.this);
  }

  /** Specify whether to draw a wireframe around the view, useful for debugging **/
  public void setDrawDebug(boolean status) {
    if (debug != status) {
      debug = status;
      postInvalidate();
    }
  }

  /** Returns whether this view is visible or not **/
  public boolean isVisible() {
    return !isDismissed && visible;
  }

  void drawJitteredShadow(Canvas c) {
    final float baseAlpha = 0.20f * outerCircleAlpha;
    outerCircleShadowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    outerCircleShadowPaint.setAlpha((int) baseAlpha);
    c.drawCircle(outerCircleCenter[0], outerCircleCenter[1] + SHADOW_DIM, outerCircleRadius, outerCircleShadowPaint);
    outerCircleShadowPaint.setStyle(Paint.Style.STROKE);
    final int numJitters = 7;
    for (int i = numJitters - 1; i > 0; --i) {
      outerCircleShadowPaint.setAlpha((int) ((i / (float) numJitters) * baseAlpha));
      c.drawCircle(outerCircleCenter[0], outerCircleCenter[1] + SHADOW_DIM ,
          outerCircleRadius + (numJitters - i) * SHADOW_JITTER_DIM , outerCircleShadowPaint);
    }
  }

  void drawDebugInformation(Canvas c) {
    if (debugPaint == null) {
      debugPaint = new Paint();
      debugPaint.setARGB(255, 255, 0, 0);
      debugPaint.setStyle(Paint.Style.STROKE);
      debugPaint.setStrokeWidth(UiUtils.getDp(1));
    }

    if (debugTextPaint == null) {
      debugTextPaint = new TextPaint();
      debugTextPaint.setColor(0xFFFF0000);
      debugTextPaint.setTextSize(UiUtils.getSp(16));
    }

    // Draw wireframe
    debugPaint.setStyle(Paint.Style.STROKE);
    c.drawRect(textBounds, debugPaint);
    c.drawRect(targetBounds, debugPaint);
    c.drawCircle(outerCircleCenter[0], outerCircleCenter[1], 10, debugPaint);
    c.drawCircle(outerCircleCenter[0], outerCircleCenter[1], calculatedOuterCircleRadius - CIRCLE_PADDING, debugPaint);
    getTapType().drawInformation(c, targetBounds, targetCirclePaint);

    // Draw positions and dimensions
    debugPaint.setStyle(Paint.Style.FILL);
    final String debugText =
            "Text bounds: " + textBounds.toShortString() + "\n" +
            "Target bounds: " + targetBounds.toShortString() + "\n" +
            "Center: " + outerCircleCenter[0] + " " + outerCircleCenter[1] + "\n" +
            "View size: " + getWidth() + " " + getHeight() + "\n" +
            "Target bounds: " + targetBounds.toShortString();

    if (debugStringBuilder == null) {
      debugStringBuilder = new SpannableStringBuilder(debugText);
    } else {
      debugStringBuilder.clear();
      debugStringBuilder.append(debugText);
    }

    if (debugLayout == null) {
      debugLayout = new DynamicLayout(debugText, debugTextPaint, getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
    }

    final int saveCount = c.save();
    {
      debugPaint.setARGB(220, 0, 0, 0);
      c.translate(0.0f, topBoundary);
      c.drawRect(0.0f, 0.0f, debugLayout.getWidth(), debugLayout.getHeight(), debugPaint);
      debugPaint.setARGB(255, 255, 0, 0);
      debugLayout.draw(c);
    }
    c.restoreToCount(saveCount);
  }

  void drawTintedTarget() {
    final Drawable icon = target.getIcon();
    if (!shouldTintTarget || icon == null) {
      tintedTarget = null;
      return;
    }

    if (tintedTarget != null) return;

    tintedTarget = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(),
        Bitmap.Config.ARGB_8888);
    final Canvas canvas = new Canvas(tintedTarget);
    icon.setColorFilter(new PorterDuffColorFilter(
        outerCirclePaint.getColor(), PorterDuff.Mode.SRC_ATOP));
    icon.draw(canvas);
    icon.setColorFilter(null);
  }

  void updateTextLayouts() {
    final int textWidth = Math.min(getWidth(), TEXT_MAX_WIDTH) - TEXT_PADDING * 2;
    if (textWidth <= 0) {
      return;
    }

    titleLayout = new StaticLayout(title, titlePaint, textWidth,
            Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

    if (description != null) {
      descriptionLayout = new StaticLayout(description, descriptionPaint, textWidth,
              Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
    } else {
      descriptionLayout = null;
    }
  }

  void calculateDimensions() {
    textBounds = getTextBounds();
    outerCircleCenter = getOuterCircleCenterPoint();
    calculatedOuterCircleRadius = getOuterCircleRadius(outerCircleCenter[0], outerCircleCenter[1], textBounds, targetBounds);
  }

  void calculateDrawingBounds() {
    if (outerCircleCenter == null) {
      // Called dismiss before we got a chance to display the tap target
      // So we have no center -> cant determine the drawing bounds
      return;
    }
    drawingBounds.left = (int) Math.max(0, outerCircleCenter[0] - outerCircleRadius);
    drawingBounds.top = (int) Math.min(0, outerCircleCenter[1] - outerCircleRadius);
    drawingBounds.right = (int) Math.min(getWidth(),
        outerCircleCenter[0] + outerCircleRadius + CIRCLE_PADDING);
    drawingBounds.bottom = (int) Math.min(getHeight(),
        outerCircleCenter[1] + outerCircleRadius + CIRCLE_PADDING);
  }

  int getOuterCircleRadius(int centerX, int centerY, Rect textBounds, Rect targetBounds) {
    final int targetCenterX = targetBounds.centerX();
    final int targetCenterY = targetBounds.centerY();
    final int expandedRadius = (int) (1.1f * getTapType().getEdgeLength());
    final Rect expandedBounds = new Rect(targetCenterX, targetCenterY, targetCenterX, targetCenterY);
    expandedBounds.inset(-expandedRadius, -expandedRadius);

    final int textRadius = maxDistanceToPoints(centerX, centerY, textBounds);
    final int targetRadius = maxDistanceToPoints(centerX, centerY, expandedBounds);
    return Math.max(textRadius, targetRadius) + CIRCLE_PADDING;
  }

  Rect getTextBounds() {
    final int totalTextHeight = getTotalTextHeight();
    final int totalTextWidth = getTotalTextWidth();
    return getTapType().getTextBounds(totalTextHeight, totalTextWidth, targetBounds, topBoundary, getWidth());
  }

  int[] getOuterCircleCenterPoint() {
    if (inGutter(targetBounds.centerY())) {
      return new int[]{targetBounds.centerX(), targetBounds.centerY()};
    }

    int edgeLength = getTapType().getEdgeLength();

    final int targetRadius = Math.max(targetBounds.width(), targetBounds.height()) / 2 + TARGET_PADDING;
    final int totalTextHeight = getTotalTextHeight();

    final boolean onTop = targetBounds.centerY() - edgeLength - TARGET_PADDING - totalTextHeight > 0;

    final int left = Math.min(textBounds.left, targetBounds.left - targetRadius);
    final int right = Math.max(textBounds.right, targetBounds.right + targetRadius);
    final int titleHeight = titleLayout == null ? 0 : titleLayout.getHeight();
    final int centerY = onTop ?
        targetBounds.centerY() - edgeLength - TARGET_PADDING - totalTextHeight + titleHeight
        :
        targetBounds.centerY() + edgeLength + TARGET_PADDING + titleHeight;

    return new int[] { (left + right) / 2, centerY };
  }

  int getTotalTextHeight() {
    if (titleLayout == null) {
      return 0;
    }

    if (descriptionLayout == null) {
      return titleLayout.getHeight() + TEXT_SPACING;
    }

    return titleLayout.getHeight() + descriptionLayout.getHeight() + TEXT_SPACING;
  }

  int getTotalTextWidth() {
    if (titleLayout == null) {
      return 0;
    }

    if (descriptionLayout == null) {
      return titleLayout.getWidth();
    }

    return Math.max(titleLayout.getWidth(), descriptionLayout.getWidth());
  }

  boolean inGutter(int y) {
    if (bottomBoundary > 0) {
      return y < GUTTER_DIM || y > bottomBoundary - GUTTER_DIM;
    } else {
      return y < GUTTER_DIM || y > getHeight() - GUTTER_DIM;
    }
  }

  int maxDistanceToPoints(int x1, int y1, Rect bounds) {
    final double tl = distance(x1, y1, bounds.left, bounds.top);
    final double tr = distance(x1, y1, bounds.right, bounds.top);
    final double bl = distance(x1, y1, bounds.left, bounds.bottom);
    final double br = distance(x1, y1, bounds.right, bounds.bottom);
    return (int) Math.max(tl, Math.max(tr, Math.max(bl, br)));
  }

  double distance(int x1, int y1, int x2, int y2) {
    return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
  }

  void invalidateViewAndOutline(Rect bounds) {
    invalidate(bounds);
    if (outlineProvider != null && Build.VERSION.SDK_INT >= 21) {
      invalidateOutline();
    }
  }

  TapTargetShapeType getTapType() {
    return target.getTapTargetType$taptargetview_debug();
  }
}
