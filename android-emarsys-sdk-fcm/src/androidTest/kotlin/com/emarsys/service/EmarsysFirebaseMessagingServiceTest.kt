package com.emarsys.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.junit.Before
import org.junit.Test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class EmarsysFirebaseMessagingServiceTest {
    private lateinit var emarsysFirebaseMessagingService: EmarsysFirebaseMessagingService

    @Before
    fun setup() {
        emarsysFirebaseMessagingService = EmarsysFirebaseMessagingService()
    }

    @Test
    fun registerMessagingService_shouldAddMessagingService_toList() {
        val testMessagingService1 = FirebaseMessagingService()
        val testMessagingService2 = FirebaseMessagingService()
        emarsysFirebaseMessagingService.registerMessagingService(testMessagingService1)
        emarsysFirebaseMessagingService.registerMessagingService(testMessagingService2, true)

        val result = EmarsysFirebaseMessagingService.messagingServices

        result.size shouldBe 2
        result[0].first shouldBe false
        result[0].second shouldBe testMessagingService1
        result[1].first shouldBe true
        result[1].second shouldBe testMessagingService2
    }

    @Test
    fun onMessageReceived_shouldCallRegisteredMessagingServices() {
        val mockMessagingService = mockk<FirebaseMessagingService>(relaxed = true)
        val mockMessage = mockk<RemoteMessage>(relaxed = true)
        every { mockMessage.data  } returns mapOf()
        emarsysFirebaseMessagingService.registerMessagingService(mockMessagingService, true)

        emarsysFirebaseMessagingService.onMessageReceived(mockMessage)
        verify { mockMessagingService.onMessageReceived(mockMessage) }
    }

    @Test
    fun onMessageReceived_shouldFilterEmarsysMessages() {
        val mockMessagingService = mockk<FirebaseMessagingService>(relaxed = true)
        val mockMessage1 = mockk<RemoteMessage>(relaxed = true)
        every { mockMessage1.data  } returns mapOf("ems.version" to "version1")
        val mockMessage2 = mockk<RemoteMessage>(relaxed = true)
        every { mockMessage2.data  } returns mapOf()
        emarsysFirebaseMessagingService.registerMessagingService(mockMessagingService, false)

        emarsysFirebaseMessagingService.onMessageReceived(mockMessage1)
        emarsysFirebaseMessagingService.onMessageReceived(mockMessage2)
        verify(exactly = 0) {
            mockMessagingService.onMessageReceived(mockMessage1)
        }
        verify {
            mockMessagingService.onMessageReceived(mockMessage2)
        }
    }
}