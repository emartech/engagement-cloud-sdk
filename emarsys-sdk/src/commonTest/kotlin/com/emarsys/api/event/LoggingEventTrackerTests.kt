package com.emarsys.api.event

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test


class LoggingEventTrackerTests: TestsWithMocks() {
    companion object {
        val event = CustomEvent("testEvent", mapOf("testAttribute" to "testValue"))
    }

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockLogger: Logger

    private lateinit var loggingInstance: LoggingEventTracker

    @BeforeTest
    fun setup() = runTest {
        everySuspending { mockLogger.debug(isAny()) } returns Unit

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
        verifyWithSuspend { mockLogger.debug(isAny(capture = logEntryCapture)) }
        logEntryCapture.first().topic shouldBe "log_method_not_allowed"
    }
}