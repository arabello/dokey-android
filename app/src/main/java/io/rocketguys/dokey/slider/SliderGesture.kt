package io.rocketguys.dokey.slider

import android.view.GestureDetector
import android.view.MotionEvent

abstract class SliderGesture(private val controller: SliderController) : GestureDetector.SimpleOnGestureListener(){

    protected fun change(deltaGesture: Float){
        controller.update(deltaGesture)
    }

    abstract override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean
}