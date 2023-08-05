@file:Suppress("unused")
package com.getkeepsafe.taptargetview

import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

open class TapTarget protected constructor(title: CharSequence?, description: CharSequence?) {
    val title: CharSequence
    val description: CharSequence?
    var outerCircleAlpha = 0.96f
    var targetRadius = 44
    var bounds: Rect? = null
    var icon: Drawable? = null
    var titleTypeface: Typeface? = null
    var descriptionTypeface: Typeface? = null

    @ColorRes
    private var outerCircleColorRes = -1

    @ColorRes
    private var targetCircleColorRes = -1

    @ColorRes
    private var dimColorRes = -1

    @ColorRes
    private var titleTextColorRes = -1

    @ColorRes
    private var descriptionTextColorRes = -1
    private var outerCircleColor: Int? = null
    private var targetCircleColor: Int? = null
    private var dimColor: Int? = null
    private var titleTextColor: Int? = null
    private var descriptionTextColor: Int? = null

    @DimenRes
    private var titleTextDimen = -1

    @DimenRes
    private var descriptionTextDimen = -1
    private var titleTextSize = 20
    private var descriptionTextSize = 18
    private var id = -1
    var drawShadow = false
    var cancelable = true
    var tintTarget = true
    var transparentTarget = false
    var descriptionTextAlpha = 0.54f

    protected constructor(
        bounds: Rect?,
        title: CharSequence?,
        description: CharSequence?
    ): this(title, description) {
        requireNotNull(bounds) { "Cannot pass null bounds or title" }
        this.bounds = bounds
    }

    init {
        requireNotNull(title) { "Cannot pass null title" }
        this.title = title
        this.description = description
    }

    /** Specify whether the target should be transparent  */
    fun transparentTarget(transparent: Boolean): TapTarget {
        transparentTarget = transparent
        return this
    }

    /** Specify the color resource for the outer circle  */
    fun outerCircleColor(@ColorRes color: Int): TapTarget {
        outerCircleColorRes = color
        return this
    }

    /** Specify the color value for the outer circle  */
    fun outerCircleColorInt(@ColorInt color: Int): TapTarget {
        outerCircleColor = color
        return this
    }

    /** Specify the alpha value [0.0, 1.0] of the outer circle  */
    fun outerCircleAlpha(alpha: Float): TapTarget {
        require(!(alpha < 0.0f || alpha > 1.0f)) { "Given an invalid alpha value: $alpha" }
        outerCircleAlpha = alpha
        return this
    }

    /** Specify the color resource for the target circle  */
    fun targetCircleColor(@ColorRes color: Int): TapTarget {
        targetCircleColorRes = color
        return this
    }

    /** Specify the color value for the target circle  */
    fun targetCircleColorInt(@ColorInt color: Int): TapTarget {
        targetCircleColor = color
        return this
    }

    /** Specify the color resource for all text  */
    fun textColor(@ColorRes color: Int): TapTarget {
        titleTextColorRes = color
        descriptionTextColorRes = color
        return this
    }

    /** Specify the color value for all text  */
    fun textColorInt(@ColorInt color: Int): TapTarget {
        titleTextColor = color
        descriptionTextColor = color
        return this
    }

    /** Specify the color resource for the title text  */
    fun titleTextColor(@ColorRes color: Int): TapTarget {
        titleTextColorRes = color
        return this
    }

    /** Specify the color value for the title text  */
    fun titleTextColorInt(@ColorInt color: Int): TapTarget {
        titleTextColor = color
        return this
    }

    /** Specify the color resource for the description text  */
    fun descriptionTextColor(@ColorRes color: Int): TapTarget {
        descriptionTextColorRes = color
        return this
    }

    /** Specify the color value for the description text  */
    fun descriptionTextColorInt(@ColorInt color: Int): TapTarget {
        descriptionTextColor = color
        return this
    }

    /** Specify the typeface for all text  */
    fun textTypeface(typeface: Typeface?): TapTarget {
        requireNotNull(typeface) { "Cannot use a null typeface" }
        titleTypeface = typeface
        descriptionTypeface = typeface
        return this
    }

    /** Specify the typeface for title text  */
    fun titleTypeface(titleTypeface: Typeface?): TapTarget {
        requireNotNull(titleTypeface) { "Cannot use a null typeface" }
        this.titleTypeface = titleTypeface
        return this
    }

