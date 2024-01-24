package com.emarsys.setup

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.emarsys.push.PushTokenBroadcastReceiver
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class PlatformInitStateTests {

    private lateinit var mockPushTokenBroadcastReceiver: PushTokenBroadcastReceiver

    private lateinit var mockIntentFilter: IntentFilter

    private lateinit var mockContext: Context

    private lateinit var platformInitState: PlatformInitState

    @BeforeTest
    fun setup() = runTest {
        mockPushTokenBroadcastReceiver = mockk(relaxed = true)
        mockIntentFilter = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)

        mockkStatic(ContextCompat::class)

        platformInitState =
            PlatformInitState(mockPushTokenBroadcastReceiver, mockIntentFilter, mockContext)
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testPrepare_should_registerPushTokenBroadcastReceiver() = runTest {
        slot<IntentFilter>()
        every {
            ContextCompat.registerReceiver(
                mockContext,
                mockPushTokenBroadcastReceiver,
                mockIntentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } returns Intent()

        platformInitState.prepare()

        verify {
            ContextCompat.registerReceiver(
                mockContext,
                mockPushTokenBroadcastReceiver,
                mockIntentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }
}