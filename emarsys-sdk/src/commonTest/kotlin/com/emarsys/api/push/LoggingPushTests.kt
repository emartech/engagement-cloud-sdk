package com.emarsys.api.push

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test


class LoggingPushTests {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
    }

    private lateinit var mockLogger: Logger
    private lateinit var loggingPush: LoggingPush
    private var slot = Capture.slot<LogEntry>()

    @BeforeTest
    fun setup() = runTest {
        mockLogger = mock()
        every { mockLogger.log(capture(slot), LogLevel.Debug) } returns Unit

        loggingPush = LoggingPush(mockLogger)
    }

    @Test
    fun testSetPushToken() = runTest {
        loggingPush.registerPushToken(PUSH_TOKEN)

        verifyLogging()
    }

    @Test
    fun testClearPushToken() = runTest {
        loggingPush.clearPushToken()

        verifyLogging()
    }

    @Test
    fun testPushToken() = runTest {
        val result = loggingPush.pushToken

        verifyLogging()

        result shouldBe null
    }

    @Test
    fun testActive() = runTest {
        loggingPush.activate()

        verifyLogging()
    }

    private fun verifyLogging() {
        val capturedLogEntry = slot.get()
        capturedLogEntry.topic shouldBe "log_method_not_allowed"
    }

}