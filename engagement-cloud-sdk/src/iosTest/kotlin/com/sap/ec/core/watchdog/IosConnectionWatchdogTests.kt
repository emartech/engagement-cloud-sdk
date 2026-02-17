package com.sap.ec.core.watchdog

import com.sap.ec.core.watchdog.connection.IosConnectionWatchdog
import com.sap.ec.core.watchdog.connection.NetworkConnection
import com.sap.ec.core.watchdog.connection.Reachability
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class IosConnectionWatchdogTests {

    @Test
    fun `isOnline is true when initially connected`() = runTest {
        val reachabilityWrapper: Reachability = mock()
        every { reachabilityWrapper.isConnected() } returns true
        every { reachabilityWrapper.subscribeToNetworkChanges(any()) } returns Unit

        val watchdog = IosConnectionWatchdog(reachabilityWrapper)

        watchdog.register()

        watchdog.isOnline.first() shouldBe true
        verify { reachabilityWrapper.isConnected() }
        verify { reachabilityWrapper.subscribeToNetworkChanges(any()) }
    }

    @Test
    fun `isOnline updates when network status changes to offline`() = runTest {
        val reachabilityWrapper: Reachability = object : Reachability {
            override fun subscribeToNetworkChanges(lambda: (Boolean) -> Unit) {
                lambda(true)
                lambda(false)
            }

            override fun isConnected(): Boolean {
                return true
            }

            override fun getNetworkConnection(): NetworkConnection {
                return NetworkConnection.Wifi
            }
        }

        val watchdog = IosConnectionWatchdog(reachabilityWrapper)

        watchdog.register()

        watchdog.isOnline.first() shouldBe false
    }

    @Test
    fun `isOnline updates when network status changes to online`() = runTest {
        val reachabilityWrapper: Reachability = object : Reachability {
            override fun subscribeToNetworkChanges(lambda: (Boolean) -> Unit) {
                lambda(false)
                lambda(true)
            }

            override fun isConnected(): Boolean {
                return false
            }

            override fun getNetworkConnection(): NetworkConnection {
                return NetworkConnection.Wifi
            }
        }

        val watchdog = IosConnectionWatchdog(reachabilityWrapper)

        watchdog.register()

        watchdog.isOnline.first() shouldBe true
    }
}