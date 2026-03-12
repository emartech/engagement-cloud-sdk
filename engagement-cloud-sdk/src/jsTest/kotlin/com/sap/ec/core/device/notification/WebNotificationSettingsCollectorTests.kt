package com.sap.ec.core.device.notification

import com.sap.ec.mobileengage.push.JsPushWrapperApi
import com.sap.ec.mobileengage.push.PushServiceApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import web.serviceworker.ServiceWorkerRegistration
import kotlin.test.BeforeTest
import kotlin.test.Test

class WebNotificationSettingsCollectorTests {

    private lateinit var mockPushService: PushServiceApi
    private lateinit var mockJsPushWrapperApi: JsPushWrapperApi
    private lateinit var webNotificationSettingsCollector: WebNotificationSettingsCollectorApi

    @BeforeTest
    fun setup() {
        mockPushService = mock()
        mockJsPushWrapperApi = mock()
        webNotificationSettingsCollector =
            WebNotificationSettingsCollector(mockPushService, mockJsPushWrapperApi)
    }

    @Test
    fun collect_shouldReturn_notificationSettings_withCorrectValues_granted() = runTest {
        val testRegistration: ServiceWorkerRegistration = js("({ active: {} })")
        val expectedSettings = WebNotificationSettings(
            PermissionState.Granted,
            isServiceWorkerRegistered = true,
            isSubscribed = false
        )

        everySuspend { mockPushService.getPermissionState() } returns PermissionState.Granted
        everySuspend { mockPushService.getServiceWorkerRegistration() } returns testRegistration
        everySuspend { mockJsPushWrapperApi.isSubscribed() } returns false

        val result = webNotificationSettingsCollector.collect()

        result shouldBe expectedSettings
    }

    @Test
    fun collect_shouldReturn_notificationSettings_withCorrectValues_denied() = runTest {
        val testRegistration: ServiceWorkerRegistration = js("({ installing: {} })")
        everySuspend { mockPushService.getPermissionState() } returns PermissionState.Denied
        everySuspend { mockPushService.getServiceWorkerRegistration() } returns testRegistration
        everySuspend { mockJsPushWrapperApi.isSubscribed() } returns true
        val expectedSettings = WebNotificationSettings(
            PermissionState.Denied,
            isServiceWorkerRegistered = false,
            isSubscribed = true
        )

        val result = webNotificationSettingsCollector.collect()

        result shouldBe expectedSettings
    }
}