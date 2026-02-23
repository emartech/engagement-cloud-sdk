package com.sap.ec.api.embeddedmessaging

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
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
class LoggingEmbeddedMessagingTests {

    private lateinit var mockLogger: Logger
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var loggingInstance: LoggingEmbeddedMessaging
    private var slot = Capture.slot<LogEntry>()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockSdkContext = mock()
        mockLogger = mock()
        every { mockSdkContext.sdkDispatcher } returns StandardTestDispatcher()
        everySuspend { mockLogger.debug(capture(slot)) } returns Unit
        loggingInstance = LoggingEmbeddedMessaging(mockSdkContext, mockLogger)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun categories_shouldReturn_emptyList_andLog_logMethodNotAllowed() = runTest {
        loggingInstance.categories shouldBe emptyList()

        advanceUntilIdle()

        val capturedLogEntry = slot.get()
        capturedLogEntry.topic shouldBe "log_method_not_allowed"
    }

    @Test
    fun isUnreadFilterActive_shouldReturn_false_andLog_logMethodNotAllowed() = runTest {
        loggingInstance.isUnreadFilterActive shouldBe false

        advanceUntilIdle()

        val capturedLogEntry = slot.get()
        capturedLogEntry.topic shouldBe "log_method_not_allowed"
    }

    @Test
    fun activeCategoryFilters_shouldReturn_emptySet_andLog_logMethodNotAllowed() = runTest {
        loggingInstance.activeCategoryFilters shouldBe emptySet()

        advanceUntilIdle()

        val capturedLogEntry = slot.get()
        capturedLogEntry.topic shouldBe "log_method_not_allowed"
    }

    @Test
    fun filterUnreadOnly_shouldLog_logMethodNotAllowed() = runTest {
        loggingInstance.filterUnreadOnly(true)

        advanceUntilIdle()

        val capturedLogEntry = slot.get()
        capturedLogEntry.topic shouldBe "log_method_not_allowed"
    }

    @Test
    fun filterByCategories_shouldLog_logMethodNotAllowed() = runTest {
        loggingInstance.filterByCategories(emptySet())

        advanceUntilIdle()

        val capturedLogEntry = slot.get()
        capturedLogEntry.topic shouldBe "log_method_not_allowed"
    }

    @Test
    fun activate_shouldLog_debugInfo() = runTest {
        loggingInstance.activate()

        advanceUntilIdle()

        val capturedLogEntry = slot.get()
        capturedLogEntry.topic shouldBe "log_method_not_allowed"
    }
}