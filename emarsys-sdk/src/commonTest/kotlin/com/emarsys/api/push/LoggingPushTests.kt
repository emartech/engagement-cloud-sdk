package com.emarsys.api.push

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

class LoggingPushTests {

    companion object {
        val pushToken = "testPushToken"
    }

    lateinit var fakeLogger: FakeSdkLogger
    lateinit var loggingPush: LoggingPush

    @BeforeTest
    fun setup() = runTest {
        fakeLogger = FakeSdkLogger()
        loggingPush = LoggingPush(fakeLogger)
    }

    @Test
    fun testSetPushToken() = runTest {
        loggingPush.registerPushToken(pushToken)

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
    fun testActive() = runTest {
        loggingPush.activate()

        fakeLogger.funCalls.first().first.topic shouldBe "log_method_not_allowed"
        fakeLogger.funCalls.first().second shouldBe LogLevel.debug
    }

}