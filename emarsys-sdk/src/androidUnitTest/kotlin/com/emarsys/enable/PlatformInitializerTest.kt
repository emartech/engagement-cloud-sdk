package com.emarsys.enable

import android.app.NotificationManager
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.event.SdkEvent
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class PlatformInitializerTest {
    private lateinit var sdkEventDistributor: SdkEventDistributorApi
    private lateinit var sdkEventFlow: MutableSharedFlow<SdkEvent>
    private lateinit var mockNotificationManager: NotificationManager

    private lateinit var platformInitializer: PlatformInitializer

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        sdkEventFlow = MutableSharedFlow()
        sdkEventDistributor = mockk()
        every { sdkEventDistributor.sdkEventFlow } returns sdkEventFlow

        mockNotificationManager = mockk(relaxed = true)
        platformInitializer = PlatformInitializer(
            sdkEventDistributor,
            mockNotificationManager,
            StandardTestDispatcher()
        )
    }

    @Test
    fun init_should_cancelNotification_whenSdkEvent_isDismiss() = runTest {
        val testId2 = "dismissId2"
        val testId1 = "dismissId1"

        platformInitializer.init()

        sdkEventFlow.emit(SdkEvent.Internal.Sdk.Dismiss(id = testId1))
        sdkEventFlow.emit(SdkEvent.Internal.Sdk.Dismiss(id = testId2))

        advanceUntilIdle()

        verify {
            mockNotificationManager.cancel(testId1, testId1.hashCode())
            mockNotificationManager.cancel(testId2, testId2.hashCode())
        }
    }

    @Test
    fun init_should_doNothing_whenSdkEvent_isNotDismiss() = runTest {
        platformInitializer.init()

        sdkEventFlow.emit(SdkEvent.Internal.Sdk.Metric())

        advanceUntilIdle()

        verify {
            mockNotificationManager wasNot Called
        }
    }

}