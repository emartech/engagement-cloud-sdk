package com.emarsys.api.push

import com.emarsys.api.AppEvent
import com.emarsys.core.log.Logger
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test


class LoggingPushTests {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
    }

    private lateinit var mockLogger: Logger
    private lateinit var loggingPush: LoggingPush
    private val notificationEvents: MutableSharedFlow<AppEvent> = MutableSharedFlow()

    @BeforeTest
    fun setup() = runTest {
        mockLogger = mock()
        every { mockLogger.log(any(), any()) } returns Unit

        loggingPush = LoggingPush(mockLogger, notificationEvents)
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
//        TODO: figure out argument capturing
//        val slot = Capture.slot<LogEntry>()
//        verify { mockLogger.log(capture(slot), eq(LogLevel.Debug)) }
//
//        val capturedLogEntry = slot.get()
//        capturedLogEntry.topic shouldBe "log_method_not_allowed"
    }

}