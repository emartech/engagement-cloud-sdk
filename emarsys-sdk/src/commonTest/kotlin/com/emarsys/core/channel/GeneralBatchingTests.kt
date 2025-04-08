package com.emarsys.core.channel

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GeneralBatchingTests {

    @Test
    fun testBatching_should_handle_spaced_Events_and_flushesCorrectly() = runTest {
        val eventsForFirstBatch = (1..11).toList()
        val eventsForSecondBatch = (1..2).toList()
        val results = mutableListOf<List<Int>>()

        val emits = flow {
            eventsForFirstBatch.forEach {
                emit(it)
            }
            eventsForSecondBatch.forEach {
                delay(5000)
                emit(it)
            }
        }
        val job = launch {
            emits.batched(
                batchSize = 10,
                batchIntervalMillis = 10000L
            )
                .collect { batch ->
                    results.add(batch)
                }
        }

        testScheduler.advanceTimeBy(15000)

        results.size shouldBe 2
        results[0].size shouldBe 10
        results[1].size shouldBe 2
        job.cancel()
    }

    @Test
    fun testBatchingMechanism_should_batch_event_based_on_time() = runTest {
        val events = listOf(1)
        val results = mutableListOf<List<Int>>()
        val job = launch {
            flowOf(*events.toTypedArray())
                .batched(
                    batchSize = 10,
                    batchIntervalMillis = 10000L
                )
                .collect { batch ->
                    results.add(batch)
                }
        }

        testScheduler.advanceTimeBy(20000)

        results.size shouldBe 1
        results[0].size shouldBe 1
        job.cancel()
    }

    @Test
    fun testBatchingMechanism_should_batch_events_based_on_count() = runTest {
        val events = (1..10).toList()
        val results = mutableListOf<List<Int>>()

        val job = launch {
            flowOf(*events.toTypedArray())
                .batched(
                    batchSize = 5,
                    batchIntervalMillis = 10000L
                )
                .collect { batch ->
                    results.add(batch)
                }
        }
        testScheduler.advanceUntilIdle()

        results.size shouldBe 2
        results[0].size shouldBe 5
        results[1].size shouldBe 5
        job.cancel()
    }
}