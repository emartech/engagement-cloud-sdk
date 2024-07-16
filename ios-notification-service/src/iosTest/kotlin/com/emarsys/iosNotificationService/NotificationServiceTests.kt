package com.emarsys.iosNotificationService

import com.emarsys.iosNotificationService.notification.FakeNotificationCenter
import com.emarsys.iosNotificationService.notification.NotificationCenterApi
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationContent
import platform.UserNotifications.UNNotificationRequest
import kotlin.coroutines.resume
import kotlin.test.BeforeTest
import kotlin.test.Test

class NotificationServiceTests {

    private lateinit var notificationService: NotificationService
    private val fakeNotificationCenter: NotificationCenterApi = FakeNotificationCenter()

    @BeforeTest
    fun setup() = runTest {
        notificationService = NotificationService(fakeNotificationCenter)
    }

    @Test
    fun didReceiveNotificationRequest_shouldContainNotificationCategory() = runTest {
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
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun NotificationService.didReceiveNotificationRequest(request: UNNotificationRequest): UNNotificationContent =
    suspendCancellableCoroutine { continuation ->
        didReceiveNotificationRequest(request) { content ->
            continuation.resume(content!!)
        }
    }