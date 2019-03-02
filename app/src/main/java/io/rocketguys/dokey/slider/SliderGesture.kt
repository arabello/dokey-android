package io.rocketguys.dokey.slider

import android.view.GestureDetector
import android.view.MotionEvent


object doNothing: SliderGesture.OnValueChange{
    override fun onChange(oldValue: Float, newValue: Float) {}
}

abstract class SliderGesture(initValue: Float, val domain: IntRange, val onValueChange: OnValueChange = doNothing) : GestureDetector.SimpleOnGestureListener(){

    constructor(initValue: Float, domain: IntRange, onValueChange: (old: Float, new: Float) -> Unit) : this(initValue, domain, object : OnValueChange{
        override fun onChange(oldValue: Float, newValue: Float) = onValueChange(oldValue, newValue)
    })

    interface OnValueChange{
        fun onChange(oldValue: Float, newValue: Float)
    }

    private var currentValue = initValue
    protected abstract val gestureDomainSize: Int

    protected fun change(deltaGesture: Float){
        val delta = ( (deltaGesture * domain.count()) / gestureDomainSize) * sensibility
        val new = currentValue + delta
        val newValue: Float = if (new < domain.start) domain.start.toFloat() else if (new > domain.endInclusive) domain.endInclusive.toFloat() else new

        onValueChange.onChange(currentValue, newValue)
        currentValue = newValue
    }


    abstract override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean

    var sensibility = 1.0f
}