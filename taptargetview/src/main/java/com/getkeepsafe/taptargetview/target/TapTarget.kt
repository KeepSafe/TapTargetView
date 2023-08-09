@file:Suppress("unused")
package com.getkeepsafe.taptargetview.target

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.drawToBitmap
import com.getkeepsafe.taptargetview.sp

open class TapTarget {

    private var view: View? = null

    val title: CharSequence?
    val description: CharSequence?
    private var bounds: Rect?
    var icon: Drawable? = null

    internal var tapTargetType: TapTargetShapeType = TapTargetShapeType.Circle

    @JvmOverloads
    constructor(
        view: View,
        title: CharSequence?,
        description: CharSequence?,
        bounds: Rect? = null
    ) {
        this.view = view
        this.title = title
        this.description = description
        this.bounds = bounds
    }

    @JvmOverloads
    constructor(
        icon: Drawable,
        title: CharSequence?,
        description: CharSequence?,
        bounds: Rect? = null,
        iconBounds: Rect? = null
    ) {
        this.icon = icon
        if (iconBounds == null) {
            icon.bounds = Rect(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
        } else {
            icon.bounds = iconBounds
        }
        this.title = title
        this.description = description
        this.bounds = bounds
    }

    var outerCircleAlpha = 0.96f
        private set

    var titleTypeface: Typeface? = null
    var descriptionTypeface: Typeface? = null

    @ColorRes
    private var outerCircleColorRes = -1

    @ColorRes
    private var targetIconColorRes = -1

    @ColorRes
    private var dimColorRes = -1

    @ColorRes
    private var titleTextColorRes = -1

    @ColorRes
    private var descriptionTextColorRes = -1
    private var outerCircleColor: Int? = null
    private var targetIconColor: Int? = null
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
    fun targetIconColor(@ColorRes color: Int): TapTarget {
        targetIconColorRes = color
        return this
    }

    /** Specify the color value for the target circle  */
    fun targetIconColorInt(@ColorInt color: Int): TapTarget {
        targetIconColor = color
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

    /** Specify a unique identifier for this target.  */
    fun id(id: Int): TapTarget {
        this.id = id
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
        val view = this.view ?: kotlin.run {
            runnable?.run()
            tapTargetType.onReadyTarget(bounds)
            return
        }
        view.doOnLayout {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            bounds = Rect(
                location[0],
                location[1],
                location[0] + view.width,
                location[1] + view.height
            )
            if (icon == null && view.width > 0 && view.height > 0) {
                val viewBitmap = view.drawToBitmap()
                val canvas = Canvas(viewBitmap)
                view.draw(canvas)
                val icon = BitmapDrawable(view.context.resources, viewBitmap)
                icon.setBounds(0, 0, view.width, view.height)
                this.icon = icon
            }
            tapTargetType.onReadyTarget(bounds)
            runnable?.run()
        }
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
        return colorResOrInt(context, targetIconColor, targetIconColorRes)
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

    fun setTargetShapeType(type: TapTargetShapeType): TapTarget {
        this.tapTargetType = type
        return this
    }

}