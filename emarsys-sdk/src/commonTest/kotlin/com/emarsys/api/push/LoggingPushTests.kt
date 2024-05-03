package com.emarsys.api.push

import com.emarsys.api.AppEvent
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.BeforeTest
import kotlin.test.Test


class LoggingPushTests: TestsWithMocks() {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
    }

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockLogger: Logger

    private lateinit var loggingPush: LoggingPush
    private val notificationEvents: MutableSharedFlow<AppEvent> = MutableSharedFlow()

    @BeforeTest
    fun setup() = runTest {
        every { mockLogger.log(isAny(), isAny()) } returns Unit

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
        val logEntryCapture = mutableListOf<LogEntry>()
        verify { mockLogger.log(isAny(capture = logEntryCapture), isEqual(LogLevel.Debug)) }
        logEntryCapture.first().topic shouldBe "log_method_not_allowed"
    }

}