    /** Specify the typeface for description text  */
    fun descriptionTypeface(descriptionTypeface: Typeface?): TapTarget {
        requireNotNull(descriptionTypeface) { "Cannot use a null typeface" }
        this.descriptionTypeface = descriptionTypeface
        return this
    }

    /** Specify the text size for the title in SP  */
    fun titleTextSize(sp: Int): TapTarget {
        require(sp >= 0) { "Given negative text size" }
        titleTextSize = sp
        return this
    }

    /** Specify the text size for the description in SP  */
    fun descriptionTextSize(sp: Int): TapTarget {
        require(sp >= 0) { "Given negative text size" }
        descriptionTextSize = sp
        return this
    }

    /**
     * Specify the text size for the title via a dimen resource
     *
     *
     * Note: If set, this value will take precedence over the specified sp size
     */
    fun titleTextDimen(@DimenRes dimen: Int): TapTarget {
        titleTextDimen = dimen
        return this
    }

    /** Specify the alpha value [0.0, 1.0] of the description text  */
    fun descriptionTextAlpha(descriptionTextAlpha: Float): TapTarget {
        require(!(descriptionTextAlpha < 0 || descriptionTextAlpha > 1f)) { "Given an invalid alpha value: $descriptionTextAlpha" }
        this.descriptionTextAlpha = descriptionTextAlpha
        return this
    }

    /**
     * Specify the text size for the description via a dimen resource
     *
     *
     * Note: If set, this value will take precedence over the specified sp size
     */
    fun descriptionTextDimen(@DimenRes dimen: Int): TapTarget {
        descriptionTextDimen = dimen
        return this
    }

    /**
     * Specify the color resource to use as a dim effect
     *
     *
     * **Note:** The given color will have its opacity modified to 30% automatically
     */
    fun dimColor(@ColorRes color: Int): TapTarget {
        dimColorRes = color
        return this
    }

    /**
     * Specify the color value to use as a dim effect
     *
     *
     * **Note:** The given color will have its opacity modified to 30% automatically
     */
    fun dimColorInt(@ColorInt color: Int): TapTarget {
        dimColor = color
        return this
    }

    /** Specify whether or not to draw a drop shadow around the outer circle  */
    fun drawShadow(draw: Boolean): TapTarget {
        drawShadow = draw
        return this
    }

    /** Specify whether or not the target should be cancelable  */
    fun cancelable(status: Boolean): TapTarget {
        cancelable = status
        return this
    }

