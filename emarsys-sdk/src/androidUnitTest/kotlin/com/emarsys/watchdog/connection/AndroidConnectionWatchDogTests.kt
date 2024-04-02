package com.emarsys.watchdog.connection

import android.net.ConnectivityManager
import android.net.Network
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class AndroidConnectionWatchDogTests {

    private lateinit var androidConnectionWatchDog: AndroidConnectionWatchDog

    private lateinit var mockConnectivityManager: ConnectivityManager

    @Before
    fun setUp() {
        mockConnectivityManager = mockk<ConnectivityManager>(relaxed = true)
        androidConnectionWatchDog = AndroidConnectionWatchDog(mockConnectivityManager, mockk(relaxed = true))
    }

    @Test
    fun testStart_shouldRegisterItselfAsDefaultNetworkCallback() {
        androidConnectionWatchDog.start()

        verify { mockConnectivityManager.registerDefaultNetworkCallback(androidConnectionWatchDog) }
    }

    @Test
    fun testOnAvailable_shouldSetIsOnlineToTrue_whenPreviouslyFalse() {
        val network = mockk<Network>()
        androidConnectionWatchDog.isOnline.value shouldBe false

        androidConnectionWatchDog.onAvailable(network)

        androidConnectionWatchDog.isOnline.value shouldBe true
    }

    @Test
    fun testOnAvailable_shouldLeaveIsOnlineAsTrue_whenPreviouslyTrue() {
        val network = mockk<Network>()
        androidConnectionWatchDog.onAvailable(network)

        androidConnectionWatchDog.onAvailable(network)

        androidConnectionWatchDog.isOnline.value shouldBe true
    }

    @Test
    fun testOnLost_shouldSetIsOnlineToFalse_whenPreviouslyTrue() {
        val network = mockk<Network>()
        androidConnectionWatchDog.onAvailable(network)

        androidConnectionWatchDog.onLost(network)

        androidConnectionWatchDog.isOnline.value shouldBe false
    }

    @Test
    fun testOnLost_shouldLeaveIsOnlineAsFalse_whenPreviouslyFalse() {
        val network = mockk<Network>()

        androidConnectionWatchDog.onLost(network)

        androidConnectionWatchDog.isOnline.value shouldBe false
    }

}