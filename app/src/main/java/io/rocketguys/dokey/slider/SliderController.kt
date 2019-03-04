package io.rocketguys.dokey.slider

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class SliderController(private val inputBoundary: SliderInputBoundary, sliderId: Int, val minValue: Float, val maxValue: Float) {
    init {
        inputBoundary.requestSlider(sliderId)
    }

    fun update(newValue: Float) = inputBoundary.projectFrom(newValue, minValue, maxValue)
}