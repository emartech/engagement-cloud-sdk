package com.sap.ec.core.providers

import kotlin.random.Random

internal class RandomProvider: DoubleProvider {
    override fun provide(): Double {
        return Random.nextDouble()
    }
}