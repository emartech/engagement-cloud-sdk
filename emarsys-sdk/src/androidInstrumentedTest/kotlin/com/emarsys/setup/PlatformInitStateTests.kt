package com.emarsys.setup

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.emarsys.mobileengage.push.PushMessageBroadcastReceiver
import com.emarsys.mobileengage.push.PushTokenBroadcastReceiver
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class PlatformInitStateTests {

    private lateinit var mockPushTokenBroadcastReceiver: PushTokenBroadcastReceiver

    private lateinit var mockPushMessageBroadcastReceiver: PushMessageBroadcastReceiver

    private lateinit var mockTokenIntentFilter: IntentFilter

    private lateinit var mockPushMessageIntentFilter: IntentFilter

    private lateinit var mockContext: Context

    private lateinit var platformInitState: PlatformInitState

    @BeforeTest
    fun setup() = runTest {
        mockPushTokenBroadcastReceiver = mockk(relaxed = true)
        mockPushMessageBroadcastReceiver = mockk(relaxed = true)
        mockTokenIntentFilter = mockk(relaxed = true)
        mockPushMessageIntentFilter = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)

        mockkStatic(ContextCompat::class)

        platformInitState =
            PlatformInitState(
                mockPushTokenBroadcastReceiver,
                mockTokenIntentFilter,
                mockPushMessageBroadcastReceiver,
                mockPushMessageIntentFilter,
                mockContext
            )
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testPrepare_should_registerPushTokenBroadcastReceiver() = runTest {
        every {
            ContextCompat.registerReceiver(
                mockContext,
                mockPushTokenBroadcastReceiver,
                mockTokenIntentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } returns Intent()

        platformInitState.prepare()

        verify {
            ContextCompat.registerReceiver(
                mockContext,
                mockPushTokenBroadcastReceiver,
                mockTokenIntentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    @Test
    fun testPrepare_should_registerPushMessageBroadcastReceiver() = runTest {
        every {
            ContextCompat.registerReceiver(
                mockContext,
                mockPushMessageBroadcastReceiver,
                mockPushMessageIntentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } returns Intent()

        platformInitState.prepare()

        verify {
            ContextCompat.registerReceiver(
                mockContext,
                mockPushMessageBroadcastReceiver,
                mockPushMessageIntentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }
}