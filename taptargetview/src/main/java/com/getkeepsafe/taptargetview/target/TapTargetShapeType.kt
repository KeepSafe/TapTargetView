@file:Suppress("unused")
package com.getkeepsafe.taptargetview.target

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

interface TapTargetShapeType {

    companion object {

        val Circle = CircleShapeTapTarget

        val RectAngle = RectAngleShapeType

        fun RectAngle(roundRadius: Int): RectAngleShapeType {
            RectAngleShapeType.roundRadius = roundRadius
            return RectAngleShapeType
        }

    }


    val edgeLength: Int

    fun initResource(context: Context) {}

    fun expandContractChange(lerpTime: Float, isExpanding: Boolean)

    fun pulseAnimation(lerpTime: Float) {}

    fun dismissConfirmAnimation(lerpTime: Float)

    fun drawTarget(
        canvas: Canvas,
        targetBounds: Rect,
        paint: Paint
    )

    fun drawPulse(
        canvas: Canvas,
        targetPulseAlpha: Float,
        targetBounds: Rect,
        paint: Paint
    ) {}

    fun drawInformation(canvas: Canvas, targetBounds: Rect, paint: Paint) {}

    fun clickInTarget(targetBounds: Rect, lastTouchX: Int, lastTouchY: Int): Boolean

    fun onReadyTarget(bounds: Rect?) {}

}

