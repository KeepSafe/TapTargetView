@file:Suppress("unused")
package com.getkeepsafe.taptargetview.target

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.getkeepsafe.taptargetview.dp

abstract class TapTargetShapeType {

    var textPadding = 40.dp

    var circlePadding = 40.dp

    var textSpacing = 8.dp

    var textMaxWidth = 360.dp

    var targetPadding = 20.dp

    var textPositionBias = 20.dp


    companion object {

        val Circle = CircleShapeTapTarget()

        val RectAngle = RectAngleShapeType()

        fun RectAngle(roundRadius: Int): RectAngleShapeType {
            val rectangleType = RectAngleShapeType()
            rectangleType.roundRadius = roundRadius
            return rectangleType
        }

    }


    abstract val edgeLength: Int

    open fun initResource(context: Context) {}

    abstract fun expandContractChange(lerpTime: Float, isExpanding: Boolean)

    open fun pulseAnimation(lerpTime: Float) {}

    abstract fun dismissConfirmAnimation(lerpTime: Float)

    abstract fun drawTarget(
        canvas: Canvas,
        targetBounds: Rect,
        paint: Paint
    )

    open fun drawPulse(
        canvas: Canvas,
        targetPulseAlpha: Float,
        targetBounds: Rect,
        paint: Paint
    ) {}

    open fun drawInformation(canvas: Canvas, targetBounds: Rect, paint: Paint) {}

    abstract fun clickInTarget(targetBounds: Rect, lastTouchX: Int, lastTouchY: Int): Boolean

    open fun onReadyTarget(bounds: Rect?) {}

    open fun getTextBounds(
        totalTextHeight: Int,
        totalTextWidth: Int,
        targetBounds: Rect,
        topBoundary: Int,
        viewWidth: Int
    ): Rect {
        val verticalLocation = getTextVertical(targetBounds, totalTextHeight, topBoundary)
        val horizontalLocation = getTextHorizontal(targetBounds, totalTextWidth, viewWidth, viewWidth)
        return Rect(
            horizontalLocation.first,
            verticalLocation.first,
            horizontalLocation.second,
            verticalLocation.second
        )
    }

    open fun getTextVertical(
        targetBounds: Rect,
        totalTextHeight: Int,
        topBoundary: Int
    ): Pair<Int, Int> {
        val possibleTop = targetBounds.centerY() - edgeLength - targetPadding - totalTextHeight
        val top =  if (possibleTop > topBoundary) {
            possibleTop
        } else {
            targetBounds.centerY() + edgeLength + targetPadding
        }
        return top to top + totalTextHeight
    }

    open fun getTextHorizontal(
        targetBounds: Rect,
        totalTextWidth: Int,
        leftBoundary: Int,
        viewWidth: Int
    ): Pair<Int, Int> {
        val relativeCenterDistance: Int = viewWidth / 2 - targetBounds.centerX()
        val bias: Int =
            if (relativeCenterDistance < 0) -textPositionBias else textPositionBias
        val left: Int = textPadding.coerceAtLeast(targetBounds.centerX() - bias - totalTextWidth)
        val right = (viewWidth - textPadding).coerceAtMost(left + totalTextWidth)
        return left to right
    }

}

