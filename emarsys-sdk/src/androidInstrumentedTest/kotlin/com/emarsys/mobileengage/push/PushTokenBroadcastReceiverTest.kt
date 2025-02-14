package com.emarsys.mobileengage.push

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.emarsys.api.push.PushApi
import io.mockk.coVerify
import io.mockk.mockk
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
    private lateinit var mockPushApi: PushApi
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var broadcastReceiver: PushTokenBroadcastReceiver

    @Before
    fun setUp() {
        mockPushApi = mockk(relaxed = true)
        sdkDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(sdkDispatcher)

        broadcastReceiver = PushTokenBroadcastReceiver(sdkDispatcher, mockPushApi)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
}