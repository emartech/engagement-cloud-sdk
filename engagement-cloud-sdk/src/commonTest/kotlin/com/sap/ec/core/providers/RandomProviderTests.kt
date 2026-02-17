package com.sap.ec.core.providers

import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RandomProviderTests {

    private lateinit var provider: RandomProvider

    @BeforeTest
    fun setup() = runTest {
        provider = RandomProvider()
    }

    @Test
    fun testProvide_shouldGenerate_differentValues_everyTime() = runTest {
        val randoms = mutableListOf<Double>()
        for (i in 1..1000) {
            val random = provider.provide()
            random shouldBeGreaterThanOrEqual  0.0
            random shouldBeLessThanOrEqual 1.0
            randoms.add(random)
        }

        randoms.size shouldBe randoms.distinct().size
    }

}