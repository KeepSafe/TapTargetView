@file:Suppress("unused")
package com.getkeepsafe.taptargetview.target

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.getkeepsafe.taptargetview.dp
import com.getkeepsafe.taptargetview.getDelayLerp
import com.getkeepsafe.taptargetview.halfwayLerp

object RectAngleShapeType : TapTargetShapeType {

    private var width = 0

    private var height = 0

    private var drawWidth = 0f

    private var drawHeight = 0f

    private var pulseLength = 4.dp

    private var drawPulseWidth= 0f

    private var drawPulseHeight = 0f

    internal var roundRadius = 8.dp

    override val edgeLength: Int
        get() = 8.dp

    override fun onReadyTarget(bounds: Rect?) {
        checkNotNull(bounds)
        this.width = bounds.width()
        this.height = bounds.height()
    }

    override fun expandContractChange(lerpTime: Float, isExpanding: Boolean) {
        if (isExpanding) {
            drawHeight = height * 1.0f.coerceAtMost(lerpTime * 1.5f)
            drawWidth = width * 1.0f.coerceAtMost(lerpTime * 1.5f)
        } else {
            drawHeight = height * lerpTime
            drawWidth = width * lerpTime
            drawPulseWidth *= lerpTime
            drawPulseHeight *= lerpTime
        }
    }

    override fun dismissConfirmAnimation(lerpTime: Float) {
        drawHeight = height * (1.0f - lerpTime)
        drawWidth = width * (1.0f - lerpTime)
        drawPulseWidth = (1.0f + lerpTime) * width
        drawPulseHeight = (1.0f + lerpTime) * height
    }

    override fun pulseAnimation(lerpTime: Float) {
        drawWidth = width + lerpTime.halfwayLerp * pulseLength
        drawHeight = height + lerpTime.halfwayLerp * pulseLength
        drawPulseHeight = (1.0f + lerpTime.getDelayLerp(0.5f)) * height
        drawPulseWidth = (1.0f + lerpTime.getDelayLerp(0.5f)) * width
    }

    override fun drawTarget(canvas: Canvas, targetBounds: Rect, paint: Paint) {
        canvas.drawRoundRect(
            targetBounds.toTargetRectF(
                drawWidth, drawHeight
            ),
            roundRadius.toFloat(),
            roundRadius.toFloat(),
            paint
        )
    }

    private fun Rect.toTargetRectF(
        width: Float,
        height: Float
    ): RectF {
        val centerX = centerX()
        val centerY = centerY()
        val right = width * 0.5f + roundRadius / 2
        val bottom = height * 0.5f + roundRadius / 2
        return RectF(
            centerX - right,
            centerY - bottom,
            centerX + right,
            centerY + bottom
        )
    }

    override fun drawPulse(
        canvas: Canvas,
        targetPulseAlpha: Float,
        targetBounds: Rect,
        paint: Paint
    ) {
        if (targetPulseAlpha < 0) return
        canvas.drawRoundRect(
            targetBounds.toTargetRectF(
                drawPulseWidth, drawPulseHeight
            ),
            roundRadius.toFloat(),
            roundRadius.toFloat(),
            paint
        )
    }

    override fun clickInTarget(targetBounds: Rect, lastTouchX: Int, lastTouchY: Int): Boolean {
        return targetBounds.contains(lastTouchX, lastTouchY)
    }
}