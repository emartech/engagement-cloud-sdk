package com.emarsys.watchdog.connection

import io.kotest.matchers.shouldBe
import kotlinx.browser.window
import org.w3c.dom.events.Event
import kotlin.test.BeforeTest
import kotlin.test.Test

class WebConnectionWatchDogTests {

    private lateinit var webConnectionWatchDog: WebConnectionWatchDog

    @BeforeTest
    fun setup() {
        webConnectionWatchDog= WebConnectionWatchDog(window)
    }

    @Test
    fun testIsOnline_shouldGetConnectionState_FromNavigator() {
        webConnectionWatchDog.isOnline.value shouldBe window.navigator.onLine
    }

    @Test
    fun testStart_shouldAddOnlineEventListener() {
        webConnectionWatchDog.start()

        window.dispatchEvent(Event("online"))

        webConnectionWatchDog.isOnline.value shouldBe true
    }

    @Test
    fun testStart_shouldAddOfflineEventListener() {
        webConnectionWatchDog.start()

        window.dispatchEvent(Event("offline"))

        webConnectionWatchDog.isOnline.value shouldBe false
    }

    @Test
    fun testWatchDog_shouldSetIsOnlineToFalseThenToTrue_WhenConnection_GoesOffline_AndThen_Online() {
        webConnectionWatchDog.start()

        window.dispatchEvent(Event("offline"))

        webConnectionWatchDog.isOnline.value shouldBe false

        window.dispatchEvent(Event("online"))

        webConnectionWatchDog.isOnline.value shouldBe true
    }

    @Test
    fun testWatchDog_shouldSetIsOnlineToTrueThenToFalse_WhenConnection_GoesOnline_AndThen_Offline() {
        webConnectionWatchDog.start()

        window.dispatchEvent(Event("online"))

        webConnectionWatchDog.isOnline.value shouldBe true

        window.dispatchEvent(Event("offline"))

        webConnectionWatchDog.isOnline.value shouldBe false
    }

}