package com.emarsys.core.message

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class MsgHubTests {

    private val testMsgBox = object : MsgBox<String> {
        override val id: String = "id"
        override val replayCount: Int = 0
    }

    private val testMsg = object : Msg<String> {
        override val content: String = "content"
    }

    private lateinit var msgHub: MsgHub

    @BeforeTest
    fun setUp() = runTest {
        msgHub = MsgHub()
    }

    @Test
    fun testOpen_shouldThrowException_whenMsgBoxHasAlreadyOpened() = runTest {
        msgHub.open(testMsgBox)

        shouldThrow<IllegalArgumentException> {
            msgHub.open(testMsgBox)
        }
    }

    @Test
    fun testClose_shouldThrowException_whenMsgBoxHasNotAlreadyOpened() = runTest {
        shouldThrow<IllegalArgumentException> {
            msgHub.close(testMsgBox)
        }
    }

    @Test
    fun testSend_shouldThrowException_whenMsgBoxHasNotAlreadyOpened() = runTest {
        shouldThrow<IllegalArgumentException> {
            msgHub.send(testMsg, testMsgBox)
        }
    }

    @Test
    fun testSend_msgShouldBeDelivered() = runTest {
        var result: Msg<String>? = null
        val waiter = CompletableDeferred<Unit>()

        msgHub.open(testMsgBox)

        msgHub.enrollFor(testMsgBox) {
            result = it
            waiter.complete(Unit)
        }

        msgHub.send(testMsg, testMsgBox)

        waiter.await()

        result shouldBe testMsg
    }

    @Test
    fun testSend_msgShouldBeDeliveredForAll() = runTest {
        var result1: Msg<String>? = null
        var result2: Msg<String>? = null
        var result3: Msg<String>? = null
        val waiter1 = CompletableDeferred<Unit>()
        val waiter2 = CompletableDeferred<Unit>()
        val waiter3 = CompletableDeferred<Unit>()

        msgHub.open(testMsgBox)

        msgHub.enrollFor(testMsgBox) {
            result1 = it
            waiter1.complete(Unit)
        }
        msgHub.enrollFor(testMsgBox) {
            result2 = it
            waiter2.complete(Unit)
        }
        msgHub.enrollFor(testMsgBox) {
            result3 = it
            waiter3.complete(Unit)
        }

        msgHub.send(testMsg, testMsgBox)

        waiter1.await()
        waiter2.await()
        waiter3.await()

        result1 shouldBe testMsg
        result2 shouldBe testMsg
        result3 shouldBe testMsg
    }

    @Test
    fun testEnrollFor_shouldDeliverPreviousMessages_whenMsgBoxReplayCountBiggerThanZero() = runTest {
        val testMsgBoxWithReplayCount = object : MsgBox<String> {
            override val id: String = "id"
            override val replayCount: Int = 3
        }

        val testMsg1 = object : Msg<String> {
            override val content = "content1"
        }
        val testMsg2 = object : Msg<String> {
            override val content = "content2"
        }
        val testMsg3 = object : Msg<String> {
            override val content = "content3"
        }
        val testMsg4 = object : Msg<String> {
            override val content = "content3"
        }
        val testMsg5 = object : Msg<String> {
            override val content = "content3"
        }

        var result1: Msg<String>? = null
        var result2: Msg<String>? = null
        var result3: Msg<String>? = null

        val waiter = CompletableDeferred<Unit>()

        msgHub.open(testMsgBoxWithReplayCount)

        msgHub.send(testMsg1, testMsgBoxWithReplayCount)
        msgHub.send(testMsg2, testMsgBoxWithReplayCount)
        msgHub.send(testMsg3, testMsgBoxWithReplayCount)
        msgHub.send(testMsg4, testMsgBoxWithReplayCount)
        msgHub.send(testMsg5, testMsgBoxWithReplayCount)

        var counter = 1
        msgHub.enrollFor(testMsgBoxWithReplayCount) {
            when (counter) {
                1 -> result1 = it
                2 -> result2 = it
                3 -> {
                    result3 = it
                    waiter.complete(Unit)
                }
            }
            counter++
        }

        waiter.await()

        result1 shouldBe testMsg3
        result2 shouldBe testMsg4
        result3 shouldBe testMsg5
    }

}