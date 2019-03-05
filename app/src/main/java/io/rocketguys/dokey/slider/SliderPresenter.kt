package io.rocketguys.dokey.slider

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */

interface SliderView{
    fun onDataChange(viewModel: SliderViewModel)
}

data class SliderViewModel(val value: Float, val minValue: Float, val maxValue: Float)

class SliderPresenter(val view: SliderView) : SliderOutputBoundary {
    override fun onValueChange(outputData: Data) {
        val projMinValue = if (outputData.minValue < 0) 0f else outputData.minValue
        val projMaxValue = if (outputData.minValue < 0) outputData.maxValue + (-outputData.minValue) else outputData.maxValue
        val projValue = if (outputData.minValue < 0) outputData.value + (-outputData.minValue) else outputData.value
        val value = projValue / (projMaxValue - projMinValue)
        view.onDataChange(SliderViewModel(value, 0f, 1f))
    }
}