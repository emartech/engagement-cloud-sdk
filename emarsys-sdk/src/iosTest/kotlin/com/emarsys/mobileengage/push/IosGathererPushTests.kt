package com.emarsys.mobileengage.push

import com.emarsys.api.push.BasicPushUserInfo
import com.emarsys.api.push.BasicPushUserInfoEms
import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushContext
import com.emarsys.core.storage.StringStorageApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject
import kotlin.test.BeforeTest
import kotlin.test.Test


class IosGathererPushTests {
    private lateinit var mockStorage: StringStorageApi
    private lateinit var pushContext: PushContext
    private lateinit var mockIosPushInternal: IosPushInstance
    private lateinit var iosGathererPush: IosGathererPush
    private lateinit var testNotificationCenterDelegateProtocol: UNUserNotificationCenterDelegateProtocol

    @BeforeTest
    fun setup() {
        mockStorage = mock()
        mockIosPushInternal = mock()
        pushContext = PushContext(mutableListOf())
        testNotificationCenterDelegateProtocol =
            object : NSObject(), UNUserNotificationCenterDelegateProtocol {}
        iosGathererPush = IosGathererPush(pushContext, mockStorage, mockIosPushInternal)
    }

    @Test
    fun testCustomerUserNotificationCenterDelegate_getter_shouldInvokeOnInternalInstance() {
        every { mockIosPushInternal.customerUserNotificationCenterDelegate } returns testNotificationCenterDelegateProtocol
        val result = iosGathererPush.customerUserNotificationCenterDelegate

        verify { mockIosPushInternal.customerUserNotificationCenterDelegate }
        result shouldBe testNotificationCenterDelegateProtocol
    }

    @Test
    fun testCustomerUserNotificationCenterDelegate_setter_shouldInvokeOnInternalInstance() {
        every { mockIosPushInternal.customerUserNotificationCenterDelegate } returns null
        every { mockIosPushInternal.customerUserNotificationCenterDelegate = any() } returns Unit
        iosGathererPush.customerUserNotificationCenterDelegate =
            testNotificationCenterDelegateProtocol

        verify {
            mockIosPushInternal.customerUserNotificationCenterDelegate =
                testNotificationCenterDelegateProtocol
        }
    }

    @Test
    fun testEmarsysUserNotificationCenterDelegate_shouldInvokeOnInternalInstance() {
        every { mockIosPushInternal.emarsysUserNotificationCenterDelegate } returns testNotificationCenterDelegateProtocol
        val result = iosGathererPush.emarsysUserNotificationCenterDelegate

        verify { mockIosPushInternal.emarsysUserNotificationCenterDelegate }
        result shouldBe testNotificationCenterDelegateProtocol
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_shouldStoreCallInContext() = runTest {
        val userInfo = BasicPushUserInfo(BasicPushUserInfoEms("testId"))

        iosGathererPush.handleSilentMessageWithUserInfo(userInfo)

        pushContext.calls.contains(PushCall.HandleMessageWithUserInfo(userInfo)) shouldBe true
        pushContext.calls.size shouldBe 1
    }
}