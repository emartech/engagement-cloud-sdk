package com.sap.ec.mobileengage.push

import com.sap.ec.api.push.Ems
import com.sap.ec.api.push.SilentNotification
import com.sap.ec.api.push.SilentPushUserInfo
import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StringStorageApi
import dev.mokkery.answering.returns
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
class IosLoggingPushTests {
    private lateinit var mockLogger: Logger
    private lateinit var iosLoggingPush: IosLoggingPush
    private lateinit var mockStorage: StringStorageApi
    private var slot = Capture.slot<LogEntry>()
    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    @BeforeTest
    fun setup() = runTest {
        mockLogger = mock()
        everySuspend { mockLogger.debug(logEntry = capture(slot)) } returns Unit

        mockStorage = mock()

        iosLoggingPush = IosLoggingPush(mockLogger, mockStorage, mainDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testCustomerUserNotificationCenterDelegate() = runTest {
        val result = iosLoggingPush.customerUserNotificationCenterDelegate

        advanceUntilIdle()

        verifyLogging()

        result shouldBe emptyList()
    }

    @Test
    fun testUserNotificationCenterDelegate() = runTest {
        iosLoggingPush.userNotificationCenterDelegate

        advanceUntilIdle()

        verifyLogging()
    }

    @Test
    fun testHandleSilentMessageWithUserInfo() = runTest {
        val userInfo = SilentPushUserInfo(
            Ems(version = "testVersion", trackingInfo = "testTrackingInfo"),
            SilentNotification(
                silent = true,
                defaultAction = null,
                actions = null,
                badgeCount = null
            )
        )
        iosLoggingPush.handleSilentMessageWithUserInfo(userInfo)

        verifyLogging()
    }

    @Test
    fun testActive() = runTest {
        iosLoggingPush.activate()

        verifyLogging()
    }

    private fun verifyLogging() {
        val capturedLogEntry = slot.get()
        capturedLogEntry.topic shouldBe "log_method_not_allowed"
    }

}