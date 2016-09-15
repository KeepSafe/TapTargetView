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
import android.view.ViewGroup;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

public class TapTargetSequence {
    private final Activity activity;
    private final ViewGroup container;
    private final Queue<TapTarget> targets;
    private Listener listener;
    private boolean continueOnCancel;
    private boolean started;

    public interface Listener {
        void onSequenceFinish();
        void onSequenceCanceled();
    }

    public TapTargetSequence(Activity activity) {
        if (activity == null) throw new IllegalArgumentException("Activity is null");
        this.activity = activity;
        this.container = null;
        this.targets = new LinkedList<>();
    }

    public TapTargetSequence(ViewGroup container) {
        this.container = container;
        this.activity = null;
        this.targets = new LinkedList<>();
    }

    public TapTargetSequence targets(List<TapTarget> targets) {
        this.targets.addAll(targets);
        return this;
    }

    public TapTargetSequence targets(TapTarget... targets) {
        Collections.addAll(this.targets, targets);
        return this;
    }

    public TapTargetSequence target(TapTarget target) {
        this.targets.add(target);
        return this;
    }

    public TapTargetSequence continueOnCancel(boolean status) {
        this.continueOnCancel = status;
        return this;
    }

    public TapTargetSequence listener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public void start() {
        if (targets.isEmpty() || started) {
            return;
        }

        started = true;
        showNext();
    }

    private void showNext() {
        try {
            if (activity != null)
                TapTargetView.showFor(activity, targets.remove(), tapTargetListener);
            else
                TapTargetView.showFor(container, targets.remove(), tapTargetListener);
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
            showNext();
        }

        @Override
        public void onTargetCancel(TapTargetView view) {
            super.onTargetCancel(view);
            if (continueOnCancel) {
                showNext();
            } else {
                if (listener != null) {
                    listener.onSequenceCanceled();
                }
            }
        }
    };
}
