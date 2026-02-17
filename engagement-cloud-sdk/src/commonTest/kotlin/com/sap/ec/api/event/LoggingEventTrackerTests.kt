package com.sap.ec.api.event

import com.sap.ec.api.event.model.CustomEvent
import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test


class LoggingEventTrackerTests {
    companion object {
        val event = CustomEvent("testEvent", mapOf("testAttribute" to "testValue"))
    }
    private lateinit var mockLogger: Logger
    private lateinit var loggingInstance: LoggingEventTracker
    private var slot = Capture.slot<LogEntry>()

    @BeforeTest
    fun setup() = runTest {
        mockLogger = mock()
        everySuspend { mockLogger.debug(capture(slot)) } returns Unit

        loggingInstance = LoggingEventTracker(mockLogger)
    }

    @Test
    fun testTrackEvent() = runTest {
        loggingInstance.trackEvent(event)

        verifyLogging()
    }

    @Test
    fun testActive() = runTest {
        loggingInstance.activate()

        verifyLogging()
    }

    private fun verifyLogging() {
        val capturedLogEntry = slot.get()
        capturedLogEntry.topic shouldBe "log_method_not_allowed"
    }
}