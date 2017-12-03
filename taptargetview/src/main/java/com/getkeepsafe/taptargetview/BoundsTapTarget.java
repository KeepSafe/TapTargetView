package com.getkeepsafe.taptargetview;

import android.content.Context;
import android.graphics.Rect;

class BoundsTapTarget extends TapTarget {
  BoundsTapTarget(Rect rect, Parameters parameters) {
    super(parameters);
    setBounds(rect);
  }

  public static class Builder extends TapTarget.Builder {
    private final Rect rect;

    Builder(Context context, Rect rect) {
      super(context);
      if (rect == null) {
        throw new IllegalArgumentException("Given null bounds to target");
      }
      this.rect = rect;
    }

    @Override
    public BoundsTapTarget build() {
      return new BoundsTapTarget(rect, parameters);
    }
  }
}
