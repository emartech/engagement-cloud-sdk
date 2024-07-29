package com.emarsys.iosNotificationService

import com.emarsys.iosNotificationService.notification.FakeNotificationCenter
import com.emarsys.iosNotificationService.notification.NotificationCenterApi
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationAttachment
import platform.UserNotifications.UNNotificationContent
import platform.UserNotifications.UNNotificationRequest
import kotlin.coroutines.resume
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(BetaInteropApi::class)
class NotificationServiceTests {

    private lateinit var notificationService: NotificationService
    private val fakeNotificationCenter: NotificationCenterApi = FakeNotificationCenter()

    @BeforeTest
    fun setup() = runTest {
        notificationService = NotificationService(fakeNotificationCenter)
    }

    @Test
    fun didReceiveNotificationRequest_shouldSetNotificationCategory() = runTest {
        val content = UNMutableNotificationContent()
        content.setUserInfo(
            mapOf(
                "ems" to mapOf(
                    "actions" to listOf(
                        mapOf(
                            "id" to "testId",
                            "title" to "testTitle",
                            "type" to "Dismiss"
                        )
                    )
                )
            )
        )
        content.categoryIdentifier shouldBe ""

        val request = UNNotificationRequest.requestWithIdentifier("testId", content, null)

        val result = notificationService.didReceiveNotificationRequest(request)

        result.categoryIdentifier shouldNotBe ""
    }

    @Test
    fun didReceiveNotificationRequest_shouldSetNotificationAttachment() = runTest {
        val expectedAttachments: List<UNNotificationAttachment> = emptyList()
        val content = UNMutableNotificationContent()
        content.setUserInfo(
            mapOf(
                "image_url" to "https://mobile-sdk-config-staging.gservice.emarsys.com/testing/Emarsys.png"
            )
        )
        content.attachments shouldBe expectedAttachments

        val request = UNNotificationRequest.requestWithIdentifier("testId", content, null)

        val result = notificationService.didReceiveNotificationRequest(request)

        result.attachments shouldNotBe expectedAttachments
    }

    @Test
    fun didReceiveNotificationRequest_shouldSetInAppData() = runTest {
        val content = UNMutableNotificationContent()
        content.setUserInfo(
            mapOf(
                "ems" to mapOf(
                    "inapp" to mapOf(
                        "url" to "https://mobile-sdk-config-staging.gservice.emarsys.com/testing/Emarsys.png"
                    )
                )
            )
        )

        val request = UNNotificationRequest.requestWithIdentifier("testId", content, null)

        val result = notificationService.didReceiveNotificationRequest(request)

        val inAppData = ((result.userInfo["ems"] as Map<String, Any>)["inapp"] as Map<String, Any>)["inAppData"]
        inAppData shouldNotBe null
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun NotificationService.didReceiveNotificationRequest(request: UNNotificationRequest): UNNotificationContent =
    suspendCancellableCoroutine { continuation ->
        didReceiveNotificationRequest(request) { content ->
            continuation.resume(content!!)
        }
    }