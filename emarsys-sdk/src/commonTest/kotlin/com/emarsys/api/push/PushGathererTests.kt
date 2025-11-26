package com.emarsys.api.push

import com.emarsys.TestEmarsysConfig
import com.emarsys.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.emarsys.context.SdkContextApi
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
        const val APPLICATION_CODE = "testAppCode"
        val registerPushToken = PushCall.RegisterPushToken(PUSH_TOKEN)
        val clearPushToken = PushCall.ClearPushToken(applicationCode = APPLICATION_CODE)

        val expected = mutableListOf(
            registerPushToken,
            clearPushToken
        )
    }

    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var pushContext: PushContextApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var pushGatherer: PushInstance

    @BeforeTest
    fun setUp() {
        mockStringStorage = mock()
        mockSdkContext = mock()
        every { mockSdkContext.config } returns TestEmarsysConfig(APPLICATION_CODE)
        pushContext = PushContext(mutableListOf())
        pushGatherer = PushGatherer(pushContext, mockStringStorage, mockSdkContext)
    }

    @Test
    fun testGathering() = runTest {
        pushGatherer.registerPushToken(PUSH_TOKEN)
        pushGatherer.clearPushToken()

        pushContext.calls shouldBe expected
        pushContext.calls.size shouldBe 2
    }

    @Test
    fun testPushToken() = runTest {
        every { mockStringStorage.get(PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN

        pushGatherer.getPushToken() shouldBe PUSH_TOKEN
    }

}