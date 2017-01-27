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

import android.app.Activity;

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
    private final Activity activity;
    private final Queue<TapTarget> targets;
    private boolean started;

    Listener listener;
    boolean considerOuterCircleCanceled;
    boolean continueOnCancel;

    public interface Listener {
        void onSequenceFinish();
        void onSequenceStep(TapTarget lastTarget);
        void onSequenceCanceled(TapTarget lastTarget);
    }

    public TapTargetSequence(Activity activity) {
        if (activity == null) throw new IllegalArgumentException("Activity is null");
        this.activity = activity;
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

    /** Immediately starts the sequence and displays the first target from the queue **/
    public void start() {
        if (targets.isEmpty() || started) {
            return;
        }

        started = true;
        showNext();
    }

    void showNext() {
        try {
            TapTargetView.showFor(activity, targets.remove(), tapTargetListener);
        } catch (NoSuchElementException e) {
            // No more targets
            if (listener != null) {
                listener.onSequenceFinish();
            }
        }
    }

    private final TapTargetView.Listener tapTargetListener = new TapTargetView.Listener() {
        @Override
        public void onTargetClick(TapTargetView view) {
            super.onTargetClick(view);
            if (listener != null) {
                listener.onSequenceStep(view.target);
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
            super.onTargetCancel(view);
            if (continueOnCancel) {
                showNext();
            } else {
                if (listener != null) {
                    listener.onSequenceCanceled(view.target);
                }
            }
        }
    };
}
