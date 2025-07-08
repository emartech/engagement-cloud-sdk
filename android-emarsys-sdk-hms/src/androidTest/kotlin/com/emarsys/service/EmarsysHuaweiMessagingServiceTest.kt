package com.emarsys.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Looper
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.service.hms.EmarsysHuaweiMessagingService
import com.emarsys.service.hms.model.HuaweiMessagingServiceRegistrationOptions
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

class EmarsysHuaweiMessagingServiceTest {
    companion object {
        const val PACKAGE_NAME = "com.emarsys.package"
    }

    private lateinit var huaweiMessagingService: EmarsysHuaweiMessagingService
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        if (Looper.myLooper() == null) {
            Looper.prepare()
        }

        mockContext = mockk(relaxed = true)
        every { mockContext.applicationContext } returns mockContext
        every { mockContext.packageName } returns PACKAGE_NAME

        huaweiMessagingService = EmarsysHuaweiMessagingService()
        setField(
            huaweiMessagingService,
            EmarsysHuaweiMessagingService::class.java,
            "mBase",
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        )
    }

    @Test
    fun registerMessagingService_shouldAddMessagingServiceToList() {
        val testService1 = HmsMessageService()
        val testService2 = HmsMessageService()

        EmarsysHuaweiMessagingService.registerMessagingService(testService1)
        EmarsysHuaweiMessagingService.registerMessagingService(
            testService2,
            HuaweiMessagingServiceRegistrationOptions(includeEmarsysMessages = true)
        )

        val result = EmarsysHuaweiMessagingService.messagingServices

        result.size shouldBe 2
        result[0].first.includeEmarsysMessages shouldBe false
        result[0].second shouldBe testService1
        result[1].first. includeEmarsysMessages shouldBe true
        result[1].second shouldBe testService2
    }

    @Test
    fun onNewToken_shouldSendBroadcast_withPushToken() {
        val testPushToken = "testPushToken"
        val mockReceiver: BroadcastReceiver = mockk(relaxed = true)
        val intentSlot: CapturingSlot<Intent> = slot()

        InstrumentationRegistry.getInstrumentation().targetContext.registerReceiver(
            mockReceiver, IntentFilter("com.emarsys.sdk.PUSH_TOKEN"), 4
        )

        huaweiMessagingService.onNewToken(testPushToken)

        verify {
            mockReceiver.onReceive(
                InstrumentationRegistry.getInstrumentation().targetContext,
                capture(intentSlot)
            )
        }

        with(intentSlot.captured) {
            getStringExtra("pushToken") shouldBe testPushToken
            action shouldBe "com.emarsys.sdk.PUSH_TOKEN"
        }
    }

    @Test
    fun onMessageReceived_shouldCallRegisteredMessagingServices() {
        val mockMessagingService = mockk<HmsMessageService>(relaxed = true)
        val mockMessage = mockk<RemoteMessage>(relaxed = true)
        every { mockMessage.dataOfMap } returns mapOf()
        EmarsysHuaweiMessagingService.registerMessagingService(
            mockMessagingService,
            HuaweiMessagingServiceRegistrationOptions(includeEmarsysMessages = true)
        )

        huaweiMessagingService.onMessageReceived(mockMessage)
        verify { mockMessagingService.onMessageReceived(mockMessage) }
    }

    @Test
    fun onMessageReceived_shouldFilterEmarsysMessages() {
        val mockMessagingService = mockk<HmsMessageService>(relaxed = true)
        val mockMessage1 = mockk<RemoteMessage>(relaxed = true)
        every { mockMessage1.dataOfMap } returns mapOf("ems" to "true")
        val mockMessage2 = mockk<RemoteMessage>(relaxed = true)
        every { mockMessage2.dataOfMap } returns mapOf()
        EmarsysHuaweiMessagingService.registerMessagingService(
            mockMessagingService,
            HuaweiMessagingServiceRegistrationOptions(includeEmarsysMessages = false)
        )

        huaweiMessagingService.onMessageReceived(mockMessage1)
        huaweiMessagingService.onMessageReceived(mockMessage2)
        verify(exactly = 0) {
            mockMessagingService.onMessageReceived(mockMessage1)
        }
        verify {
            mockMessagingService.onMessageReceived(mockMessage2)
        }
    }

    @Test
    fun onMessageReceived_shouldSendBroadCast_withRemotePayload() {
        val mockReceiver: BroadcastReceiver = mockk(relaxed = true)
        val intentSlot: CapturingSlot<Intent> = slot()
        val testRemotePayload = mapOf("ems" to mapOf("version" to "HUAWEI_V2").toString(), "payload" to "content")
        val mockMessage = mockk<RemoteMessage>(relaxed = true)
        every { mockMessage.data } returns Json.encodeToString(testRemotePayload)

        InstrumentationRegistry.getInstrumentation().targetContext.registerReceiver(
            mockReceiver, IntentFilter("com.emarsys.sdk.PUSH_MESSAGE_PAYLOAD"), 4
        )

        huaweiMessagingService.onMessageReceived(mockMessage)

        verify {
            mockReceiver.onReceive(
                InstrumentationRegistry.getInstrumentation().targetContext,
                capture(intentSlot)
            )
        }

        with(intentSlot.captured) {
            getStringExtra("pushPayload") shouldBe Json.encodeToString(testRemotePayload)
            action shouldBe "com.emarsys.sdk.PUSH_MESSAGE_PAYLOAD"
            `package` shouldNotBe null
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
}