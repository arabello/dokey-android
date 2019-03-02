package io.rocketguys.dokey.slider

import android.view.MotionEvent


class HorizontalSliderGesture(initValue: Float, domain: IntRange, override val gestureDomainSize: Int, onValueChange: (old: Float, new: Float) -> Unit):
        SliderGesture(initValue, domain, onValueChange) {

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        change(distanceX)
        return true
    }
}