package com.getkeepsafe.taptargetview;

import android.content.Context;
import android.graphics.Rect;

public class BoundsTapTarget extends TapTarget {
  /** Returns a tap target builder for the specified bounds **/
  public static TapTarget.Builder of(Context context, Rect bounds) {
    return new BoundsTapTarget.Builder(context, bounds);
  }

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
