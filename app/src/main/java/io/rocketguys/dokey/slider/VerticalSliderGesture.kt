package io.rocketguys.dokey.slider

import android.view.MotionEvent


class VerticalSliderGesture(controller: SliderController): SliderGesture(controller) {

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        change(distanceY)
        return true
    }
}