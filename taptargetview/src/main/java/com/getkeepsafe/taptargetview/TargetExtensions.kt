@file:Suppress("unused")
package com.getkeepsafe.taptargetview

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import com.getkeepsafe.taptargetview.target.TapTarget

/** Return a tap target for the overflow button from the given toolbar
 *
 *
 * **Note:** This is currently experimental, use at your own risk
 */
@JvmOverloads
fun Toolbar?.createOverflow(
    title: CharSequence?,
    description: CharSequence? = null
): TapTarget {
    return ToolbarTapTarget(this, false, title, description)
}
/** Return a tap target for the overflow button from the given toolbar
 *
 *
 * **Note:** This is currently experimental, use at your own risk
 */
@JvmOverloads
fun android.widget.Toolbar?.createOverflow(
    title: CharSequence?,
    description: CharSequence? = null
): TapTarget {
    return ToolbarTapTarget(this, false, title, description)
}
/** Return a tap target for the navigation button (back, up, etc) from the given toolbar  */
@JvmOverloads
fun Toolbar?.createNavigationIcon(
    title: CharSequence?,
    description: CharSequence? = null
): TapTarget {
    return ToolbarTapTarget(this, true, title, description)
}
/** Return a tap target for the navigation button (back, up, etc) from the given toolbar  */
@JvmOverloads
fun android.widget.Toolbar?.createNavigationIcon(
    title: CharSequence?,
    description: CharSequence? = null
): TapTarget {
    return ToolbarTapTarget(this, true, title, description)
}
/** Return a tap target for the menu item from the given toolbar  */
@JvmOverloads
fun Toolbar?.forToolbarMenuItem(
     @IdRes menuItemId: Int,
    title: CharSequence?, description: CharSequence? = null
): TapTarget {
    return ToolbarTapTarget(this, menuItemId, title, description)
}
/** Return a tap target for the menu item from the given toolbar  */
/** Return a tap target for the menu item from the given toolbar  */
@JvmOverloads
fun android.widget.Toolbar?.forToolbarMenuItem(
    @IdRes menuItemId: Int,
    title: CharSequence?, description: CharSequence? = null
): TapTarget {
    return ToolbarTapTarget(this, menuItemId, title, description)
}
/** Return a tap target for the specified view  */
@JvmOverloads
fun View?.createTarget(
    title: CharSequence,
    description: CharSequence? = null
): TapTarget {
    requireNotNull(this) {
        "Cannot create tap target with null"
    }
    return TapTarget(this, title, description)
}
/** Return a tap target for the specified bounds  */
@JvmOverloads
fun Drawable?.createTarget(
    bounds: Rect?,
    title: CharSequence?,
    description: CharSequence? = null
): TapTarget {
    requireNotNull(this) {
        "Cannot create tap target with null"
    }
    return TapTarget(this, title, description, bounds)
}