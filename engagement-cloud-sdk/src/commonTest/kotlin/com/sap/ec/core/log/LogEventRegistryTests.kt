package com.sap.ec.core.log

import com.sap.ec.event.SdkEvent
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LogEventRegistryTests {
    private lateinit var registry: LogEventRegistryApi

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        registry = LogEventRegistry()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun registerLogEvent_shouldRegisterEvent() = runTest {
        val logAttributes = buildJsonObject {
            put("message", "Test log message")
        }
        val logEvent = SdkEvent.Internal.Sdk.Log(
            level = LogLevel.Info,
            attributes = logAttributes
        )

        val emittedEvent = backgroundScope.async {
            registry.logEvents.take(1).toList()
        }

        advanceUntilIdle()

        registry.registerLogEvent(logEvent)

        advanceUntilIdle()

        emittedEvent.await() shouldBe listOf(logEvent)
    }
}