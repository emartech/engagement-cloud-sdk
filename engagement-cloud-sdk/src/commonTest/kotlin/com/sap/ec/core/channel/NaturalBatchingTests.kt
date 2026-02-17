package com.sap.ec.core.channel

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class NaturalBatchingTests {

    @Test
    fun testNaturalBatching_maxChunkSize_must_be_greater_than_0() {
        val flow = flowOf(1)
        shouldThrow<IllegalArgumentException> {
            flow.naturalBatching(0)
        }
    }

    @Test
    fun testNaturalBatching_should_create_equal_sized_chunks() = runTest {
        val flow = flowOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        var counter = 0
        flow.naturalBatching(2).onEach {
            it.size shouldBe 2
            counter++
        }.collect()
        counter shouldBe 5
    }

    @Test
    fun testNaturalBatching_should_not_wait_for_a_chunk_to_be_max_sized() = runTest {
        val flow = flowOf(1)
        var counter = 0

        flow.naturalBatching(2).onEach {
            it.size shouldBe 1
            counter++
        }.collect()
        counter shouldBe 1
    }

    @Test
    fun testNaturalBatching_should_do_nothing_when_flow_is_empty() = runTest {
        val flow = flow<Int> {}
        var counter = 0
        flow.naturalBatching(2).onEach {
            it.size shouldBe 0
            counter++
        }
        counter shouldBe 0
    }
}