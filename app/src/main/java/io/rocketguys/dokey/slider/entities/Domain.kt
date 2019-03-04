package io.rocketguys.dokey.slider.entities

data class Domain(val lowerBound: Float, val upperBound: Float){
    init {
        if (lowerBound > upperBound)
            throw IllegalStateException("Inconsistent domain")
    }

    companion object {
        fun forceCreate(minOrMax1: Float, minOrMax2: Float) =
                if (minOrMax1 < minOrMax2)
                    Domain(minOrMax1, minOrMax2)
                else
                    Domain(minOrMax2, minOrMax1)
    }

    val size = Math.abs(upperBound - lowerBound)

    fun contains(value: Float) : Boolean = value in lowerBound..upperBound
}