package com.emarsys.api.push

import com.emarsys.api.AppEvent
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test


class FakeSdkLogger : Logger {

    var funCalls: MutableList<Pair<LogEntry, LogLevel>> = mutableListOf()
    override fun log(entry: LogEntry, level: LogLevel) {
        funCalls.add(Pair(entry, level))
    }

}

class LoggingPushTests {

    private companion object {
        const val PUSH_TOKEN = "testPushToken"
    }

    private lateinit var fakeLogger: FakeSdkLogger
    private lateinit var loggingPush: LoggingPush
    private val notificationEvents: MutableSharedFlow<AppEvent> = MutableSharedFlow()

    @BeforeTest
    fun setup() = runTest {
        fakeLogger = FakeSdkLogger()
        loggingPush = LoggingPush(fakeLogger, notificationEvents)
    }

    @Test
    fun testSetPushToken() = runTest {
        loggingPush.registerPushToken(PUSH_TOKEN)

        fakeLogger.funCalls.first().first.topic shouldBe "log_method_not_allowed"
        fakeLogger.funCalls.first().second shouldBe LogLevel.debug
    }

    @Test
    fun testClearPushToken() = runTest {
        loggingPush.clearPushToken()

        fakeLogger.funCalls.first().first.topic shouldBe "log_method_not_allowed"
        fakeLogger.funCalls.first().second shouldBe LogLevel.debug
    }

    @Test
    fun testPushToken() = runTest {
        val result = loggingPush.pushToken

        fakeLogger.funCalls.first().first.topic shouldBe "log_method_not_allowed"
        fakeLogger.funCalls.first().second shouldBe LogLevel.debug

        result shouldBe null
    }

    @Test
    fun testActive() = runTest {
        loggingPush.activate()

        fakeLogger.funCalls.first().first.topic shouldBe "log_method_not_allowed"
        fakeLogger.funCalls.first().second shouldBe LogLevel.debug
    }

}