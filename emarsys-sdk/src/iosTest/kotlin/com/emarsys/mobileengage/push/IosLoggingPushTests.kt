package com.emarsys.mobileengage.push

import com.emarsys.api.push.BasicPushUserInfo
import com.emarsys.api.push.BasicPushUserInfoEms
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

class IosLoggingPushTests {
    private lateinit var mockLogger: Logger
    private lateinit var iosLoggingPush: IosLoggingPush
    private var slot = Capture.slot<LogEntry>()

    @BeforeTest
    fun setup() = runTest {
        mockLogger = mock()
        every { mockLogger.log(capture(slot), LogLevel.Debug) } returns Unit

        iosLoggingPush = IosLoggingPush(mockLogger)
    }

    @Test
    fun testCustomerUserNotificationCenterDelegate() = runTest {
        val result = iosLoggingPush.customerUserNotificationCenterDelegate

        verifyLogging()

        result shouldBe null
    }

    @Test
    fun testEmarsysUserNotificationCenterDelegate() = runTest {
        iosLoggingPush.emarsysUserNotificationCenterDelegate

        verifyLogging()
    }

    @Test
    fun testHandleSilentMessageWithUserInfo() = runTest {
        val userInfo = BasicPushUserInfo(BasicPushUserInfoEms("test", null))
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