package com.getkeepsafe.taptargetview

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.drawToBitmap

open class ViewTapTarget @JvmOverloads constructor(
    private val view: View,
    title: CharSequence,
    description: CharSequence? = null
): TapTarget(title, description) {

    override fun onReady(runnable: Runnable?) {
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
            runnable?.run()
        }
    }

}