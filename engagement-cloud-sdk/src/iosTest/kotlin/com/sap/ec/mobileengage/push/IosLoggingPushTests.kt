package com.sap.ec.mobileengage.push

import com.sap.ec.api.push.Ems
import com.sap.ec.api.push.NotificationCenterDelegateRegistration
import com.sap.ec.api.push.NotificationCenterDelegateRegistrationOptions
import com.sap.ec.api.push.SilentNotification
import com.sap.ec.api.push.SilentPushUserInfo
import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StringStorageApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject
import kotlin.test.BeforeTest
import kotlin.test.Test

class IosLoggingPushTests {
    private lateinit var mockLogger: Logger
    private lateinit var iosLoggingPush: IosLoggingPush
    private lateinit var mockStorage: StringStorageApi
    private lateinit var mockIosPushInternal: IosPushInstance
    private var logEntrySlot = Capture.slot<LogEntry>()
    private var messageSlot = Capture.slot<String>()
    private lateinit var testDelegate: UNUserNotificationCenterDelegateProtocol

    @BeforeTest
    fun setup() = runTest {
        mockLogger = mock(MockMode.autofill)
        everySuspend { mockLogger.debug(logEntry = capture(logEntrySlot)) } returns Unit
        everySuspend { mockLogger.debug(message = capture(messageSlot)) } returns Unit

        mockStorage = mock()
        mockIosPushInternal = mock()
        testDelegate = object : NSObject(), UNUserNotificationCenterDelegateProtocol {}

        iosLoggingPush = IosLoggingPush(mockLogger,
            StandardTestDispatcher(), mockStorage, mockIosPushInternal)
    }

    @Test
    fun testUserNotificationCenterDelegate_shouldDelegateToInternal() {
        every { mockIosPushInternal.userNotificationCenterDelegate } returns testDelegate

        val result = iosLoggingPush.userNotificationCenterDelegate

        verify { mockIosPushInternal.userNotificationCenterDelegate }
        result shouldBe testDelegate
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_shouldLog() = runTest {
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

        verifyMethodNotAllowedLogging()
    }

    @Test
    fun testActive_shouldCallParentActivate() = runTest {
        iosLoggingPush.activate()

        verifySuspend { mockLogger.debug(message = any()) }
    }

    @Test
    fun testRegisteredNotificationDelegates_shouldDelegateToInternal() {
        val expectedRegistrations = listOf(
            NotificationCenterDelegateRegistration(testDelegate)
        )
        every { mockIosPushInternal.registeredNotificationCenterDelegates } returns expectedRegistrations

        val result = iosLoggingPush.registeredNotificationCenterDelegates

        verify { mockIosPushInternal.registeredNotificationCenterDelegates }
        result shouldBe expectedRegistrations
    }

    @Test
    fun testRegisterNotificationDelegate_shouldDelegateToInternal() {
        val options = NotificationCenterDelegateRegistrationOptions(includeEngagementCloudMessages = true)
        every { mockIosPushInternal.registerNotificationCenterDelegate(any(), any()) } returns Unit

        iosLoggingPush.registerNotificationCenterDelegate(testDelegate, options)

        verify { mockIosPushInternal.registerNotificationCenterDelegate(testDelegate, options) }
    }

    @Test
    fun testUnregisterNotificationCenterDelegate_shouldDelegateToInternal() {
        every { mockIosPushInternal.unregisterNotificationCenterDelegate(any()) } returns Unit

        iosLoggingPush.unregisterNotificationCenterDelegate(testDelegate)

        verify { mockIosPushInternal.unregisterNotificationCenterDelegate(testDelegate) }
    }

    private fun verifyMethodNotAllowedLogging() {
        val capturedLogEntry = logEntrySlot.get()
        capturedLogEntry.topic shouldBe "log_method_not_allowed"
    }
}
