@file:OptIn(BetaInteropApi::class)

package com.sap.ec

import com.sap.ec.iosNotificationService.notification.FakeNotificationCenter
import com.sap.ec.iosNotificationService.notification.NotificationCenterApi
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
class EngagementCloudNotificationServiceTests {

    private lateinit var engagementCloudNotificationService: EngagementCloudNotificationService
    private val fakeNotificationCenter: NotificationCenterApi = FakeNotificationCenter()

    @BeforeTest
    fun setup() = runTest {
        engagementCloudNotificationService =
            EngagementCloudNotificationService(fakeNotificationCenter)
    }

    @Test
    fun didReceiveNotificationRequest_shouldSetNotificationCategory() = runTest {
        val content = UNMutableNotificationContent()
        content.setUserInfo(
            mapOf(
                "notification" to mapOf(
                    "actions" to listOf(
                        mapOf(
                            "id" to "testId",
                            "reporting" to """{"reportingKey":"reportingValue"}""",
                            "title" to "testTitle",
                            "type" to "Dismiss"
                        )
                    )
                )
            )
        )
        content.categoryIdentifier shouldBe ""

        val request = UNNotificationRequest.requestWithIdentifier("testId", content, null)

        val result = engagementCloudNotificationService.didReceiveNotificationRequest(request)

        result.categoryIdentifier shouldNotBe ""
    }

    @Test
    fun didReceiveNotificationRequest_shouldSetNotificationAttachment() = runTest {
        val expectedAttachments: List<UNNotificationAttachment> = emptyList()
        val content = UNMutableNotificationContent()
        content.setUserInfo(
            mapOf(
                "notification" to mapOf(
                    "imageUrl" to "https://mobile-sdk-config-staging.gservice.emarsys.com/testing/Emarsys.png"
                )
            )
        )
        content.attachments shouldBe expectedAttachments

        val request = UNNotificationRequest.requestWithIdentifier("testId", content, null)

        val result = engagementCloudNotificationService.didReceiveNotificationRequest(request)

        result.attachments shouldNotBe expectedAttachments
    }


    @Test
    fun didReceiveNotificationRequest_shouldNotCrash_whenImageUrlIsNull() = runTest {
        val content = UNMutableNotificationContent()
        content.setUserInfo(mapOf("imageUrl" to null))

        val request = UNNotificationRequest.requestWithIdentifier("testId", content, null)

        val result = engagementCloudNotificationService.didReceiveNotificationRequest(request)

        result.attachments shouldBe emptyList<UNNotificationAttachment>()
    }

    @Test
    fun didReceiveNotificationRequest_shouldNotCrash_whenImageUrlIsEmptyString() = runTest {
        val content = UNMutableNotificationContent()
        content.setUserInfo(mapOf("imageUrl" to ""))

        val request = UNNotificationRequest.requestWithIdentifier("testId", content, null)

        val result = engagementCloudNotificationService.didReceiveNotificationRequest(request)

        result.attachments shouldBe emptyList<UNNotificationAttachment>()
    }
}

suspend fun EngagementCloudNotificationService.didReceiveNotificationRequest(request: UNNotificationRequest): UNNotificationContent =
    suspendCancellableCoroutine { continuation ->
        didReceiveNotificationRequest(request) { content ->
            continuation.resume(content)
        }
    }