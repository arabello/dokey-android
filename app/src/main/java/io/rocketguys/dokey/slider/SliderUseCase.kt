package io.rocketguys.dokey.slider

import io.rocketguys.dokey.network.NetworkManagerService
import io.rocketguys.dokey.slider.entities.Domain
import io.rocketguys.dokey.slider.entities.SliderEntity
import model.command.AnalogCommand

interface SliderOutputBoundary{
    fun onValueChange(outputData: Data)
}

data class Data(val value: Float, val minValue: Float, val maxValue: Float)

interface SliderInputBoundary{
    fun requestSlider(command: AnalogCommand)
    fun projectFrom(value: Float, minValue: Float, maxValue: Float)
}

class SliderUseCase(val networkManagerService: NetworkManagerService, val output: SliderOutputBoundary) : SliderInputBoundary{
    private lateinit var slider: SliderEntity
    private lateinit var analogCommand: AnalogCommand
    private val interceptor = TimedInterceptor(120)

    override fun requestSlider(command: AnalogCommand) {
        command.min?.let { min ->
            command.max?.let { max ->
                analogCommand = command
                slider = SliderEntity(100f, Domain( min, max)) // TODO (When ready use APi to initialize the correct value)
                output.onValueChange(Data(slider.value, slider.domain.lowerBound, slider.domain.upperBound))
            }
        }
    }

    override fun projectFrom(value: Float, minValue: Float, maxValue: Float) {
        val newValue = (value * slider.domain.size) / (maxValue - minValue)
        slider = SliderEntity.forceCreate(newValue + slider.value, slider.domain)
        val data = Data(slider.value, slider.domain.lowerBound, slider.domain.upperBound)
        output.onValueChange(data)
        interceptor.perform {
            networkManagerService.executeSliderUpdate(analogCommand, data.value)
        }
    }
}

class TimedInterceptor(val msInterval: Long){
    private var msLatAction: Long = System.currentTimeMillis()

    fun perform(block: () -> Unit){
        val now = System.currentTimeMillis()
        if (now - msLatAction >= msInterval){
            msLatAction = now
            block()
        }
    }
}