    /** Specify whether to tint the target's icon with the outer circle's color  */
    fun tintTarget(tint: Boolean): TapTarget {
        tintTarget = tint
        return this
    }
    /**
     * Specify the icon that will be drawn in the center of the target bounds
     * @param hasSetBounds Whether the drawable already has its bounds correctly set. If the
     * drawable does not have its bounds set, then the following bounds will
     * be applied: <br></br>
     * `(0, 0, intrinsic-width, intrinsic-height)`
     */
    @JvmOverloads
    fun icon(icon: Drawable?, hasSetBounds: Boolean = false): TapTarget {
        requireNotNull(icon) { "Cannot use null drawable" }
        this.icon = icon
        if (!hasSetBounds) {
            icon.bounds = Rect(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
        }
        return this
    }

    /** Specify a unique identifier for this target.  */
    fun id(id: Int): TapTarget {
        this.id = id
        return this
    }

    /** Specify the target radius in dp.  */
    fun targetRadius(targetRadius: Int): TapTarget {
        this.targetRadius = targetRadius
        return this
    }

    /** Return the id associated with this tap target  */
    fun id(): Int {
        return id
    }

    /**
     * In case your target needs time to be ready (laid out in your view, not created, etc), the
     * runnable passed here will be invoked when the target is ready.
     */
    open fun onReady(runnable: Runnable?) {
        runnable?.run()
    }

    /**
     * Returns the target bounds. Throws an exception if they are not set
     * (target may not be ready)
     *
     *
     * This will only be called internally when [.onReady] invokes its runnable
     */
    fun bounds(): Rect {
        checkNotNull(bounds) { "Requesting bounds that are not set! Make sure your target is ready" }
        return bounds as Rect
    }

    fun outerCircleColorInt(context: Context): Int? {
        return colorResOrInt(context, outerCircleColor, outerCircleColorRes)
    }

    fun targetCircleColorInt(context: Context): Int? {
        return colorResOrInt(context, targetCircleColor, targetCircleColorRes)
    }

    fun dimColorInt(context: Context): Int? {
        return colorResOrInt(context, dimColor, dimColorRes)
    }

    fun titleTextColorInt(context: Context): Int? {
        return colorResOrInt(context, titleTextColor, titleTextColorRes)
    }

    fun descriptionTextColorInt(context: Context): Int? {
        return colorResOrInt(context, descriptionTextColor, descriptionTextColorRes)
    }

    fun titleTextSizePx(context: Context): Int {
        return dimenOrSize(context, titleTextSize, titleTextDimen)
    }

    fun descriptionTextSizePx(context: Context): Int {
        return dimenOrSize(context, descriptionTextSize, descriptionTextDimen)
    }

    private fun colorResOrInt(context: Context, value: Int?, @ColorRes resource: Int): Int? {
        return if (resource != -1) {
            ContextCompat.getColor(context, resource)
        } else value
    }

    private fun dimenOrSize(context: Context, size: Int, @DimenRes dimen: Int): Int {
        return if (dimen != -1) {
            context.resources.getDimensionPixelSize(dimen)
        } else size.sp
    }

    companion object {
        /** Return a tap target for the overflow button from the given toolbar
         *
         *
         * **Note:** This is currently experimental, use at your own risk
         */
        /**
         * Return a tap target for the overflow button from the given toolbar
         *
         *
         * **Note:** This is currently experimental, use at your own risk
         */
        @JvmOverloads
        fun forToolbarOverflow(
            toolbar: Toolbar?, title: CharSequence?,
            description: CharSequence? = null
        ): TapTarget {
            return ToolbarTapTarget(toolbar, false, title, description)
        }
        /** Return a tap target for the overflow button from the given toolbar
         *
         *
         * **Note:** This is currently experimental, use at your own risk
         */
        /** Return a tap target for the overflow button from the given toolbar
         *
         *
         * **Note:** This is currently experimental, use at your own risk
         */
        @JvmOverloads
        fun forToolbarOverflow(
            toolbar: android.widget.Toolbar?, title: CharSequence?,
            description: CharSequence? = null
        ): TapTarget {
            return ToolbarTapTarget(toolbar, false, title, description)
        }
        /** Return a tap target for the navigation button (back, up, etc) from the given toolbar  */
        /** Return a tap target for the navigation button (back, up, etc) from the given toolbar  */
        @JvmOverloads
        fun forToolbarNavigationIcon(
            toolbar: Toolbar?, title: CharSequence?,
            description: CharSequence? = null
        ): TapTarget {
            return ToolbarTapTarget(toolbar, true, title, description)
        }
        /** Return a tap target for the navigation button (back, up, etc) from the given toolbar  */
        /** Return a tap target for the navigation button (back, up, etc) from the given toolbar  */
        @JvmOverloads
        fun forToolbarNavigationIcon(
            toolbar: android.widget.Toolbar?, title: CharSequence?,
            description: CharSequence? = null
        ): TapTarget {
            return ToolbarTapTarget(toolbar, true, title, description)
        }
        /** Return a tap target for the menu item from the given toolbar  */
        /** Return a tap target for the menu item from the given toolbar  */
        @JvmOverloads
        fun forToolbarMenuItem(
            toolbar: Toolbar?, @IdRes menuItemId: Int,
            title: CharSequence?, description: CharSequence? = null
        ): TapTarget {
            return ToolbarTapTarget(toolbar, menuItemId, title, description)
        }
        /** Return a tap target for the menu item from the given toolbar  */
        /** Return a tap target for the menu item from the given toolbar  */
        @JvmOverloads
        fun forToolbarMenuItem(
            toolbar: android.widget.Toolbar?, @IdRes menuItemId: Int,
            title: CharSequence?, description: CharSequence? = null
        ): TapTarget {
            return ToolbarTapTarget(toolbar, menuItemId, title, description)
        }
        /** Return a tap target for the specified view  */
        /** Return a tap target for the specified view  */
        @JvmOverloads
        fun forView(
            view: View,
            title: CharSequence,
            description: CharSequence? = null
        ): TapTarget {
            return ViewTapTarget(view, title, description)
        }
        /** Return a tap target for the specified bounds  */
        /** Return a tap target for the specified bounds  */
        @JvmOverloads
        fun forBounds(
            bounds: Rect?,
            title: CharSequence?,
            description: CharSequence? = null
        ): TapTarget {
            return TapTarget(bounds, title, description)
        }
    }
}