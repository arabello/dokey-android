package io.rocketguys.dokey.slider

import io.rocketguys.dokey.network.NetworkManagerService
import io.rocketguys.dokey.slider.entities.Domain
import io.rocketguys.dokey.slider.entities.SliderEntity

interface SliderOutputBoundary{
    fun onValueChange(outputData: Data)
}

data class Data(val value: Float, val minValue: Float, val maxValue: Float)

interface SliderInputBoundary{
    fun requestSlider(id: Int)
    fun projectFrom(value: Float, minValue: Float, maxValue: Float)
}

class SliderUseCase(val networkManagerService: NetworkManagerService, val output: SliderOutputBoundary) : SliderInputBoundary{
    private lateinit var slider: SliderEntity

    override fun requestSlider(id: Int) {
        slider = SliderEntity(0f, Domain(-50f, 50f))
        //TODO("Network call to init slider data")
        output.onValueChange(Data(slider.value, slider.domain.lowerBound, slider.domain.upperBound))
    }

    override fun projectFrom(value: Float, minValue: Float, maxValue: Float) {
        val newValue = (value * slider.domain.size) / (maxValue - minValue)
        slider = SliderEntity.forceCreate(newValue + slider.value, slider.domain)
        val data = Data(slider.value, slider.domain.lowerBound, slider.domain.upperBound)
        output.onValueChange(data)
        //TODO("Network call to send changes")
    }
}