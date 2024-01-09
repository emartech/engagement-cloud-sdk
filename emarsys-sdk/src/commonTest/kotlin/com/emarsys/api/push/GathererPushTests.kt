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
    private lateinit var gathererPush: GathererPush

    @BeforeTest
    fun setup() {
        pushContext = PushContext()
        gathererPush = GathererPush(pushContext)
    }

    @Test
    fun testGathering() = runTest {
        val setPushToken = PushCall.SetPushToken(pushToken)
        val clearPushToken = PushCall.ClearPushToken()

        val expected = listOf(
            setPushToken,
            clearPushToken
        )

        gathererPush.setPushToken(pushToken)
        gathererPush.clearPushToken()

        pushContext.calls shouldBe expected
    }

}