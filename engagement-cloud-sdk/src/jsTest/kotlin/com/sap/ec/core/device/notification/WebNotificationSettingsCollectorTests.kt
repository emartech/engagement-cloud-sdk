package com.sap.ec.core.device.notification

import com.sap.ec.mobileengage.push.PushServiceContextApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class WebNotificationSettingsCollectorTests {

    private lateinit var mockPushServiceContext: PushServiceContextApi
    private lateinit var webNotificationSettingsCollector: WebNotificationSettingsCollectorApi

    @BeforeTest
    fun setup() {
        mockPushServiceContext = mock()
        webNotificationSettingsCollector = WebNotificationSettingsCollector(mockPushServiceContext)
    }

    @Test
    fun collect_shouldReturn_notificationSettings_withCorrectValues_granted() = runTest {
        val expectedSettings = WebNotificationSettings(
            PermissionState.Granted,
            isServiceWorkerRegistered = true,
            isSubscribed = false
        )

        everySuspend { mockPushServiceContext.getPermissionState() } returns PermissionState.Granted
        everySuspend { mockPushServiceContext.isServiceWorkerRegistered } returns true
        everySuspend { mockPushServiceContext.isSubscribed } returns false

        val result = webNotificationSettingsCollector.collect()

        result shouldBe expectedSettings
    }

    @Test
    fun collect_shouldReturn_notificationSettings_withCorrectValues_denied() = runTest {
        val expectedSettings = WebNotificationSettings(
            PermissionState.Denied,
            isServiceWorkerRegistered = true,
            isSubscribed = false
        )

        everySuspend { mockPushServiceContext.getPermissionState() } returns PermissionState.Denied
        everySuspend { mockPushServiceContext.isServiceWorkerRegistered } returns true
        everySuspend { mockPushServiceContext.isSubscribed } returns false

        val result = webNotificationSettingsCollector.collect()

        result shouldBe expectedSettings
    }
}