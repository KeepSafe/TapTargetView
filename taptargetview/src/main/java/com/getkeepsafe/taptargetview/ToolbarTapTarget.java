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

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Stack;

public class ToolbarTapTarget extends ViewTapTarget {
  protected ToolbarTapTarget(Toolbar toolbar, @IdRes int menuItemId,
                             CharSequence title, @Nullable CharSequence description) {
    super(toolbar.findViewById(menuItemId), title, description);
  }

  protected ToolbarTapTarget(android.widget.Toolbar toolbar, @IdRes int menuItemId,
                             CharSequence title, @Nullable CharSequence description) {
    super(toolbar.findViewById(menuItemId), title, description);
  }

  protected ToolbarTapTarget(Toolbar toolbar, boolean findNavView,
                             CharSequence title, @Nullable CharSequence description) {
    super(findNavView ? findNavView(toolbar) : findOverflowView(toolbar), title, description);
  }

  protected ToolbarTapTarget(android.widget.Toolbar toolbar, boolean findNavView,
                             CharSequence title, @Nullable CharSequence description) {
    super(findNavView ? findNavView(toolbar) : findOverflowView(toolbar), title, description);
  }

  private static ToolbarProxy proxyOf(Object instance) {
    if (instance == null) {
      throw new IllegalArgumentException("Given null instance");
    }

    if (instance instanceof Toolbar) {
      return new SupportToolbarProxy((Toolbar) instance);
    } else if (instance instanceof android.widget.Toolbar) {
      return new StandardToolbarProxy((android.widget.Toolbar) instance);
    }

    throw new IllegalStateException("Couldn't provide proper toolbar proxy instance");
  }

  private static View findNavView(Object instance) {
    final ToolbarProxy toolbar = proxyOf(instance);

    // First we try to find the view via its content description
    final CharSequence currentDescription = toolbar.getNavigationContentDescription();
    final boolean hadContentDescription = !TextUtils.isEmpty(currentDescription);
    final CharSequence sentinel = hadContentDescription ? currentDescription : "taptarget-findme";
    toolbar.setNavigationContentDescription(sentinel);

    final ArrayList<View> possibleViews = new ArrayList<>(1);
    toolbar.findViewsWithText(possibleViews, sentinel, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);

    if (!hadContentDescription) {
      toolbar.setNavigationContentDescription(null);
    }

    if (possibleViews.size() > 0) {
      return possibleViews.get(0);
    }

    // If that doesn't work, we try to grab it via matching its drawable
    final Drawable navigationIcon = toolbar.getNavigationIcon();
    if (navigationIcon != null) {
      final int size = toolbar.getChildCount();
      for (int i = 0; i < size; ++i) {
        final View child = toolbar.getChildAt(i);
        if (child instanceof ImageButton) {
          final Drawable childDrawable = ((ImageButton) child).getDrawable();
          if (childDrawable == navigationIcon) {
            return child;
          }
        }
      }
    }

    // If that doesn't work, we fall-back to our last resort solution: Reflection
    // Both the appcompat and standard Toolbar implementations utilize a variable
    // "mNavButtonView" to represent the navigation icon
    try {
      return (View) ReflectUtil.getPrivateField(toolbar.internalToolbar(), "mNavButtonView");
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException("Could not find navigation view for Toolbar!", e);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Unable to access navigation view for Toolbar!", e);
    }
  }

  private static View findOverflowView(Object instance) {
    final ToolbarProxy toolbar = proxyOf(instance);

    // First we try to find the overflow menu view via drawable matching
    final Drawable overflowDrawable = toolbar.getOverflowIcon();
    if (overflowDrawable != null) {
      final Stack<ViewGroup> parents = new Stack<>();
      parents.push((ViewGroup) toolbar.internalToolbar());
      while (!parents.empty()) {
        ViewGroup parent = parents.pop();
        final int size = parent.getChildCount();
        for (int i = 0; i < size; ++i) {
          final View child = parent.getChildAt(i);
          if (child instanceof ViewGroup) {
            parents.push((ViewGroup) child);
            continue;
          }
          if (child instanceof ImageView) {
            final Drawable childDrawable = ((ImageView) child).getDrawable();
            if (childDrawable == overflowDrawable) {
              return child;
            }
          }
        }
      }
    }

    // If that doesn't work, we fall-back to our last resort solution: Reflection
    // Toolbars contain an "ActionMenuView" which in turn contains an "ActionMenuPresenter".
    // The "ActionMenuPresenter" then holds a reference to an "OverflowMenuButton" which is the
    // desired target
    try {
      final Object actionMenuView = ReflectUtil.getPrivateField(toolbar.internalToolbar(), "mMenuView");
      final Object actionMenuPresenter = ReflectUtil.getPrivateField(actionMenuView, "mPresenter");
      return (View) ReflectUtil.getPrivateField(actionMenuPresenter, "mOverflowButton");
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException("Could not find overflow view for Toolbar!", e);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Unable to access overflow view for Toolbar!", e);
    }
  }


  private interface ToolbarProxy {
    CharSequence getNavigationContentDescription();

    void setNavigationContentDescription(CharSequence description);

    void findViewsWithText(ArrayList<View> out, CharSequence toFind, int flags);

    Drawable getNavigationIcon();

    @Nullable
    Drawable getOverflowIcon();

    int getChildCount();

    View getChildAt(int position);

    Object internalToolbar();
  }

  static class SupportToolbarProxy implements ToolbarProxy {
    private final Toolbar toolbar;

    SupportToolbarProxy(Toolbar toolbar) {
      this.toolbar = toolbar;
    }

    @Override
    public CharSequence getNavigationContentDescription() {
      return toolbar.getNavigationContentDescription();
    }

    @Override
    public void setNavigationContentDescription(CharSequence description) {
      toolbar.setNavigationContentDescription(description);
    }

    @Override
    public void findViewsWithText(ArrayList<View> out, CharSequence toFind, int flags) {
      toolbar.findViewsWithText(out, toFind, flags);
    }

    @Override
    public Drawable getNavigationIcon() {
      return toolbar.getNavigationIcon();
    }

    @Override
    public Drawable getOverflowIcon() {
      return toolbar.getOverflowIcon();
    }

    @Override
    public int getChildCount() {
      return toolbar.getChildCount();
    }

    @Override
    public View getChildAt(int position) {
      return toolbar.getChildAt(position);
    }

    @Override
    public Object internalToolbar() {
      return toolbar;
    }
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  static class StandardToolbarProxy implements ToolbarProxy {
    private final android.widget.Toolbar toolbar;

    StandardToolbarProxy(android.widget.Toolbar toolbar) {
      this.toolbar = toolbar;
    }

    @Override
    public CharSequence getNavigationContentDescription() {
      return toolbar.getNavigationContentDescription();
    }

    @Override
    public void setNavigationContentDescription(CharSequence description) {
      toolbar.setNavigationContentDescription(description);
    }

    @Override
    public void findViewsWithText(ArrayList<View> out, CharSequence toFind, int flags) {
      toolbar.findViewsWithText(out, toFind, flags);
    }

    @Override
    public Drawable getNavigationIcon() {
      return toolbar.getNavigationIcon();
    }

    @Nullable
    @Override
    public Drawable getOverflowIcon() {
      if (Build.VERSION.SDK_INT >= 23) {
        return toolbar.getOverflowIcon();
      }

      return null;
    }

    @Override
    public int getChildCount() {
      return toolbar.getChildCount();
    }

    @Override
    public View getChildAt(int position) {
      return toolbar.getChildAt(position);
    }

    @Override
    public Object internalToolbar() {
      return toolbar;
    }
  }
}
