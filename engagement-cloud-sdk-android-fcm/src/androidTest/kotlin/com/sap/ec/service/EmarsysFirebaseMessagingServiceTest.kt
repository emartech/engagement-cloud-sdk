package com.sap.ec.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sap.ec.service.fcm.SAPEngagementCloudFirebaseMessagingService
import com.sap.ec.service.fcm.model.FirebaseMessagingServiceRegistrationOptions
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

class EmarsysFirebaseMessagingServiceTest {
    companion object {
        const val PACKAGE_NAME = "com.sap.ec.package"
    }

    private lateinit var engagementCloudFirebaseMessagingService: SAPEngagementCloudFirebaseMessagingService
    private lateinit var mockContext: Context
    private lateinit var mockReceiver: BroadcastReceiver

    @Before
    fun setup() {
        mockReceiver = mockk(relaxed = true)

        mockContext = mockk(relaxed = true)
        every { mockContext.applicationContext } returns mockContext
        every { mockContext.packageName } returns PACKAGE_NAME

        engagementCloudFirebaseMessagingService = SAPEngagementCloudFirebaseMessagingService()
        setField(
            engagementCloudFirebaseMessagingService,
            engagementCloudFirebaseMessagingService::class.java,
            "mBase",
            InstrumentationRegistry.getInstrumentation().targetContext
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
        SAPEngagementCloudFirebaseMessagingService.messagingServices.clear()
    }

    @Test
    fun registerMessagingService_shouldAddMessagingService_toList() {
        val testMessagingService1 = FirebaseMessagingService()
        val testMessagingService2 = FirebaseMessagingService()
        SAPEngagementCloudFirebaseMessagingService.registerMessagingService(testMessagingService1)
        SAPEngagementCloudFirebaseMessagingService.registerMessagingService(
            testMessagingService2,
            FirebaseMessagingServiceRegistrationOptions(true)
        )

        val result = SAPEngagementCloudFirebaseMessagingService.messagingServices

        result.size shouldBe 2
        result[0].first.includeEmarsysMessages shouldBe false
        result[0].second shouldBe testMessagingService1
        result[1].first.includeEmarsysMessages shouldBe true
        result[1].second shouldBe testMessagingService2
    }

    @Test
    fun onNewToken_shouldSendBroadcast() {
        val pushToken = "testPushToken"
        val intentSlot: CapturingSlot<Intent> = slot()
        setField(
            engagementCloudFirebaseMessagingService,
            SAPEngagementCloudFirebaseMessagingService::class.java,
            "mBase",
            mockContext
        )

        engagementCloudFirebaseMessagingService.onNewToken(pushToken)

        verify { mockContext.sendBroadcast(capture(intentSlot)) }

        with(intentSlot.captured) {
            getStringExtra("pushToken") shouldBe pushToken
            action shouldBe "com.sap.ec.sdk.PUSH_TOKEN"
            `package` shouldBe PACKAGE_NAME
        }
    }

    @Test
    fun onNewToken_shouldSendBroadcast_withAnIntentContaining_thePackageName() {
        val pushToken = "testPushToken"
        val intentSlot: CapturingSlot<Intent> = slot()

        registerReceiver("com.sap.ec.sdk.PUSH_TOKEN")

        engagementCloudFirebaseMessagingService.onNewToken(pushToken)

        verify {
            mockReceiver.onReceive(
                InstrumentationRegistry.getInstrumentation().targetContext,
                capture(intentSlot)
            )
        }

        with(intentSlot.captured) {
            getStringExtra("pushToken") shouldBe pushToken
            action shouldBe "com.sap.ec.sdk.PUSH_TOKEN"
            `package` shouldNotBe null
        }

        InstrumentationRegistry.getInstrumentation().targetContext.unregisterReceiver(mockReceiver)
    }

    @Test
    fun onMessageReceived_shouldCallRegisteredMessagingServices() {
        val mockMessagingService = mockk<FirebaseMessagingService>(relaxed = true)
        val mockMessage = mockk<RemoteMessage>(relaxed = true)
        every { mockMessage.data } returns mapOf()
        SAPEngagementCloudFirebaseMessagingService.registerMessagingService(
            mockMessagingService,
            FirebaseMessagingServiceRegistrationOptions(true)
        )

        engagementCloudFirebaseMessagingService.onMessageReceived(mockMessage)
        verify { mockMessagingService.onMessageReceived(mockMessage) }
    }

    @Test
    fun onMessageReceived_shouldSendBroadcast_withAnIntentContaining_thePackageName() {
        val testPayload = mapOf("ems.version" to "version1", "notification.title" to "title")
        val intentSlot: CapturingSlot<Intent> = slot()
        val mockMessagingService = mockk<FirebaseMessagingService>(relaxed = true)
        val mockMessage = mockk<RemoteMessage>(relaxed = true)
        every { mockMessage.data } returns testPayload

        registerReceiver("com.sap.ec.sdk.PUSH_MESSAGE_PAYLOAD")

        SAPEngagementCloudFirebaseMessagingService.registerMessagingService(
            mockMessagingService,
            FirebaseMessagingServiceRegistrationOptions(true)
        )

        engagementCloudFirebaseMessagingService.onMessageReceived(mockMessage)

        verify {
            mockReceiver.onReceive(
                InstrumentationRegistry.getInstrumentation().targetContext,
                capture(intentSlot)
            )
        }

        with(intentSlot.captured) {
            getStringExtra("pushPayload") shouldBe Json.encodeToString(testPayload)
            action shouldBe "com.sap.ec.sdk.PUSH_MESSAGE_PAYLOAD"
            `package` shouldNotBe null
        }

        InstrumentationRegistry.getInstrumentation().targetContext.unregisterReceiver(mockReceiver)
    }

    @Test
    fun onMessageReceived_shouldFilterEmarsysMessages() {
        val mockMessagingService = mockk<FirebaseMessagingService>(relaxed = true)
        val mockMessage1 = mockk<RemoteMessage>(relaxed = true)
        every { mockMessage1.data } returns mapOf("ems.version" to "version1")
        val mockMessage2 = mockk<RemoteMessage>(relaxed = true)
        every { mockMessage2.data } returns mapOf()
        SAPEngagementCloudFirebaseMessagingService.registerMessagingService(
            mockMessagingService,
            FirebaseMessagingServiceRegistrationOptions(false)
        )

        engagementCloudFirebaseMessagingService.onMessageReceived(mockMessage1)
        engagementCloudFirebaseMessagingService.onMessageReceived(mockMessage2)
        verify(exactly = 0) {
            mockMessagingService.onMessageReceived(mockMessage1)
        }
        verify {
            mockMessagingService.onMessageReceived(mockMessage2)
        }
    }

    private fun setField(instance: Any?, type: Class<*>, fieldName: String, value: Any?) {
        val containerField = searchForField(type, fieldName)
        containerField.isAccessible = true
        containerField.set(instance, value)
    }

    private fun searchForField(type: Class<*>, fieldName: String): Field = try {
        type.getDeclaredField(fieldName)
    } catch (nsfe: NoSuchFieldException) {
        nsfe
    }.let { result ->
        when (result) {
            is NoSuchFieldException -> when (val superclass = type.superclass) {
                null -> throw NoSuchFieldException("Could not find field in class hierarchy!")
                else -> searchForField(superclass, fieldName)
            }

            is Field -> result
            else -> throw IllegalStateException("Unrecognized type: ${result.javaClass}")
        }
    }

    private fun registerReceiver(intentFilterAction: String) {
        InstrumentationRegistry.getInstrumentation().targetContext.registerReceiver(
            mockReceiver, IntentFilter(intentFilterAction), 4
        )
    }
}