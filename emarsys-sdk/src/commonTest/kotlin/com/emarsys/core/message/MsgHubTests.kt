package com.emarsys.core.message

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
class MsgHubTests {

    private lateinit var msgHub: MsgHub

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @BeforeTest
    fun setUp() = runTest {
        msgHub = MsgHub(StandardTestDispatcher())
    }

    @Test
    fun testSend_msgShouldBeDeliveredForAll() = runTest {
        var counter = 0
        val waiter = CompletableDeferred<Unit>()
        msgHub.subscribe("testTopic") {
            counter += 1
            if (counter == 2) {
                waiter.complete(Unit)
            }
        }
        msgHub.subscribe("testTopic2") {
            fail("Message should not be delivered to this topic")
        }
        msgHub.subscribe("testTopic") {
            counter += 1
            if (counter == 2) {
                waiter.complete(Unit)
            }
        }

        launch {
            msgHub.send("testMsg", "testTopic")
        }

        advanceUntilIdle()
        waiter.await()

        counter shouldBe 2
    }

}
