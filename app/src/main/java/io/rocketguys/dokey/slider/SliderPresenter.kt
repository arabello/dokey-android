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
        val value = outputData.value / (outputData.maxValue - outputData.minValue)
        view.onDataChange(SliderViewModel(value, 0f, 1f))
    }
}