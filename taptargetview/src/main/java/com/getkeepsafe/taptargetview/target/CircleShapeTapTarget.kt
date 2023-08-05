package com.getkeepsafe.taptargetview.target

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.getkeepsafe.taptargetview.dp
import com.getkeepsafe.taptargetview.getDelayLerp
import com.getkeepsafe.taptargetview.halfwayLerp
import kotlin.math.pow
import kotlin.math.roundToInt

object CircleShapeTapTarget: TapTargetShapeType {

    private var targetRadius = 44

    private var targetCircleRadius = 0f

    private var targetCirclePulseRadius = 0f

    private var TARGET_RADIUS = 0

    private var TARGET_PULSE_RADIUS = 0


    /**
     * 用于测量文本，圆心等位置的
     */
    override val edgeLength: Int get() = TARGET_RADIUS

    /** Specify the target radius in dp.  */
    fun setTargetRadius(targetRadius: Int): CircleShapeTapTarget {
        this.targetRadius = targetRadius
        return this
    }

    override fun initResource(context: Context) {
        TARGET_RADIUS = targetRadius.dp
        TARGET_PULSE_RADIUS = (TARGET_RADIUS * 0.1f).roundToInt()
    }

    override fun expandContractChange(lerpTime: Float, isExpanding: Boolean) {
        if (isExpanding) {
            targetCircleRadius = TARGET_RADIUS * 1.0f.coerceAtMost(lerpTime * 1.5f)
        } else {
            targetCircleRadius = TARGET_RADIUS * lerpTime
            targetCirclePulseRadius *= lerpTime
        }
    }

    override fun pulseAnimation(lerpTime: Float) {
        targetCirclePulseRadius = (1.0f + lerpTime.getDelayLerp(0.5f)) * TARGET_RADIUS
        targetCircleRadius = TARGET_RADIUS + lerpTime.halfwayLerp * TARGET_PULSE_RADIUS
    }

    override fun dismissConfirmAnimation(lerpTime: Float) {
        targetCircleRadius = (1.0f - lerpTime) * TARGET_RADIUS
        targetCirclePulseRadius = (1.0f + lerpTime) * TARGET_RADIUS
    }

    override fun drawTarget(
        canvas: Canvas,
        targetBounds: Rect,
        paint: Paint
    ) {
        canvas.drawCircle(
            targetBounds.centerX().toFloat(), targetBounds.centerY().toFloat(),
            targetCircleRadius, paint
        )
    }

    override fun drawPulse(
        canvas: Canvas,
        targetPulseAlpha: Float,
        targetBounds: Rect,
        paint: Paint
    ) {
        if (targetPulseAlpha < 0) return
        canvas.drawCircle(
            targetBounds.centerX().toFloat(), targetBounds.centerY().toFloat(),
            targetCirclePulseRadius, paint
        )
    }

    override fun drawInformation(canvas: Canvas, targetBounds: Rect, paint: Paint) {
        canvas.drawCircle(
            targetBounds.centerX().toFloat(),
            targetBounds.centerY().toFloat(),
            TARGET_RADIUS + 20.dp.toFloat(),
            paint
        )
    }

    override fun clickInTarget(targetBounds: Rect, lastTouchX: Int, lastTouchY: Int): Boolean {
        val xPow = (lastTouchX - targetBounds.centerX()).toDouble().pow(2.0)
        val yPow = (lastTouchY - targetBounds.centerY()).toDouble().pow(2.0)
        val sqrt = (xPow + yPow).pow(0.5)
        return sqrt <= targetCircleRadius
    }
}