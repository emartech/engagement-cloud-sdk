package com.emarsys.api.event

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test


class LoggingEventTrackerTests {
    companion object {
        val event = CustomEvent("testEvent", mapOf("testAttribute" to "testValue"))
    }
    private lateinit var mockLogger: Logger
    private lateinit var loggingInstance: LoggingEventTracker

    @BeforeTest
    fun setup() = runTest {
        mockLogger = mock()
        everySuspend { mockLogger.debug(any()) } returns Unit

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

    private suspend fun verifyLogging() {
        val logEntryCapture = mutableListOf<LogEntry>()
//        verifySuspend { mockLogger.debug(logEntryCapture) }  TODO: fix this
//        logEntryCapture.first().topic shouldBe "log_method_not_allowed"
    }
}