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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * TapTargetView implements a feature discovery paradigm following Google's Material Design
 * guidelines.
 * <p>
 * This class should not be instantiated directly. Instead, please use the
 * {@link #showFor(Context, TapTarget, Listener)} static factory method instead.
 * <p>
 * More information can be found here:
 * https://material.google.com/growth-communications/feature-discovery.html#feature-discovery-design
 */
@SuppressLint("ViewConstructor")
public class TapTargetView extends View {
  private static final long EXPAND_DURATION = 250L;
  private boolean isDismissed = false;
  private boolean isDismissing = false;
  private boolean isInteractable = true;

  final int TARGET_PADDING;
  final int TARGET_RADIUS;
  final int TARGET_PULSE_RADIUS;
  final int TEXT_PADDING;
  final int TEXT_SPACING;
  final int TEXT_MAX_WIDTH;
  final int TEXT_POSITIONING_BIAS;
  final int CIRCLE_PADDING;
  final int GUTTER_DIM;
  final int SHADOW_DIM;
  final int SHADOW_JITTER_DIM;

  final @Nullable ViewGroup boundingParent;
  final ViewManager parent;
  final TapTarget target;
  final Rect targetBounds;

  final TextPaint titlePaint;
  final TextPaint descriptionPaint;
  final Paint outerCirclePaint;
  final Paint outerCircleShadowPaint;
  final Paint targetCirclePaint;
  final Paint targetCirclePulsePaint;

  @Nullable StaticLayout titleLayout;
  @Nullable StaticLayout descriptionLayout;
  boolean debug;
  boolean visible;

  // Debug related variables
  @Nullable SpannableStringBuilder debugStringBuilder;
  @Nullable DynamicLayout debugLayout;
  @Nullable TextPaint debugTextPaint;
  @Nullable Paint debugPaint;

  // Drawing properties
  Rect drawingBounds;
  Rect textBounds;

  Path outerCirclePath;
  float outerCircleRadius;
  int outerCircleAlpha;
  int calculatedOuterCircleRadius;
  int[] outerCircleCenter;

  float targetCirclePulseRadius;
  int targetCirclePulseAlpha;

  float targetCircleRadius;
  int targetCircleAlpha;

  float textAlpha;

  float lastTouchX;
  float lastTouchY;

  int topBoundary;
  int bottomBoundary;

  Bitmap tintedTarget;
  Listener listener;

  @Nullable
  ViewOutlineProvider outlineProvider;

  public static TapTargetView showFor(Context context, TapTarget target) {
    return showFor(context, target, null);
  }

  public static TapTargetView showFor(
      Context context, TapTarget target, @Nullable Listener listener) {
    if (context == null) throw new IllegalArgumentException("Context is null");
    if (target == null) throw new IllegalArgumentException("Target cannot be null");

    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    if (windowManager == null) {
      throw new IllegalStateException("Could not retrieve window manager from context");
    }

    WindowManager.LayoutParams params = new WindowManager.LayoutParams();
    params.type = WindowManager.LayoutParams.TYPE_APPLICATION;
    params.format = PixelFormat.RGBA_8888;
    params.flags = 0;
    params.gravity = Gravity.START | Gravity.TOP;
    params.x = 0;
    params.y = 0;
    params.width = WindowManager.LayoutParams.MATCH_PARENT;
    params.height = WindowManager.LayoutParams.MATCH_PARENT;

    TapTargetView tapTargetView = new TapTargetView(context, windowManager, null, target, listener);
    windowManager.addView(tapTargetView, params);

    return tapTargetView;
  }

  public interface Listener {
    /** Signals that the user has clicked inside of the target **/
    void onTargetClick(TapTargetView view);

    /** Signals that the user has long clicked inside of the target **/
    void onTargetLongClick(TapTargetView view);

    /** If cancelable, signals that the user has clicked outside of the outer circle **/
    void onTargetCancel(TapTargetView view);

    /** Signals that the user clicked on the outer circle portion of the tap target **/
    void onOuterCircleClick(TapTargetView view);

    /**
     * Signals that the tap target has been dismissed
     * @param userInitiated Whether the user caused this action
     */
    void onTargetDismissed(TapTargetView view, boolean userInitiated);
  }

  public static class BaseListener implements Listener {
    @CallSuper
    public void onTargetClick(TapTargetView view) {
      view.dismiss(true);
    }

    @CallSuper
    public void onTargetLongClick(TapTargetView view) {
      onTargetClick(view);
    }

    @CallSuper
    public void onTargetCancel(TapTargetView view) {
      view.dismiss(false);
    }

    public void onOuterCircleClick(TapTargetView view) {
      // no-op as default
    }

    public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
      // no-op as default
    }
  }

  final FloatValueAnimatorBuilder.UpdateListener expandContractUpdateListener =
      new FloatValueAnimatorBuilder.UpdateListener() {
        @Override
        public void onUpdate(float lerpTime) {
          final float newOuterCircleRadius = calculatedOuterCircleRadius * lerpTime;
          final boolean expanding = newOuterCircleRadius > outerCircleRadius;
          if (!expanding) {
            // When contracting we need to invalidate the old drawing bounds. Otherwise
            // you will see artifacts as the circle gets smaller
            calculateDrawingBounds();
          }

          TapTarget.Parameters parameters = target.param;
          int targetAlpha = Color.alpha(parameters.outerCircle.color);
          outerCircleRadius = newOuterCircleRadius;
          outerCircleAlpha = (int) Math.min(targetAlpha, (lerpTime * 1.5f * targetAlpha));
          outerCirclePath.reset();
          outerCirclePath.addCircle(
              outerCircleCenter[0], outerCircleCenter[1], outerCircleRadius, Path.Direction.CW);

          targetCircleAlpha =
              (int) Math.min(
                  Color.alpha(parameters.targetCircle.color),
                  (lerpTime * 1.5f * 255.0f));

          if (expanding) {
            targetCircleRadius = TARGET_RADIUS * Math.min(1.0f, lerpTime * 1.5f);
          } else {
            targetCircleRadius = TARGET_RADIUS * lerpTime;
            targetCirclePulseRadius *= lerpTime;
          }

          textAlpha = delayedLerp(lerpTime, 0.7f);

          if (expanding) {
            calculateDrawingBounds();
          }

          invalidateViewAndOutline(drawingBounds);
        }
      };

  final ValueAnimator expandAnimation = new FloatValueAnimatorBuilder()
      .duration(EXPAND_DURATION)
      .delayBy(250)
      .interpolator(new AccelerateDecelerateInterpolator())
      .onUpdate(new FloatValueAnimatorBuilder.UpdateListener() {
        @Override
        public void onUpdate(float lerpTime) {
          expandContractUpdateListener.onUpdate(lerpTime);
        }
      })
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
          final float pulseLerp = delayedLerp(lerpTime, 0.5f);
          targetCirclePulseRadius = (1.0f + pulseLerp) * TARGET_RADIUS;
          targetCirclePulseAlpha = (int) ((1.0f - pulseLerp) * 255);
          targetCircleRadius = TARGET_RADIUS + halfwayLerp(lerpTime) * TARGET_PULSE_RADIUS;

          if (outerCircleRadius != calculatedOuterCircleRadius) {
            outerCircleRadius = calculatedOuterCircleRadius;
          }

          calculateDrawingBounds();
          invalidateViewAndOutline(drawingBounds);
        }
      })
      .build();

  final ValueAnimator dismissAnimation = new FloatValueAnimatorBuilder(true)
      .duration(EXPAND_DURATION)
      .interpolator(new AccelerateDecelerateInterpolator())
      .onUpdate(new FloatValueAnimatorBuilder.UpdateListener() {
        @Override
        public void onUpdate(float lerpTime) {
          expandContractUpdateListener.onUpdate(lerpTime);
        }
      })
      .onEnd(new FloatValueAnimatorBuilder.EndListener() {
        @Override
        public void onEnd() {
          onDismiss(true);
          ViewUtil.removeView(parent, TapTargetView.this);
        }
      })
      .build();

  private final ValueAnimator dismissConfirmAnimation = new FloatValueAnimatorBuilder()
      .duration(EXPAND_DURATION)
      .interpolator(new AccelerateDecelerateInterpolator())
      .onUpdate(new FloatValueAnimatorBuilder.UpdateListener() {
        @Override
        public void onUpdate(float lerpTime) {
          float spedUpLerp = Math.min(1.0f, lerpTime * 2.0f);
          int targetOuterCircleAlpha = Color.alpha(target.param.outerCircle.color);
          outerCircleRadius = calculatedOuterCircleRadius * (1.0f + (spedUpLerp * 0.2f));
          outerCircleAlpha = (int) ((1.0f - spedUpLerp) * targetOuterCircleAlpha);
          outerCirclePath.reset();
          outerCirclePath.addCircle(
              outerCircleCenter[0], outerCircleCenter[1], outerCircleRadius, Path.Direction.CW);
          targetCircleRadius = (1.0f - lerpTime) * TARGET_RADIUS;
          targetCircleAlpha = (int) ((1.0f - lerpTime) * 255.0f);
          targetCirclePulseRadius = (1.0f + lerpTime) * TARGET_RADIUS;
          targetCirclePulseAlpha = (int) ((1.0f - lerpTime) * targetCirclePulseAlpha);
          textAlpha = 1.0f - spedUpLerp;
          calculateDrawingBounds();
          invalidateViewAndOutline(drawingBounds);
        }
      })
      .onEnd(new FloatValueAnimatorBuilder.EndListener() {
        @Override
        public void onEnd() {
          onDismiss(true);
          ViewUtil.removeView(parent, TapTargetView.this);
        }
      })
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
  public TapTargetView(
      final Context context,
      final ViewManager parent,
      @Nullable final ViewGroup boundingParent,
      final TapTarget target,
      @Nullable final Listener userListener) {
    super(context);
    if (target == null) throw new IllegalArgumentException("Target cannot be null");

    this.target = target;
    this.parent = parent;
    this.boundingParent = boundingParent;
    this.listener = userListener != null ? userListener : new BaseListener();

    TARGET_PADDING = UiUtil.dp(context, 20);
    CIRCLE_PADDING = UiUtil.dp(context, 40);
    TARGET_RADIUS = UiUtil.dp(context, 44);
    TEXT_PADDING = UiUtil.dp(context, 40);
    TEXT_SPACING = UiUtil.dp(context, 8);
    TEXT_MAX_WIDTH = UiUtil.dp(context, 360);
    TEXT_POSITIONING_BIAS = UiUtil.dp(context, 20);
    GUTTER_DIM = UiUtil.dp(context, 88);
    SHADOW_DIM = UiUtil.dp(context, 8);
    SHADOW_JITTER_DIM = UiUtil.dp(context, 1);
    TARGET_PULSE_RADIUS = (int) (0.1f * TARGET_RADIUS);

    outerCirclePath = new Path();
    targetBounds = new Rect();
    drawingBounds = new Rect();

    titlePaint = new TextPaint();
    titlePaint.setAntiAlias(true);

    descriptionPaint = new TextPaint();
    descriptionPaint.setAntiAlias(true);

    outerCirclePaint = new Paint();
    outerCirclePaint.setAntiAlias(true);

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

    globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        if (isDismissing) {
          return;
        }
        updateTextLayouts();
        target.attach(TapTargetView.this);
      }
    };

    getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);

    setFocusableInTouchMode(true);
    setClickable(true);
    setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (listener == null || outerCircleCenter == null || !isInteractable) return;

        final boolean clickedInTarget =
            distance(
                targetBounds.centerX(),
                targetBounds.centerY(),
                (int) lastTouchX,
                (int) lastTouchY) <= targetCircleRadius;
        final double distanceToOuterCircleCenter =
            distance(
                outerCircleCenter[0], outerCircleCenter[1], (int) lastTouchX, (int) lastTouchY);
        final boolean clickedInsideOfOuterCircle = distanceToOuterCircleCenter <= outerCircleRadius;

        if (clickedInTarget) {
          isInteractable = false;
          listener.onTargetClick(TapTargetView.this);
        } else if (clickedInsideOfOuterCircle) {
          listener.onOuterCircleClick(TapTargetView.this);
        } else if (target.param.cancelable) {
          isInteractable = false;
          listener.onTargetCancel(TapTargetView.this);
        }
      }
    });

    setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        if (listener == null) return false;

        if (targetBounds.contains((int) lastTouchX, (int) lastTouchY)) {
          listener.onTargetLongClick(TapTargetView.this);
          return true;
        }

        return false;
      }
    });
  }

  protected void applyTargetOptions(Context context) {
    final boolean shouldDrawShadow = target.param.shadow;
    final boolean transparentTarget = target.param.targetCircleTransparent;

    // We can't clip out portions of a view outline, so if the user specified a transparent
    // target, we need to fallback to drawing a jittered shadow approximation
    if (shouldDrawShadow && Build.VERSION.SDK_INT >= 21 && !transparentTarget) {
      outlineProvider = new ViewOutlineProvider() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void getOutline(View view, Outline outline) {
          if (outerCircleCenter == null) return;
          outline.setOval(
              (int) (outerCircleCenter[0] - outerCircleRadius),
              (int) (outerCircleCenter[1] - outerCircleRadius),
              (int) (outerCircleCenter[0] + outerCircleRadius),
              (int) (outerCircleCenter[1] + outerCircleRadius));
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

    outerCirclePaint.setColor(target.param.outerCircle.color);

    targetCirclePaint.setColor(target.param.targetCircle.color);
    if (transparentTarget) {
      targetCirclePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    targetCirclePulsePaint.setColor(targetCirclePaint.getColor());

    titlePaint.setColor(target.param.title.color);
    titlePaint.setTextSize(target.param.title.size);
    titlePaint.setTypeface(target.param.title.typeface);

    descriptionPaint.setColor(target.param.description.color);
    descriptionPaint.setTextSize(target.param.description.size);
    descriptionPaint.setTypeface(target.param.description.typeface);
  }

  @UiThread
  protected void onNewTargetBounds(Rect bounds) {
    final int[] offset = new int[2];
    getLocationOnScreen(offset);
    targetBounds.set(bounds);
    targetBounds.offset(-offset[0], -offset[1]);

    if (boundingParent != null) {
      final WindowManager windowManager =
          (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
      if (windowManager == null) {
        throw new IllegalStateException(
            "Could not retrieve a window manager from the current context");
      }

      final DisplayMetrics displayMetrics = new DisplayMetrics();
      windowManager.getDefaultDisplay().getMetrics(displayMetrics);

      final Rect rect = new Rect();
      boundingParent.getWindowVisibleDisplayFrame(rect);

      // We bound the boundaries to be within the screen's coordinates to
      // handle the case where the layout bounds do not match
      // (like when FLAG_LAYOUT_NO_LIMITS is specified)
      topBoundary = Math.max(0, rect.top);
      bottomBoundary = Math.min(rect.bottom, displayMetrics.heightPixels);
    }

    drawTintedTarget();
    requestFocus();
    calculateDimensions();
    if (!visible) {
      isInteractable = false;
      expandAnimation.start();
      visible = true;
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

    ViewUtil.removeOnGlobalLayoutListener(getViewTreeObserver(), globalLayoutListener);
    target.detach();
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

    if (target.param.dimColor != Color.TRANSPARENT) {
      c.drawColor(target.param.dimColor);
    }

    int saveCount;
    outerCirclePaint.setAlpha(outerCircleAlpha);
    if (target.param.shadow && outlineProvider == null) {
      saveCount = c.save();
      c.clipPath(outerCirclePath, Region.Op.DIFFERENCE);
      drawJitteredShadow(c);
      c.restoreToCount(saveCount);
    }
    c.drawCircle(outerCircleCenter[0], outerCircleCenter[1], outerCircleRadius, outerCirclePaint);

    targetCirclePaint.setAlpha(targetCircleAlpha);
    if (targetCirclePulseAlpha > 0) {
      targetCirclePulsePaint.setAlpha(targetCirclePulseAlpha);
      c.drawCircle(
          targetBounds.centerX(), targetBounds.centerY(),
          targetCirclePulseRadius, targetCirclePulsePaint);
    }
    c.drawCircle(
        targetBounds.centerX(), targetBounds.centerY(),
        targetCircleRadius, targetCirclePaint);

    saveCount = c.save();
    c.translate(textBounds.left, textBounds.top);
    titlePaint.setAlpha((int) (Color.alpha(target.param.title.color) * textAlpha));
    if (titleLayout != null) {
      titleLayout.draw(c);
    }

    if (descriptionLayout != null) {
      if (titleLayout != null) {
        c.translate(0, titleLayout.getHeight() + TEXT_SPACING);
      }
      descriptionPaint.setAlpha((int) (Color.alpha(target.param.description.color) * textAlpha));
      descriptionLayout.draw(c);
    }
    c.restoreToCount(saveCount);

    saveCount = c.save();
    if (tintedTarget != null) {
      c.translate(
          targetBounds.centerX() - tintedTarget.getWidth() / 2,
          targetBounds.centerY() - tintedTarget.getHeight() / 2);
      c.drawBitmap(tintedTarget, 0, 0, targetCirclePaint);
    } else if (target.param.icon != null) {
      Drawable icon = target.param.icon;
      c.translate(
          targetBounds.centerX() - icon.getBounds().width() / 2,
          targetBounds.centerY() - icon.getBounds().height() / 2);
      icon.setAlpha(targetCirclePaint.getAlpha());
      icon.draw(c);
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
    if (isVisible() && target.param.cancelable && keyCode == KeyEvent.KEYCODE_BACK) {
      event.startTracking();
      return true;
    }

    return false;
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (isVisible()
        && isInteractable
        && target.param.cancelable
        && keyCode == KeyEvent.KEYCODE_BACK
        && event.isTracking()
        && !event.isCanceled()) {
      isInteractable = false;

      if (listener != null) {
        listener.onTargetCancel(this);
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
    if (tappedTarget) {
      dismissConfirmAnimation.start();
    } else {
      dismissAnimation.start();
    }
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
    c.drawCircle(
        outerCircleCenter[0],
        outerCircleCenter[1] + SHADOW_DIM,
        outerCircleRadius,
        outerCircleShadowPaint);
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
      debugPaint.setStrokeWidth(UiUtil.dp(getContext(), 1));
    }

    if (debugTextPaint == null) {
      debugTextPaint = new TextPaint();
      debugTextPaint.setColor(0xFFFF0000);
      debugTextPaint.setTextSize(UiUtil.sp(getContext(), 16));
    }

    // Draw wireframe
    debugPaint.setStyle(Paint.Style.STROKE);
    c.drawRect(textBounds, debugPaint);
    c.drawRect(targetBounds, debugPaint);
    c.drawCircle(outerCircleCenter[0], outerCircleCenter[1], 10, debugPaint);
    c.drawCircle(
        outerCircleCenter[0],
        outerCircleCenter[1],
        calculatedOuterCircleRadius - CIRCLE_PADDING,
        debugPaint);
    c.drawCircle(
        targetBounds.centerX(),
        targetBounds.centerY(),
        TARGET_RADIUS + TARGET_PADDING,
        debugPaint);

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
      debugLayout =
          new DynamicLayout(
              debugText,
              debugTextPaint,
              getWidth(),
              Layout.Alignment.ALIGN_NORMAL,
              1.0f,
              0.0f,
              false);
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
    final Drawable icon = target.param.icon;
    if (!target.param.tint || icon == null) {
      tintedTarget = null;
      return;
    }

    if (tintedTarget != null) return;

    tintedTarget = Bitmap.createBitmap(
        icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    final Canvas canvas = new Canvas(tintedTarget);
    icon.setColorFilter(
        new PorterDuffColorFilter(outerCirclePaint.getColor(), PorterDuff.Mode.SRC_ATOP));
    icon.draw(canvas);
    icon.setColorFilter(null);
  }

  void updateTextLayouts() {
    final int textWidth = Math.min(getWidth(), TEXT_MAX_WIDTH) - TEXT_PADDING * 2;
    if (textWidth <= 0) {
      return;
    }

    final CharSequence titleText = target.param.title.text;
    if (titleText != null) {
      titleLayout =
          new StaticLayout(
              titleText, titlePaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
    } else {
      titleLayout = null;
    }

    final CharSequence descriptionText = target.param.description.text;
    if (descriptionText != null) {
      descriptionLayout =
          new StaticLayout(
              descriptionText,
              descriptionPaint,
              textWidth,
              Layout.Alignment.ALIGN_NORMAL,
              1.0f,
              0.0f,
              false);
    } else {
      descriptionLayout = null;
    }
  }

  float halfwayLerp(float lerp) {
    if (lerp < 0.5f) {
      return lerp / 0.5f;
    }

    return (1.0f - lerp) / 0.5f;
  }

  float delayedLerp(float lerp, float threshold) {
    if (lerp < threshold) {
      return 0.0f;
    }

    return (lerp - threshold) / (1.0f - threshold);
  }

  void calculateDimensions() {
    textBounds = getTextBounds();
    outerCircleCenter = getOuterCircleCenterPoint();
    calculatedOuterCircleRadius =
        getOuterCircleRadius(outerCircleCenter[0], outerCircleCenter[1], textBounds, targetBounds);
  }

  void calculateDrawingBounds() {
    if (outerCircleCenter == null) {
      // Called dismiss before we got a chance to display the tap target
      // So we have no center -> cant determine the drawing bounds
      return;
    }
    drawingBounds.left = (int) Math.max(0, outerCircleCenter[0] - outerCircleRadius);
    drawingBounds.top = (int) Math.min(0, outerCircleCenter[1] - outerCircleRadius);
    drawingBounds.right =
        (int) Math.min(getWidth(), outerCircleCenter[0] + outerCircleRadius + CIRCLE_PADDING);
    drawingBounds.bottom =
        (int) Math.min(getHeight(), outerCircleCenter[1] + outerCircleRadius + CIRCLE_PADDING);
  }

  int getOuterCircleRadius(int centerX, int centerY, Rect textBounds, Rect targetBounds) {
    final int targetCenterX = targetBounds.centerX();
    final int targetCenterY = targetBounds.centerY();
    final int expandedRadius = (int) (1.1f * TARGET_RADIUS);
    final Rect expandedBounds =
        new Rect(targetCenterX, targetCenterY, targetCenterX, targetCenterY);
    expandedBounds.inset(-expandedRadius, -expandedRadius);

    final int textRadius = maxDistanceToPoints(centerX, centerY, textBounds);
    final int targetRadius = maxDistanceToPoints(centerX, centerY, expandedBounds);
    return Math.max(textRadius, targetRadius) + CIRCLE_PADDING;
  }

  Rect getTextBounds() {
    final int totalTextHeight = getTotalTextHeight();
    final int totalTextWidth = getTotalTextWidth();

    final int possibleTop =
        targetBounds.centerY() - TARGET_RADIUS - TARGET_PADDING - totalTextHeight;
    final int top;
    if (possibleTop > topBoundary) {
      top = possibleTop;
    } else {
      top = targetBounds.centerY() + TARGET_RADIUS + TARGET_PADDING;
    }

    final int relativeCenterDistance = (getWidth() / 2) - targetBounds.centerX();
    final int bias = relativeCenterDistance < 0 ? -TEXT_POSITIONING_BIAS : TEXT_POSITIONING_BIAS;
    final int left = Math.max(TEXT_PADDING, targetBounds.centerX() - bias - totalTextWidth);
    final int right = Math.min(getWidth() - TEXT_PADDING, left + totalTextWidth);
    return new Rect(left, top, right, top + totalTextHeight);
  }

  int[] getOuterCircleCenterPoint() {
    if (inGutter(targetBounds.centerY())) {
      return new int[]{targetBounds.centerX(), targetBounds.centerY()};
    }

    final int targetRadius =
        Math.max(targetBounds.width(), targetBounds.height()) / 2 + TARGET_PADDING;
    final int totalTextHeight = getTotalTextHeight();

    final boolean onTop =
        targetBounds.centerY() - TARGET_RADIUS - TARGET_PADDING - totalTextHeight > 0;

    final int left = Math.min(textBounds.left, targetBounds.left - targetRadius);
    final int right = Math.max(textBounds.right, targetBounds.right + targetRadius);
    final int titleHeight = titleLayout == null ? 0 : titleLayout.getHeight();
    final int centerY =
        onTop
        ? targetBounds.centerY() - TARGET_RADIUS - TARGET_PADDING - totalTextHeight + titleHeight
        : targetBounds.centerY() + TARGET_RADIUS + TARGET_PADDING + titleHeight;

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
}
