@file:OptIn(BetaInteropApi::class)

package com.emarsys.iosNotificationService

import com.emarsys.iosNotificationService.notification.FakeNotificationCenter
import com.emarsys.iosNotificationService.notification.NotificationCenterApi
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.cinterop.BetaInteropApi
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

    private lateinit var emarsysNotificationService: EmarsysNotificationService
    private val fakeNotificationCenter: NotificationCenterApi = FakeNotificationCenter()

    @BeforeTest
    fun setup() = runTest {
        emarsysNotificationService = EmarsysNotificationService(fakeNotificationCenter)
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

        val result = emarsysNotificationService.didReceiveNotificationRequest(request)

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

        val result = emarsysNotificationService.didReceiveNotificationRequest(request)

        result.attachments shouldNotBe expectedAttachments
    }


    @Test
    fun didReceiveNotificationRequest_shouldNotCrash_whenImageUrlIsNull() = runTest {
        val content = UNMutableNotificationContent()
        content.setUserInfo(mapOf("image_url" to null))

        val request = UNNotificationRequest.requestWithIdentifier("testId", content, null)

        val result = emarsysNotificationService.didReceiveNotificationRequest(request)

        result.attachments shouldBe emptyList<UNNotificationAttachment>()
    }

    @Test
    fun didReceiveNotificationRequest_shouldNotCrash_whenImageUrlIsEmptyString() = runTest {
        val content = UNMutableNotificationContent()
        content.setUserInfo(mapOf("image_url" to ""))

        val request = UNNotificationRequest.requestWithIdentifier("testId", content, null)

        val result = emarsysNotificationService.didReceiveNotificationRequest(request)

        result.attachments shouldBe emptyList<UNNotificationAttachment>()
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

        val result = emarsysNotificationService.didReceiveNotificationRequest(request)

        val inAppData =
            ((result.userInfo["ems"] as Map<String, Any>)["inapp"] as Map<String, Any>)["inAppData"]
        inAppData shouldNotBe null
    }
}

suspend fun EmarsysNotificationService.didReceiveNotificationRequest(request: UNNotificationRequest): UNNotificationContent =
    suspendCancellableCoroutine { continuation ->
        didReceiveNotificationRequest(request) { content ->
            continuation.resume(content)
        }
    }