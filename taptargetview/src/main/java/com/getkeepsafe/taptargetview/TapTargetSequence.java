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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Displays a sequence of {@link TapTargetView}s.
 * <p>
 * Internally, a FIFO queue is held to dictate which {@link TapTarget} will be shown.
 */
public class TapTargetSequence {
  private final @Nullable Activity activity;
  private final @Nullable Dialog dialog;
  private final Queue<TapTarget> targets;
  private boolean active;
  private boolean finished;

  @Nullable
  private TapTargetView currentView;

  @Nullable
  private View skipButton;
  /** Holds the skip button in the decor view when showing for an activity */
  @Nullable
  private FrameLayout skipButtonContainer;
  /** Keeps the skip button clear of the system bars as the decor view is laid out again */
  @Nullable
  private View.OnLayoutChangeListener decorLayoutListener;
  private int skipButtonGravity = Gravity.BOTTOM | Gravity.END;

  Listener listener;
  boolean considerOuterCircleCanceled;
  boolean continueOnCancel;

  public interface Listener {
    /** Called when there are no more tap targets to display */
    void onSequenceFinish();

    /**
     * Called when moving onto the next tap target.
     * @param lastTarget The last displayed target
     * @param targetClicked Whether the last displayed target was clicked (this will always be true
     *                      unless you have set {@link #continueOnCancel(boolean)} and the user
     *                      clicks outside of the target
     */
    void onSequenceStep(TapTarget lastTarget, boolean targetClicked);

    /**
     * Called when the user taps outside of the current target, the target is cancelable, and
     * {@link #continueOnCancel(boolean)} is not set. Also called when the sequence is ended by
     * {@link #cancel()} or {@link #skip()}, including via the skip button.
     * @param lastTarget The last displayed target
     */
    void onSequenceCanceled(TapTarget lastTarget);
  }

  public TapTargetSequence(Activity activity) {
    if (activity == null) throw new IllegalArgumentException("Activity is null");
    this.activity = activity;
    this.dialog = null;
    this.targets = new LinkedList<>();
  }

  public TapTargetSequence(Dialog dialog) {
    if (dialog == null) throw new IllegalArgumentException("Given null Dialog");
    this.dialog = dialog;
    this.activity = null;
    this.targets = new LinkedList<>();
  }

  /** Adds the given targets, in order, to the pending queue of {@link TapTarget}s */
  public TapTargetSequence targets(List<TapTarget> targets) {
    this.targets.addAll(targets);
    return this;
  }

  /** Adds the given targets, in order, to the pending queue of {@link TapTarget}s */
  public TapTargetSequence targets(TapTarget... targets) {
    Collections.addAll(this.targets, targets);
    return this;
  }

  /** Adds the given target to the pending queue of {@link TapTarget}s */
  public TapTargetSequence target(TapTarget target) {
    this.targets.add(target);
    return this;
  }

  /** Whether or not to continue the sequence when a {@link TapTarget} is canceled **/
  public TapTargetSequence continueOnCancel(boolean status) {
    this.continueOnCancel = status;
    return this;
  }

  /** Whether or not to consider taps on the outer circle as a cancellation **/
  public TapTargetSequence considerOuterCircleCanceled(boolean status) {
    this.considerOuterCircleCanceled = status;
    return this;
  }

  /** Specify the listener for this sequence **/
  public TapTargetSequence listener(Listener listener) {
    this.listener = listener;
    return this;
  }

  /**
   * Shows a button with the given text above the tap targets for as long as the sequence is
   * running. Tapping it calls {@link #skip()}, ending the whole sequence.
   */
  public TapTargetSequence skipButton(CharSequence text) {
    final Button button = new Button(getContext());
    button.setText(text);
    return skipButton(button);
  }

