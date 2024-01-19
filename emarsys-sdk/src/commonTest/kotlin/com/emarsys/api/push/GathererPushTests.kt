package com.emarsys.api.push

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererPushTests {
    companion object {
        val pushToken = "testPushToken"
    }

    private lateinit var pushContext: PushContext
    private lateinit var pushGatherer: PushGatherer

    @BeforeTest
    fun setup() {
        pushContext = PushContext()
        pushGatherer = PushGatherer(pushContext)
    }

    @Test
    fun testGathering() = runTest {
        val setPushToken = PushCall.SetPushToken(pushToken)
        val clearPushToken = PushCall.ClearPushToken()

        val expected = listOf(
            setPushToken,
            clearPushToken
        )

        pushGatherer.registerPushToken(pushToken)
        pushGatherer.clearPushToken()

        pushContext.calls shouldBe expected
    }

}