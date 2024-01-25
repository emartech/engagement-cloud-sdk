package com.emarsys.api.event

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class FakeSdkLogger : Logger {

    var funCalls: MutableList<Pair<LogEntry, LogLevel>> = mutableListOf()
    override fun log(entry: LogEntry, level: LogLevel) {
        funCalls.add(Pair(entry, level))
    }

}

class LoggingEventTrackerTests {

    companion object {
        val event = CustomEvent("testEvent", mapOf("testAttribute" to "testValue"))
    }

    lateinit var fakeLogger: FakeSdkLogger
    lateinit var loggingInstance: LoggingEventTracker

    @BeforeTest
    fun setup() = runTest {
        fakeLogger = FakeSdkLogger()
        loggingInstance = LoggingEventTracker(fakeLogger)
    }

    @Test
    fun testTrackEvent() = runTest {
        loggingInstance.trackEvent(event)

        fakeLogger.funCalls.first().first.topic shouldBe "log_method_not_allowed"
        fakeLogger.funCalls.first().second shouldBe LogLevel.debug
    }

    @Test
    fun testActive() = runTest {
        loggingInstance.activate()

        fakeLogger.funCalls.first().first.topic shouldBe "log_method_not_allowed"
        fakeLogger.funCalls.first().second shouldBe LogLevel.debug
    }

}