package com.emarsys.core.providers

import kotlin.random.Random

class RandomProvider: Provider<Double> {
    override fun provide(): Double {
        return Random.nextDouble()
    }

}