  /**
   * Like {@link #skipButton(CharSequence)}, but uses the given view, so you can style it however
   * you like. The sequence replaces the view's click listener and layout params. The view must not
   * already have a parent. Pass {@code null} to remove a previously set skip button.
   */
  public TapTargetSequence skipButton(@Nullable View view) {
    if (view == skipButton) {
      // Already set, and possibly showing, in which case it has a parent
      return this;
    }
    if (view != null && view.getParent() != null) {
      throw new IllegalArgumentException("Skip button view must not already have a parent");
    }

    removeSkipButton();
    this.skipButton = view;
    if (view != null) {
      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          skip();
        }
      });

      // If the sequence is already running, show the new button straight away
      if (active && !finished && currentView != null) {
        showSkipButton();
      }
    }
    return this;
  }

  /**
   * Where to place the skip button, as a combination of {@link Gravity} constants.
   * Defaults to {@code Gravity.BOTTOM | Gravity.END}.
   */
  public TapTargetSequence skipButtonGravity(int gravity) {
    this.skipButtonGravity = gravity;
    // If the button is already showing, move it straight away
    if (skipButton != null && active && !finished && currentView != null) {
      showSkipButton();
    }
    return this;
  }

  /** Immediately starts the sequence and displays the first target from the queue **/
  @UiThread
  public void start() {
    if (targets.isEmpty() || active) {
      return;
    }

    active = true;
    finished = false;
    showNext();
  }

  /** Immediately starts the sequence from the given targetId's position in the queue */
  public void startWith(int targetId) {
    if (active) {
      return;
    }

    while (targets.peek() != null && targets.peek().id() != targetId) {
      targets.poll();
    }

    TapTarget peekedTarget = targets.peek();
    if (peekedTarget == null || peekedTarget.id() != targetId) {
      throw new IllegalStateException("Given target " + targetId + " not in sequence");
    }

    start();
  }

  /** Immediately starts the sequence at the specified zero-based index in the queue */
  public void startAt(int index) {
    if (active) {
      return;
    }

    if (index < 0 || index >= targets.size()) {
      throw new IllegalArgumentException("Given invalid index " + index);
    }

    final int expectedSize = targets.size() - index;
    while (targets.peek() != null && targets.size() != expectedSize) {
      targets.poll();
    }

    if (targets.size() != expectedSize) {
      throw new IllegalStateException("Given index " + index + " not in sequence");
    }

    start();
  }

  /**
   * Cancels the sequence, if the current target is cancelable.
   * When the sequence is canceled, the current target is dismissed and the remaining targets are
   * removed from the sequence.
   * @return whether the sequence was canceled or not
   */
  @UiThread
  public boolean cancel() {
    if (!active || finished || currentView == null || !currentView.cancelable) {
      return false;
    }
    currentView.dismiss(false);
    active = false;
    targets.clear();
    onSequenceEnded();
    if (listener != null) {
      listener.onSequenceCanceled(currentView.target);
    }
    return true;
  }

  /**
   * Ends the sequence immediately, even if the current target is not cancelable. The current
   * target is dismissed, the remaining targets are removed from the sequence and
   * {@link Listener#onSequenceCanceled(TapTarget)} is called. This is what the skip button does.
   * @return whether the sequence was skipped or not (false if it isn't running)
   */
  @UiThread
  public boolean skip() {
    if (!active || finished || currentView == null) {
      return false;
    }
    final TapTargetView view = currentView;
    view.dismiss(false);
    active = false;
    targets.clear();
    onSequenceEnded();
    if (listener != null) {
      listener.onSequenceCanceled(view.target);
    }
    return true;
  }

  void showNext() {
    try {
      TapTarget tapTarget = targets.remove();
      if (activity != null) {
        currentView = TapTargetView.showFor(activity, tapTarget, tapTargetListener);
      } else {
        currentView = TapTargetView.showFor(dialog, tapTarget, tapTargetListener);
      }
      // Each target is added on top of everything else, so re-add the skip button above it
      showSkipButton();
    } catch (NoSuchElementException e) {
      currentView = null;
      onSequenceEnded();
      // No more targets
      if (listener != null) {
        listener.onSequenceFinish();
      }
    }
  }

  private final TapTargetView.Listener tapTargetListener = new TapTargetView.Listener() {
    @Override
    public void onTargetClick(TapTargetView view) {
      if (isStale(view)) {
        return;
      }
      super.onTargetClick(view);
      if (listener != null) {
        listener.onSequenceStep(view.target, true);
      }
      showNext();
    }

    @Override
    public void onOuterCircleClick(TapTargetView view) {
      if (considerOuterCircleCanceled) {
        onTargetCancel(view);
      }
    }

    @Override
    public void onTargetCancel(TapTargetView view) {
      if (isStale(view)) {
        return;
      }
      super.onTargetCancel(view);
      if (continueOnCancel) {
        if (listener != null) {
          listener.onSequenceStep(view.target, false);
        }
        showNext();
      } else {
        onSequenceEnded();
        if (listener != null) {
          listener.onSequenceCanceled(view.target);
        }
      }
    }
  };

  /**
   * A target dismissed by {@link #skip()} or {@link #cancel()} stays interactive while it animates
   * out, so ignore its taps and back presses rather than stepping or ending the sequence again.
   */
  private boolean isStale(TapTargetView view) {
    return finished || view != currentView;
  }

  private void onSequenceEnded() {
    finished = true;
    removeSkipButton();
  }

  private Context getContext() {
    //noinspection ConstantConditions
    return activity != null ? activity : dialog.getContext();
  }

  private WindowManager getWindowManager() {
    return (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
  }

  /** Adds the skip button on top of the current target, or moves it back on top if already shown */
  private void showSkipButton() {
    if (skipButton == null) {
      return;
    }

    if (activity != null) {
      // TapTargetView adds each target to the decor view, so the skip button goes there too
      final ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
      if (skipButtonContainer == null) {
        // The decor draws and dispatches touches to its children in Z order, not just in the order
        // they were added, and a target drawn with a shadow has an elevation of SHADOW_DIM. So the
        // button sits in a container raised above that. The button itself can't be raised reliably
        // because a Material button's state list animator resets its elevation when pressed.
        // The container has no background, so it casts no shadow of its own.
        skipButtonContainer = new FrameLayout(activity);
        skipButtonContainer.setClipChildren(false);
        skipButtonContainer.setClipToPadding(false);
        // Replace any layout params the view came with, e.g. MATCH_PARENT would stretch it
        skipButtonContainer.addView(skipButton, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
      }
      final FrameLayout container = skipButtonContainer;
      if (currentView != null) {
        ViewCompat.setElevation(container, currentView.SHADOW_DIM + UiUtil.dp(activity, 1));
      }
      ViewUtil.removeView(decor, container);
      decor.addView(container, decorLayoutParams(decor));

      // The decor may not be attached or laid out yet (e.g. when started from onCreate), in which
      // case the system bar insets are unknown, and they can change later (rotation, resizing,
      // bars shown or hidden). So position the button again whenever the decor is laid out.
      if (decorLayoutListener == null) {
        decorLayoutListener = new View.OnLayoutChangeListener() {
          @Override
          public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                     int oldLeft, int oldTop, int oldRight, int oldBottom) {
            // Layout params changed during a layout pass only apply on the next one, so post it
            decor.post(new Runnable() {
              @Override
              public void run() {
                updateSkipButtonLayout(decor);
              }
            });
          }
        };
        decor.addOnLayoutChangeListener(decorLayoutListener);
      }
    } else {
      // For dialogs, each target is its own window, so the skip button needs its own window too.
      // FLAG_NOT_FOCUSABLE lets touches outside the button reach the target window below it.
      final WindowManager windowManager = getWindowManager();
      removeSkipButtonWindow(windowManager);

      final int margin = UiUtil.dp(getContext(), 16);
      final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
      params.type = WindowManager.LayoutParams.TYPE_APPLICATION;
      params.format = PixelFormat.TRANSLUCENT;
      params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
          | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
      params.gravity = skipButtonGravity;
      params.width = WindowManager.LayoutParams.WRAP_CONTENT;
      params.height = WindowManager.LayoutParams.WRAP_CONTENT;
      // With a centered gravity the offset is from the center, so only apply the margin at an edge
      params.x = (skipButtonGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL
          ? 0 : margin;
      params.y = (skipButtonGravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.CENTER_VERTICAL
          ? 0 : margin;
      windowManager.addView(skipButton, params);
    }
  }

  /** Re-positions the skip button in the decor, if it's showing and its position has changed */
  private void updateSkipButtonLayout(ViewGroup decor) {
    final FrameLayout container = skipButtonContainer;
    if (container == null || container.getParent() != decor) {
      return;
    }

    final FrameLayout.LayoutParams current = (FrameLayout.LayoutParams) container.getLayoutParams();
    final FrameLayout.LayoutParams updated = decorLayoutParams(decor);
    // Only set them when they differ, as doing so lays out the decor and calls us again
    if (current.gravity != updated.gravity
        || current.leftMargin != updated.leftMargin || current.topMargin != updated.topMargin
        || current.rightMargin != updated.rightMargin
        || current.bottomMargin != updated.bottomMargin) {
      container.setLayoutParams(updated);
    }
  }

  private FrameLayout.LayoutParams decorLayoutParams(View decor) {
    final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, skipButtonGravity);

    // The decor view extends behind the system bars and display cutout, so keep the button clear
    final Rect insets = new Rect();
    final WindowInsetsCompat rootInsets = ViewCompat.getRootWindowInsets(decor);
    if (rootInsets != null) {
      final Insets bars = rootInsets.getInsets(
          WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
      insets.set(bars.left, bars.top, bars.right, bars.bottom);
    } else if (decor.getWidth() > 0 && decor.getHeight() > 0) {
      // The frame is in screen coordinates, so make it relative to the decor, which isn't at the
      // screen's origin when the window doesn't fill the screen (e.g. a dialog-themed activity)
      final Rect frame = new Rect();
      decor.getWindowVisibleDisplayFrame(frame);
      final int[] decorLocation = new int[2];
      decor.getLocationOnScreen(decorLocation);
      frame.offset(-decorLocation[0], -decorLocation[1]);
      insets.set(Math.max(0, frame.left), Math.max(0, frame.top),
          Math.max(0, decor.getWidth() - frame.right),
          Math.max(0, decor.getHeight() - frame.bottom));
    }

    final int margin = UiUtil.dp(getContext(), 16);
    params.setMargins(margin + insets.left, margin + insets.top,
        margin + insets.right, margin + insets.bottom);
    return params;
  }

  private void removeSkipButton() {
    if (decorLayoutListener != null && activity != null) {
      activity.getWindow().getDecorView().removeOnLayoutChangeListener(decorLayoutListener);
      decorLayoutListener = null;
    }

    if (skipButtonContainer != null) {
      ViewUtil.removeView((ViewGroup) skipButtonContainer.getParent(), skipButtonContainer);
      // Release the button so it can be shown again, or set on another sequence
      skipButtonContainer.removeAllViews();
      skipButtonContainer = null;
    }

    if (dialog != null) {
      removeSkipButtonWindow(getWindowManager());
    }
  }

  private void removeSkipButtonWindow(WindowManager windowManager) {
    if (skipButton == null || skipButton.getParent() == null) {
      return;
    }

    try {
      windowManager.removeViewImmediate(skipButton);
    } catch (Exception ignored) {
      // Already removed, e.g. because the dialog's window went away
    }
  }
}
