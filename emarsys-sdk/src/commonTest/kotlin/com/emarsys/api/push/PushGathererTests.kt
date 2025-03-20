package com.emarsys.api.push

import com.emarsys.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.emarsys.core.storage.StringStorageApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushGathererTests {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
        val registerPushToken = PushCall.RegisterPushToken(PUSH_TOKEN)
        val clearPushToken = PushCall.ClearPushToken()

        val expected = mutableListOf(
            registerPushToken,
            clearPushToken
        )
    }

    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var pushContext: PushContext
    private lateinit var pushGatherer: PushGatherer

    @BeforeTest
    fun setUp() {
        mockStringStorage = mock()
        pushContext = PushContext(expected)
        pushGatherer = PushGatherer(pushContext, mockStringStorage)
    }

    @Test
    fun testGathering() = runTest {
        pushGatherer.registerPushToken(PUSH_TOKEN)
        pushGatherer.clearPushToken()

        pushContext.calls shouldBe expected
    }

    @Test
    fun testPushToken() = runTest {
        every { mockStringStorage.get(PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN

        pushGatherer.getPushToken() shouldBe PUSH_TOKEN
    }

}