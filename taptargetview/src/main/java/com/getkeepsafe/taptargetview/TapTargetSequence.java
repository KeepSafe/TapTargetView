package com.getkeepsafe.taptargetview;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TapTargetSequence {
    private final Activity activity;
    private final List<TapTargetView.Options> targets;

    public TapTargetSequence(Activity activity) {
        if (activity == null) throw new IllegalArgumentException("Activity is null");
        this.activity = activity;
        this.targets = new ArrayList<>();
    }

    public TapTargetSequence targets(List<TapTargetView.Options> targets) {
        this.targets.addAll(targets);
        return this;
    }

    public TapTargetSequence targets(TapTargetView.Options... targets) {
        Collections.addAll(this.targets, targets);
        return this;
    }

    public TapTargetSequence target(TapTargetView.Options target) {
        this.targets.add(target);
        return this;
    }

    public void start() {
        
    }
}
