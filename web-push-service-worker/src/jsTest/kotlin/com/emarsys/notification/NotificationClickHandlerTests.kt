package com.emarsys.notification

import com.emarsys.window.FakeBrowserWindowHandler
import dev.mokkery.verify
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import web.broadcast.BroadcastChannel
import kotlin.test.BeforeTest
import kotlin.test.Test

class NotificationClickHandlerTests {

    companion object {
        private const val TEST_MESSAGE = "test-message"
    }

    private lateinit var testBroadcastChannel: BroadcastChannel
    private lateinit var fakeBrowserWindowHandler: FakeBrowserWindowHandler
    private lateinit var notificationClickHandler: NotificationClickHandler

    @BeforeTest
    fun setup() {
        fakeBrowserWindowHandler = FakeBrowserWindowHandler()
        testBroadcastChannel = BroadcastChannel("test-channel")
        notificationClickHandler =
            NotificationClickHandler(testBroadcastChannel, fakeBrowserWindowHandler)
    }

    @Test
    fun testPostStoredMessageToSDK_shouldSetStoredMessageToNull_whenMessageIsStored() {
        notificationClickHandler.storedNotificationClickedMessage = TEST_MESSAGE

        notificationClickHandler.postStoredMessageToSDK()

        verify {
            testBroadcastChannel.postMessage(TEST_MESSAGE)
        }
        notificationClickHandler.storedNotificationClickedMessage shouldBe null
    }

    @Test
    fun testHandleNotificationClick_shouldStoreMessage_andOpenWindow_whenWindowIsNotOpen() = runTest {
        notificationClickHandler.handleNotificationClick(TEST_MESSAGE)

        fakeBrowserWindowHandler.openWindowWasCalled shouldBe true
        notificationClickHandler.storedNotificationClickedMessage shouldBe TEST_MESSAGE
    }

    @Test
    fun testHandleNotificationClick_shouldNotOpenWindow_whenWindowIsAlreadyOpen() = runTest {
        fakeBrowserWindowHandler.shouldReturnWindowClient = true

        notificationClickHandler.handleNotificationClick(TEST_MESSAGE)

        fakeBrowserWindowHandler.openWindowWasCalled shouldBe false
        notificationClickHandler.storedNotificationClickedMessage shouldBe null
    }

}