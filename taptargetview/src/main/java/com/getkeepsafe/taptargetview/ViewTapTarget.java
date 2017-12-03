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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

class ViewTapTarget extends TapTarget {
  private final int[] location = new int[2];
  private final View view;
  private final View.OnLayoutChangeListener layoutChangeListener =
      new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(
            View v,
            int left,
            int top,
            int right,
            int bottom,
            int oldLeft,
            int oldTop,
            int oldRight,
            int oldBottom) {
          if (!isReady()) {
            return;
          }

          view.getLocationOnScreen(location);
          Rect bounds =
              new Rect(
                  location[0],
                  location[1],
                  location[0] + view.getWidth(),
                  location[1] + view.getHeight());
          if (param.icon == null) {
            createAndSetIcon();
          }
          setBounds(bounds);
        }
      };

  ViewTapTarget(View view, Parameters parameters) {
    super(parameters);
    if (view == null) {
      throw new IllegalArgumentException("Given null view to target");
    }
    this.view = view;
  }

  protected void createAndSetIcon() {
    Bitmap viewBitmap =
        Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(viewBitmap);
    view.draw(canvas);
    param.icon = new BitmapDrawable(view.getContext().getResources(), viewBitmap);
    param.icon.setBounds(
        0, 0, param.icon.getIntrinsicWidth(), param.icon.getIntrinsicHeight());
  }

  @Override
  public void attach(TapTargetView parent) {
    super.attach(parent);
    view.addOnLayoutChangeListener(layoutChangeListener);
  }

  @Override
  public boolean isReady() {
    return ViewUtil.isLaidOut(view);
  }

  @Override
  protected void detach() {
    super.detach();
    view.removeOnLayoutChangeListener(layoutChangeListener);
  }

  public static class Builder extends TapTarget.Builder {
    protected final View view;

    Builder(View view) {
      super(view.getContext());
      this.view = view;
    }

    @Override
    public ViewTapTarget build() {
      return new ViewTapTarget(view, parameters);
    }
  }
}
