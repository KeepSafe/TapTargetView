@file:Suppress("unused")
package com.getkeepsafe.taptargetview

import android.app.Activity
import android.app.Dialog
import androidx.annotation.UiThread
import com.getkeepsafe.taptargetview.target.TapTarget
import java.util.LinkedList
import java.util.Queue

class TapTargetSequence: TapTargetView.Listener {

    private val targets: Queue<TapTarget> = LinkedList()

    private var activity: Activity? = null

    private var dialog: Dialog? = null

    private var isActive: Boolean = false

    private var listener: Listener? = null

    private var considerOuterCircleCanceled = false

    private var continueOnCancel = false

    private var currentView: TapTargetView? = null

    constructor(activity: Activity?) {
        this.activity = activity
        this.dialog = null
    }

    constructor(dialog: Dialog?) {
        this.dialog = dialog
        this.activity = null
    }

    fun targets(targets: Iterable<TapTarget>): TapTargetSequence {
        this.targets.addAll(targets)
        return this
    }

    fun addTarget(vararg targets: TapTarget): TapTargetSequence {
        this.targets.addAll(targets)
        return this
    }

    fun continueOnCancel(status: Boolean): TapTargetSequence {
        this.continueOnCancel = status
        return this
    }

    fun considerOuterCircleCanceled(status: Boolean): TapTargetSequence {
        considerOuterCircleCanceled = status
        return this
    }

    /** Specify the listener for this sequence  */
    fun listener(listener: Listener?): TapTargetSequence {
        this.listener = listener
        return this
    }


    @UiThread
    fun start() {
        if (targets.isEmpty() || isActive) return
        isActive = true
        showNext()
    }

    @UiThread
    fun cancel(): Boolean {
        val currentView = this.currentView ?: return false
        if (!isActive || !currentView.cancelable) return false
        currentView.dismiss(false)
        isActive = false
        targets.clear()
        listener?.onSequenceCanceled(currentView.target)
        return true
    }

    fun startAt(index: Int) {
        if (isActive) return
        if (index < 0  || index >= targets.size) return
        val expectedSize = targets.size - index
        repeat(index) {
            targets.poll()
        }
        check(targets.size == expectedSize) { "Given index $index not in sequence" }
        start()
    }

    private fun showNext() {
        val target = targets.poll()
        if (target == null) {
            currentView = null
            listener?.onSequenceFinish()
            return
        }
        currentView = activity?.let { it.showGuideView(target, this) }
        currentView = dialog?.let { it.showGuideView(target, this) }
    }

    override fun onTargetClick(view: TapTargetView?) {
        super.onTargetClick(view)
        listener?.onSequenceStep(view?.target, true)
        showNext()
    }

    override fun onTargetCancel(view: TapTargetView?) {
        super.onTargetCancel(view)
        if (continueOnCancel) {
            listener?.onSequenceStep(view?.target, false)
            showNext()
        } else {
            listener?.onSequenceCanceled(view?.target)
        }
    }

    override fun onOuterCircleClick(view: TapTargetView?) {
        if (considerOuterCircleCanceled) onTargetCancel(view)
    }

    interface Listener {
        /** Called when there are no more tap targets to display  */
        fun onSequenceFinish()

        /**
         * Called when moving onto the next tap target.
         * @param lastTarget The last displayed target
         * @param targetClicked Whether the last displayed target was clicked (this will always be true
         * unless you have set [.continueOnCancel] and the user
         * clicks outside of the target
         */
        fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean)

        /**
         * Called when the user taps outside of the current target, the target is cancelable, and
         * [.continueOnCancel] is not set.
         * @param lastTarget The last displayed target
         */
        fun onSequenceCanceled(lastTarget: TapTarget?)
    }

}