package com.sap.ec.mobileengage.push

import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.api.push.Ems
import com.sap.ec.api.push.PushCall
import com.sap.ec.api.push.PushContext
import com.sap.ec.api.push.PushContextApi
import com.sap.ec.api.push.SilentNotification
import com.sap.ec.api.push.SilentPushUserInfo
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.storage.StringStorageApi
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
    private companion object {
        const val TEST_APPLICATION_CODE = "testAppCode"
    }

    private lateinit var mockStorage: StringStorageApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockIosPushInternal: IosPushInstance
    private lateinit var iosGathererPush: IosGathererPush
    private lateinit var pushContext: PushContextApi
    private lateinit var testNotificationCenterDelegateProtocol: UNUserNotificationCenterDelegateProtocol

    @BeforeTest
    fun setup() {
        mockStorage = mock()
        mockIosPushInternal = mock()
        mockSdkContext = mock()
        every { mockSdkContext.config } returns TestEngagementCloudSDKConfig(TEST_APPLICATION_CODE)
        testNotificationCenterDelegateProtocol =
            object : NSObject(), UNUserNotificationCenterDelegateProtocol {}
        pushContext = PushContext(mutableListOf())
        iosGathererPush =
            IosGathererPush(pushContext, mockStorage, mockIosPushInternal, mockSdkContext)
    }

    @Test
    fun testCustomerUserNotificationCenterDelegate_getter_shouldInvokeOnInternalInstance() {
        every { mockIosPushInternal.customerUserNotificationCenterDelegate } returns listOf(
            testNotificationCenterDelegateProtocol
        )
        val result = iosGathererPush.customerUserNotificationCenterDelegate

        verify { mockIosPushInternal.customerUserNotificationCenterDelegate }
        result shouldBe listOf(testNotificationCenterDelegateProtocol)
    }

    @Test
    fun testCustomerUserNotificationCenterDelegate_setter_shouldInvokeOnInternalInstance() {
        every { mockIosPushInternal.customerUserNotificationCenterDelegate } returns listOf()
        every { mockIosPushInternal.customerUserNotificationCenterDelegate = any() } returns Unit
        iosGathererPush.customerUserNotificationCenterDelegate =
            listOf(testNotificationCenterDelegateProtocol)

        verify {
            mockIosPushInternal.customerUserNotificationCenterDelegate =
                listOf(testNotificationCenterDelegateProtocol)
        }
    }

    @Test
    fun testEmarsysUserNotificationCenterDelegate_shouldInvokeOnInternalInstance() {
        every { mockIosPushInternal.userNotificationCenterDelegate } returns testNotificationCenterDelegateProtocol
        val result = iosGathererPush.userNotificationCenterDelegate

        verify { mockIosPushInternal.userNotificationCenterDelegate }
        result shouldBe testNotificationCenterDelegateProtocol
    }

    @Test
    fun testHandleSilentMessageWithUserInfo_shouldStoreCallInContext() = runTest {
        val userInfo = SilentPushUserInfo(
            Ems(version = "testVersion", trackingInfo = "testTrackingInfo"),
            SilentNotification(
                silent = true,
                defaultAction = null,
                actions = null,
                badgeCount = null
            )
        )

        iosGathererPush.handleSilentMessageWithUserInfo(userInfo)

        pushContext.calls.contains(PushCall.HandleSilentMessageWithUserInfo(userInfo)) shouldBe true
        pushContext.calls.size shouldBe 1
    }
}