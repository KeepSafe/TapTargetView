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

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
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
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * TapTargetView implements a feature discovery paradigm following Google's Material Design
 * guidelines.
 * <p>
 * This class should not be instantiated directly. Instead, please use the
 * {@link #showFor(Activity, TapTarget, Listener)} static factory method instead.
 * <p>
 * More information can be found here:
 * https://material.google.com/growth-communications/feature-discovery.html#feature-discovery-design
 */
@SuppressLint("ViewConstructor")
public class TapTargetView extends View {
    private static final int UNSET_COLOR = -1;
    private boolean isDismissed = false;
    private boolean isCanceled = false;

    final int TARGET_PADDING;
    final int TARGET_RADIUS;
    final int TARGET_PULSE_RADIUS;
    final int TEXT_PADDING;
    final int TEXT_SPACING;
    final int CIRCLE_PADDING;
    final int GUTTER_DIM;
    final int SHADOW_DIM;

    @Nullable final ViewGroup boundingParent;
    final ViewManager parent;
    final TapTarget target;
    final Rect targetBounds;

    final TextPaint titlePaint;
    final TextPaint descriptionPaint;
    final Paint outerCirclePaint;
    final Paint outerCircleShadowPaint;
    final Paint targetCirclePaint;
    final Paint targetCirclePulsePaint;
    final Paint debugPaint;

    CharSequence title;
    StaticLayout titleLayout;
    @Nullable CharSequence description;
    @Nullable StaticLayout descriptionLayout;
    boolean isDark;
    boolean debug;
    boolean shouldTintTarget;
    boolean shouldDrawShadow;
    boolean cancelable;
    boolean visible;

    // Drawing properties
    Rect drawingBounds;
    Rect textBounds;

    Path outerCirclePath;
    float outerCircleRadius;
    int calculatedOuterCircleRadius;
    int[] outerCircleCenter;
    int outerCircleAlpha;

    float targetCirclePulseRadius;
    int targetCirclePulseAlpha;

    float targetCircleRadius;
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

    public static TapTargetView showFor(Activity activity, TapTarget target) {
        return showFor(activity, target, null);
    }

    public static TapTargetView showFor(Activity activity, TapTarget target, Listener listener) {
        if (activity == null) throw new IllegalArgumentException("Activity is null");

        final ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final ViewGroup content = (ViewGroup) decor.findViewById(android.R.id.content);
        final TapTargetView tapTargetView = new TapTargetView(activity, decor, content, target, listener);
        decor.addView(tapTargetView, layoutParams);

        return tapTargetView;
    }

    public static TapTargetView showFor(Dialog dialog, TapTarget target) {
        return showFor(dialog, target, null);
    }

    public static TapTargetView showFor(Dialog dialog, TapTarget target, Listener listener) {
        if (dialog == null) throw new IllegalArgumentException("Dialog is null");

        final Context context = dialog.getContext();
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        params.format = PixelFormat.RGBA_8888;
        params.flags = 0;
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;

        final TapTargetView tapTargetView = new TapTargetView(context, windowManager, null, target, listener);
        windowManager.addView(tapTargetView, params);

        return tapTargetView;
    }

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

        /**
         * Signals that the tap target has been dismissed
         * @param userInitiated Whether the user caused this action
         */
        public void onTargetDismissed(TapTargetView view, boolean userInitiated) {}
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

            final float targetAlpha = 0.96f * 255;
            outerCircleRadius = newOuterCircleRadius;
            outerCircleAlpha = (int) Math.min(targetAlpha, (lerpTime * 1.5f * targetAlpha));
            outerCirclePath.reset();
            outerCirclePath.addCircle(outerCircleCenter[0], outerCircleCenter[1], outerCircleRadius, Path.Direction.CW);

            targetCircleAlpha = (int) Math.min(255.0f, (lerpTime * 1.5f * 255.0f));

            if (expanding) {
                targetCircleRadius = TARGET_RADIUS * Math.min(1.0f, lerpTime * 1.5f);
            } else {
                targetCircleRadius = TARGET_RADIUS * lerpTime;
                targetCirclePulseRadius *= lerpTime;
            }

            textAlpha = (int) (delayedLerp(lerpTime, 0.7f) * 255);

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
                    calculateDrawingBounds();
                    invalidateViewAndOutline(drawingBounds);
                }
            })
            .build();

    final ValueAnimator dismissAnimation = new FloatValueAnimatorBuilder(true)
            .duration(250)
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
                    parent.removeView(TapTargetView.this);
                    onDismiss();
                }
            })
            .build();

    private final ValueAnimator dismissConfirmAnimation = new FloatValueAnimatorBuilder()
            .duration(250)
            .interpolator(new AccelerateDecelerateInterpolator())
            .onUpdate(new FloatValueAnimatorBuilder.UpdateListener() {
                @Override
                public void onUpdate(float lerpTime) {
                    final float spedUpLerp = Math.min(1.0f, lerpTime * 2.0f);
                    outerCircleRadius = calculatedOuterCircleRadius * (1.0f + (spedUpLerp * 0.2f));
                    outerCircleAlpha = (int) ((1.0f - spedUpLerp) * 255.0f);
                    outerCirclePath.reset();
                    outerCirclePath.addCircle(outerCircleCenter[0], outerCircleCenter[1], outerCircleRadius, Path.Direction.CW);
                    targetCircleRadius = (1.0f - lerpTime) * TARGET_RADIUS;
                    targetCircleAlpha = (int) ((1.0f - lerpTime) * 255.0f);
                    targetCirclePulseRadius = (1.0f + lerpTime) * TARGET_RADIUS;
                    targetCirclePulseAlpha = (int) ((1.0f - lerpTime) * targetCirclePulseAlpha);
                    textAlpha = (int) ((1.0f - spedUpLerp) * 255.0f);
                    calculateDrawingBounds();
                    invalidateViewAndOutline(drawingBounds);
                }
            })
            .onEnd(new FloatValueAnimatorBuilder.EndListener() {
                @Override
                public void onEnd() {
                    parent.removeView(TapTargetView.this);
                    onDismiss();
                }
            })
            .build();

    private ValueAnimator[] animators = new ValueAnimator[]
            {expandAnimation, pulseAnimation, dismissConfirmAnimation, dismissAnimation};

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
    public TapTargetView(Context context,
                  final ViewManager parent,
                  @Nullable final ViewGroup boundingParent,
                  final TapTarget target,
                  @Nullable final Listener userListener) {
        super(context);
        if (target == null) throw new IllegalArgumentException("Target cannot be null");

        this.target = target;
        this.parent = parent;
        this.boundingParent = boundingParent;
        this.listener = userListener != null ? userListener : new Listener();
        this.title = target.title;
        this.description = target.description;

        TARGET_PADDING = UiUtil.dp(context, 20);
        CIRCLE_PADDING = UiUtil.dp(context, 40);
        TARGET_RADIUS = UiUtil.dp(context, 44);
        TEXT_PADDING = UiUtil.dp(context, 40);
        TEXT_SPACING = UiUtil.dp(context, 8);
        GUTTER_DIM = UiUtil.dp(context, 88);
        SHADOW_DIM = UiUtil.dp(context, 8);
        TARGET_PULSE_RADIUS = (int) (0.1f * TARGET_RADIUS);

        outerCirclePath = new Path();
        targetBounds = new Rect();
        drawingBounds = new Rect();

        titlePaint = new TextPaint();
        titlePaint.setTextSize(UiUtil.sp(context, target.titleTextSize));
        titlePaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        titlePaint.setAntiAlias(true);

        descriptionPaint = new TextPaint();
        descriptionPaint.setTextSize(UiUtil.sp(context, target.descriptionTextSize));
        descriptionPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        descriptionPaint.setAntiAlias(true);
        descriptionPaint.setAlpha((int) (0.54f * 255.0f));

        outerCirclePaint = new Paint();
        outerCirclePaint.setAntiAlias(true);
        outerCirclePaint.setAlpha((int) (0.96f * 255.0f));

        outerCircleShadowPaint = new Paint();
        outerCircleShadowPaint.setAntiAlias(true);
        outerCircleShadowPaint.setAlpha(50);
        outerCircleShadowPaint.setShadowLayer(10.0f, 0.0f, 25.0f, Color.BLACK);

        targetCirclePaint = new Paint();
        targetCirclePaint.setAntiAlias(true);

        targetCirclePulsePaint = new Paint();
        targetCirclePulsePaint.setAntiAlias(true);

        debugPaint = new Paint();
        debugPaint.setColor(0xFFFF0000);
        debugPaint.setStyle(Paint.Style.STROKE);

        applyTargetOptions(context);

        ViewUtil.onLaidOut(this, new Runnable() {
            @Override
            public void run() {
                updateTextLayouts();
                target.onReady(new Runnable() {
                    @Override
                    public void run() {
                        final int[] offset = new int[2];

                        targetBounds.set(target.bounds());

                        getLocationOnScreen(offset);
                        targetBounds.offset(-offset[0], -offset[1]);

                        if (boundingParent != null) {
                            final Rect rect = new Rect();
                            boundingParent.getWindowVisibleDisplayFrame(rect);
                            topBoundary = rect.top;
                            bottomBoundary = rect.bottom;
                        }

                        drawTintedTarget();
                        calculateDimensions();
                        expandAnimation.start();
                        visible = true;
                        requestFocus();
                    }
                });
            }
        });

        setFocusableInTouchMode(true);
        setClickable(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener == null || outerCircleCenter == null) return;

                if (targetBounds.contains((int) lastTouchX, (int) lastTouchY)) {
                    listener.onTargetClick(TapTargetView.this);
                } else if (!isCanceled && cancelable
                        && distance(outerCircleCenter[0], outerCircleCenter[1],
                        (int) lastTouchX, (int) lastTouchY) > outerCircleRadius) {
                    isCanceled = true;
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
        shouldTintTarget = target.tintTarget;
        shouldDrawShadow = target.drawShadow;
        cancelable = target.cancelable;

        if (shouldDrawShadow && Build.VERSION.SDK_INT >= 21) {
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

        if ((shouldDrawShadow && outlineProvider == null) || Build.VERSION.SDK_INT < 18) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        } else {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }

        final Resources.Theme theme = context.getTheme();
        isDark = UiUtil.themeIntAttr(context, "isLightTheme") == 0;

        if (target.outerCircleColor != UNSET_COLOR) {
            outerCirclePaint.setColor(UiUtil.getColor(context, target.outerCircleColor));
        } else if (theme != null) {
            outerCirclePaint.setColor(UiUtil.themeIntAttr(context, "colorPrimary"));
        } else {
            outerCirclePaint.setColor(Color.WHITE);
        }

        if (target.targetCircleColor != UNSET_COLOR) {
            targetCirclePaint.setColor(UiUtil.getColor(context, target.targetCircleColor));
        } else {
            targetCirclePaint.setColor(isDark ? Color.BLACK : Color.WHITE);
        }

        if (target.transparentTarget) {
            targetCirclePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        targetCirclePulsePaint.setColor(targetCirclePaint.getColor());

        if (target.dimColor != UNSET_COLOR) {
            dimColor = UiUtil.setAlpha(UiUtil.getColor(context, target.dimColor), 0.3f);
        } else {
            dimColor = -1;
        }

        if (target.titleTextColor != UNSET_COLOR) {
            titlePaint.setColor(UiUtil.getColor(context, target.titleTextColor));
        } else {
            titlePaint.setColor(isDark ? Color.BLACK : Color.WHITE);
        }

        if (target.descriptionTextColor != UNSET_COLOR) {
            descriptionPaint.setColor(UiUtil.getColor(context, target.descriptionTextColor));
        } else {
            descriptionPaint.setColor(titlePaint.getColor());
        }

        if (target.typeface != null) {
            titlePaint.setTypeface(target.typeface);
            descriptionPaint.setTypeface(target.typeface);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onDismiss(false);
    }

    void onDismiss() {
        onDismiss(true);
    }

    void onDismiss(boolean userInitiated) {
        if (isDismissed) return;

        isDismissed = true;

        for (final ValueAnimator animator : animators) {
            animator.cancel();
            animator.removeAllUpdateListeners();
        }

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
            saveCount = c.save(); {
                c.clipPath(outerCirclePath, Region.Op.DIFFERENCE);
                outerCircleShadowPaint.setAlpha((int) (0.20f * outerCircleAlpha));
                c.drawCircle(outerCircleCenter[0], outerCircleCenter[1], outerCircleRadius, outerCircleShadowPaint);
            } c.restoreToCount(saveCount);
        }
        c.drawCircle(outerCircleCenter[0], outerCircleCenter[1], outerCircleRadius, outerCirclePaint);

        targetCirclePaint.setAlpha(targetCircleAlpha);
        if (targetCirclePulseAlpha > 0) {
            targetCirclePulsePaint.setAlpha(targetCirclePulseAlpha);
            c.drawCircle(targetBounds.centerX(), targetBounds.centerY(),
                    targetCirclePulseRadius, targetCirclePulsePaint);
        }
        c.drawCircle(targetBounds.centerX(), targetBounds.centerY(),
                targetCircleRadius, targetCirclePaint);

        saveCount = c.save(); {
            c.clipPath(outerCirclePath);
            c.translate(textBounds.left, textBounds.top);
            titlePaint.setAlpha(textAlpha);
            titleLayout.draw(c);

            if (descriptionLayout != null) {
                c.translate(0, titleLayout.getHeight() + TEXT_SPACING);
                descriptionPaint.setAlpha((int) (0.54f * textAlpha));
                descriptionLayout.draw(c);
            }
        } c.restoreToCount(saveCount);

        saveCount = c.save(); {
            if (tintedTarget != null) {
                c.translate(targetBounds.centerX() - tintedTarget.getWidth() / 2,
                            targetBounds.centerY() - tintedTarget.getHeight() / 2);
                c.drawBitmap(tintedTarget, 0, 0, targetCirclePaint);
            } else if (target.icon != null) {
                c.translate(targetBounds.centerX() - target.icon.getBounds().width() / 2,
                            targetBounds.centerY() - target.icon.getBounds().height() / 2);
                target.icon.setAlpha(targetCirclePaint.getAlpha());
                target.icon.draw(c);
            }
        }
        c.restoreToCount(saveCount);

        if (debug) {
            c.drawRect(textBounds, debugPaint);
            c.drawRect(targetBounds, debugPaint);
            c.drawCircle(outerCircleCenter[0], outerCircleCenter[1], 10, debugPaint);
            c.drawCircle(outerCircleCenter[0], outerCircleCenter[1], calculatedOuterCircleRadius - CIRCLE_PADDING, debugPaint);
            c.drawCircle(targetBounds.centerX(), targetBounds.centerY(), TARGET_RADIUS + TARGET_PADDING, debugPaint);
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
        if (isVisible() && !isCanceled && cancelable
                && keyCode == KeyEvent.KEYCODE_BACK && event.isTracking() && !event.isCanceled()) {
            isCanceled = true;

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

    void drawTintedTarget() {
        final Drawable icon = target.icon;
        if (!shouldTintTarget || icon == null) {
            tintedTarget = null;
            return;
        }

        tintedTarget = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(tintedTarget);
        icon.setColorFilter(new PorterDuffColorFilter(
                outerCirclePaint.getColor(), PorterDuff.Mode.SRC_ATOP));
        icon.draw(canvas);
        icon.setColorFilter(null);
    }

    void updateTextLayouts() {
        final int textWidth = getMeasuredWidth() - TEXT_PADDING * 2;
        titleLayout = new StaticLayout(title, titlePaint, textWidth,
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        if (description != null) {
            descriptionLayout = new StaticLayout(description, descriptionPaint, textWidth,
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
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
        calculatedOuterCircleRadius = getOuterCircleRadius(outerCircleCenter[0], outerCircleCenter[1], textBounds, targetBounds);
    }

    void calculateDrawingBounds() {
        drawingBounds.left = (int) Math.max(0, outerCircleCenter[0] - outerCircleRadius);
        drawingBounds.top = (int) Math.min(0, outerCircleCenter[1] - outerCircleRadius);
        drawingBounds.right = (int) Math.min(getWidth(),
                outerCircleCenter[0] + outerCircleRadius + CIRCLE_PADDING);
        drawingBounds.bottom = (int) Math.min(getHeight(),
                outerCircleCenter[1] + outerCircleRadius + CIRCLE_PADDING);
    }

    int getOuterCircleRadius(int centerX, int centerY, Rect textBounds, Rect targetBounds) {
        final Rect expandedBounds = new Rect(targetBounds);
        expandedBounds.inset(-TARGET_PADDING / 2, -TARGET_PADDING / 2);

        final int textRadius = maxDistanceToPoints(centerX, centerY, textBounds);
        final int targetRadius = maxDistanceToPoints(centerX, centerY, expandedBounds);
        return Math.max(textRadius, targetRadius) + CIRCLE_PADDING;
    }

    Rect getTextBounds() {
        final int totalTextHeight = getTotalTextHeight();
        final int totalTextWidth = getTotalTextWidth();

        final int possibleTop = targetBounds.centerY() - TARGET_RADIUS - TARGET_PADDING - totalTextHeight;
        final int top;
        if (possibleTop > topBoundary) {
            top = possibleTop;
        } else {
            top = targetBounds.centerY() + TARGET_RADIUS + TARGET_PADDING;
        }

        return new Rect(TEXT_PADDING, top, TEXT_PADDING + totalTextWidth, top + totalTextHeight);
    }

    int[] getOuterCircleCenterPoint() {
        if (inGutter(targetBounds.centerY())) {
            return new int[] {targetBounds.centerX(), targetBounds.centerY()};
        }

        final int targetRadius = Math.max(targetBounds.width(), targetBounds.height()) / 2 + TARGET_PADDING;
        final int totalTextHeight = getTotalTextHeight();

        final boolean onTop = targetBounds.centerY() - TARGET_RADIUS - TARGET_PADDING - totalTextHeight > 0;

        final int left = Math.min(TEXT_PADDING, targetBounds.left - targetRadius);
        final int right = Math.max(getWidth() - TEXT_PADDING, targetBounds.right + targetRadius);
        final int centerY = onTop ?
                targetBounds.centerY() - TARGET_RADIUS - TARGET_PADDING - totalTextHeight + titleLayout.getHeight()
                :
                targetBounds.centerY() + TARGET_RADIUS + TARGET_PADDING + titleLayout.getHeight();

        return new int[] {(left + right) / 2, centerY};
    }

    int getTotalTextHeight() {
        if (descriptionLayout == null) {
            return titleLayout.getHeight() + TEXT_SPACING;
        }

        return titleLayout.getHeight() + descriptionLayout.getHeight() + TEXT_SPACING;
    }

    int getTotalTextWidth() {
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
