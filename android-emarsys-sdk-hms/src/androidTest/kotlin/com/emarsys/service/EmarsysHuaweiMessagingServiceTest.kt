package com.emarsys.service

import android.os.Looper
import androidx.test.platform.app.InstrumentationRegistry
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

class EmarsysHuaweiMessagingServiceTest {

    private lateinit var huaweiMessagingService: EmarsysHuaweiMessagingService

    @Before
    fun setup() {
        if (Looper.myLooper() == null) {
            Looper.prepare()
        }

        huaweiMessagingService = EmarsysHuaweiMessagingService()
        setField(
            huaweiMessagingService,
            EmarsysHuaweiMessagingService::class.java,
            "application",
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        )
    }

    @Test
    fun registerMessagingService_shouldAddMessagingServiceToList() {
        val testService1 = HmsMessageService()
        val testService2 = HmsMessageService()

        EmarsysHuaweiMessagingService.registerMessagingService(testService1)
        EmarsysHuaweiMessagingService.registerMessagingService(testService2, true)

        val result = EmarsysHuaweiMessagingService.messagingServices

        result.size shouldBe 2
        result[0].first shouldBe false
        result[0].second shouldBe testService1
        result[1].first shouldBe true
        result[1].second shouldBe testService2
    }

    @Test
    fun onMessageReceived_shouldCallRegisteredMessagingServices() {
        val mockMessagingService = mockk<HmsMessageService>(relaxed = true)
        val mockMessage = mockk<RemoteMessage>(relaxed = true)
        every { mockMessage.dataOfMap } returns mapOf()
        EmarsysHuaweiMessagingService.registerMessagingService(mockMessagingService, true)

        huaweiMessagingService.onMessageReceived(mockMessage)
        verify { mockMessagingService.onMessageReceived(mockMessage) }
    }

    @Test
    fun onMessageReceived_shouldFilterEmarsysMessages() {
        val mockMessagingService = mockk<HmsMessageService>(relaxed = true)
        val mockMessage1 = mockk<RemoteMessage>(relaxed = true)
        every { mockMessage1.dataOfMap } returns mapOf("ems_msg" to "true")
        val mockMessage2 = mockk<RemoteMessage>(relaxed = true)
        every { mockMessage2.dataOfMap } returns mapOf()
        EmarsysHuaweiMessagingService.registerMessagingService(mockMessagingService, false)

        huaweiMessagingService.onMessageReceived(mockMessage1)
        huaweiMessagingService.onMessageReceived(mockMessage2)
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
}