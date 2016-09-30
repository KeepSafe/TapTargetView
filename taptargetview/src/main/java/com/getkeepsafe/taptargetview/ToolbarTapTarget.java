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

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.lang.reflect.Field;

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

    private static View findNavView(Object toolbar) {
        // Both the appcompat and standard Toolbar implementations utilize a variable
        // "mNavButtonView" to represent the navigation icon
        try {
            final Field navButtonField = toolbar.getClass().getDeclaredField("mNavButtonView");
            navButtonField.setAccessible(true);
            return (View) navButtonField.get(toolbar);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Could not find navigation view for Toolbar!", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access navigation view for Toolbar!", e);
        }
    }

    private static View findOverflowView(Object toolbar) {
        // Toolbars contain an "ActionMenuView" which in turn contains an "ActionMenuPresenter".
        // The "ActionMenuPresenter" then holds a reference to an "OverflowMenuButton" which is the
        // desired target
        try {
            final Field actionMenuField = toolbar.getClass().getDeclaredField("mMenuView");
            actionMenuField.setAccessible(true);
            final Object actionMenuView = actionMenuField.get(toolbar);

            final Field actionMenuPresenterField = actionMenuView.getClass().getDeclaredField("mPresenter");
            actionMenuPresenterField.setAccessible(true);
            final Object actionMenuPresenter = actionMenuPresenterField.get(actionMenuView);

            final Field overflowButtonField = actionMenuPresenter.getClass().getDeclaredField("mOverflowButton");
            overflowButtonField.setAccessible(true);

            return (View) overflowButtonField.get(actionMenuPresenter);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Could not find overflow view for Toolbar!", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access overflow view for Toolbar!", e);
        }
    }
}
