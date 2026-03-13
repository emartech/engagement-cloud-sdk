package com.sap.ec.mobileengage.push

import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.api.push.PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY
import com.sap.ec.api.push.PushContext
import com.sap.ec.api.push.PushContextApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StringStorageApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class JsPushInternalTests {

    private companion object {
        const val APPLICATION_CODE = "testAppCode"
    }

    private lateinit var mockStorage: StringStorageApi
    private lateinit var mockPushContext: PushContextApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockLogger: Logger
    private lateinit var mockPushService: PushServiceApi
    private lateinit var jsPushInternal: JsPushInternal

    @BeforeTest
    fun setup() {
        mockStorage = mock(MockMode.autofill)
        mockPushContext = PushContext(mutableListOf())
        mockSdkContext = mock()
        everySuspend { mockSdkContext.getSdkConfig() } returns TestEngagementCloudSDKConfig(APPLICATION_CODE)
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockLogger = mock(MockMode.autofill)
        mockPushService = mock(MockMode.autofill)
        jsPushInternal = JsPushInternal(
            mockStorage,
            mockPushContext,
            mockSdkContext,
            mockSdkEventDistributor,
            mockLogger,
            mockPushService
        )
    }

    @Test
    fun unsubscribe_shouldClearPushToken_andUnsubscribeFromPushService() = runTest {
        everySuspend { mockStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, null) } returns Unit
        everySuspend { mockPushService.unsubscribe() } returns Result.success(Unit)

        val result = jsPushInternal.unsubscribe()

        result.isSuccess shouldBe true
        verifySuspend { mockSdkEventDistributor.registerEvent(any()) }
        verifySuspend { mockStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, null) }
        verifySuspend { mockPushService.unsubscribe() }
    }

    @Test
    fun unsubscribe_shouldReturnFailure_whenPushServiceUnsubscribeFails() = runTest {
        everySuspend { mockStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, null) } returns Unit
        everySuspend { mockPushService.unsubscribe() } returns Result.failure(
            RuntimeException("test error")
        )

        val result = jsPushInternal.unsubscribe()

        result.isFailure shouldBe true
    }
}
