package io.rocketguys.dokey.slider

data class SliderEntity(val value: Float, val domain: Domain){
    init {
        if(!domain.contains(value))
            throw IllegalStateException("Slider inconsistent state: value $value outside boundaries")
    }

    companion object {
        fun forceCreate(value: Float, domain: Domain) : SliderEntity =
            when {
                domain.contains(value) -> SliderEntity(value, domain)
                value < domain.lowerBound -> SliderEntity(domain.lowerBound, domain)
                else -> SliderEntity(domain.upperBound, domain)
            }


    }
}