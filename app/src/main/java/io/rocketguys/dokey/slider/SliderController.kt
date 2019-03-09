package io.rocketguys.dokey.slider

import model.command.AnalogCommand

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class SliderController(private val inputBoundary: SliderInputBoundary, analogCommand: AnalogCommand, val minValue: Float, val maxValue: Float) {
    init {
        inputBoundary.requestSlider(analogCommand)
    }

    fun update(newValue: Float) = inputBoundary.projectFrom(newValue, minValue, maxValue)
}