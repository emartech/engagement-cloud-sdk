package com.emarsys.mobileengage.push

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushConstants
import com.emarsys.core.storage.StringStorage
import com.emarsys.di.DependencyContainer
import com.emarsys.di.DependencyInjection
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PushTokenBroadcastReceiverTest {
    private companion object {
        const val PUSH_TOKEN = "testToken"
    }

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var broadcastReceiver: PushTokenBroadcastReceiver
    private lateinit var mockDependencyContainer: DependencyContainer
    private lateinit var mockPushApi: PushApi
    private lateinit var mockStringStorage: StringStorage

    @Before
    fun setUp() {
        mockkObject(DependencyInjection)
        sdkDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(sdkDispatcher)

        mockStringStorage = mockk(relaxed = true)
        mockPushApi = mockk(relaxed = true)
        mockDependencyContainer = mockk(relaxed = true)
        every { mockDependencyContainer.pushApi } returns mockPushApi
        every { mockDependencyContainer.sdkDispatcher } returns sdkDispatcher
        every { mockDependencyContainer.stringStorage } returns mockStringStorage

        every { DependencyInjection.container } returns mockDependencyContainer

        broadcastReceiver = PushTokenBroadcastReceiver()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun testOnReceive_shouldCallPushApiWithToken() = runTest {
        val intent = Intent().apply {
            action = "com.emarsys.sdk.PUSH_TOKEN"
            putExtra("pushToken", PUSH_TOKEN)
        }

        broadcastReceiver.onReceive(context, intent)

        advanceUntilIdle()

        coVerify { mockPushApi.registerPushToken(PUSH_TOKEN) }
    }

    @Test
    fun testOnReceive_shouldCallPut_onStringStorage_withToken() = runTest {
        val intent = Intent().apply {
            action = "com.emarsys.sdk.PUSH_TOKEN"
            putExtra("pushToken", PUSH_TOKEN)
        }

        broadcastReceiver.onReceive(context, intent)

        advanceUntilIdle()

        coVerify { mockStringStorage.put(PushConstants.PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) }
    }
}