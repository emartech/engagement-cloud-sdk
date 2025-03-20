package com.emarsys.api.push

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import com.emarsys.core.storage.StringStorageApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoggingPushTests {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
    }

    private lateinit var mockLogger: Logger
    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var loggingPush: LoggingPush
    private var slot = Capture.slot<LogEntry>()

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    @BeforeTest
    fun setup() = runTest {
        mockLogger = mock()
        Dispatchers.setMain(mainDispatcher)

        everySuspend { mockLogger.debug(logEntry = capture(slot)) } returns Unit

        mockStringStorage = mock()
        everySuspend { mockStringStorage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit

        loggingPush = LoggingPush(mockLogger, mockStringStorage)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSetPushToken() = runTest {
        loggingPush.registerPushToken(PUSH_TOKEN)

        verify { mockStringStorage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) }
        verifyLogging()
    }

    @Test
    fun testClearPushToken() = runTest {
        loggingPush.clearPushToken()

        verifyLogging()
    }

    @Test
    fun testPushToken() = runTest {
        val result = loggingPush.getPushToken()

        advanceUntilIdle()

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