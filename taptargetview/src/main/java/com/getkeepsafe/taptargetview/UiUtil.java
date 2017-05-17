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

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.util.TypedValue;

class UiUtil {
    UiUtil() {
    }

    /**
     * Returns the given pixel value in dp
     **/
    static int dp(Context context, int val) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, val, context.getResources().getDisplayMetrics());
    }

    static int fromDp(Context context, int val) {
        double factor = dp(context, 1);
        return (int) (val / factor);
    }

    /**
     * Returns the given pixel value in sp
     **/
    static int sp(Context context, int val) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, val, context.getResources().getDisplayMetrics());
    }

    /**
     * Returns the value of the desired theme integer attribute, or -1 if not found
     **/
    static int themeIntAttr(Context context, String attr) {
        final Resources.Theme theme = context.getTheme();
        if (theme == null) {
            return -1;
        }

        final TypedValue value = new TypedValue();
        final int id = context.getResources().getIdentifier(attr, "attr", context.getPackageName());

        if (id == 0) {
            // Not found
            return -1;
        }

        theme.resolveAttribute(id, value, true);
        return value.data;
    }

    /**
     * Modifies the alpha value of the given ARGB color
     **/
    static int setAlpha(int argb, float alpha) {
        if (alpha > 1.0f) {
            alpha = 1.0f;
        } else if (alpha <= 0.0f) {
            alpha = 0.0f;
        }

        return ((int) ((argb >>> 24) * alpha) << 24) | (argb & 0x00FFFFFF);
    }
}
