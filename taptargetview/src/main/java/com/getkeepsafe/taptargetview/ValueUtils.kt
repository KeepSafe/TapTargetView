@file:JvmName("ValueExtensions")
package com.getkeepsafe.taptargetview


internal val Float.halfwayLerp: Float
    get() {
        if (this < 0.5f) return this / 0.5f
        return (1.0f - this) / 0.5f
    }

internal fun Float.getDelayLerp(threshold: Float): Float {
    if (this < threshold) return 0f
    return (this - threshold) / (1.0f - threshold)
}