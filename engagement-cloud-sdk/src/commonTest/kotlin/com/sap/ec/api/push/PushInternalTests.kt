package com.sap.ec.api.push

import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.api.push.PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY
import com.sap.ec.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.matcher.capture.isAbsent
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import dev.mokkery.resetCalls
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushInternalTests {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
        const val APPLICATION_CODE = "testAppCode"
        val registerPushToken = PushCall.RegisterPushToken(PUSH_TOKEN)
        val clearPushToken = PushCall.ClearPushToken(APPLICATION_CODE)
        val expectedCalls = mutableListOf(
            registerPushToken,
            clearPushToken
        )
    }

    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var eventSlot: SlotCapture<SdkEvent>
    private lateinit var mockLogger: Logger
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var pushContext: PushContextApi
    private lateinit var pushInternal: PushInternal

    @BeforeTest
    fun setup() {
        mockStringStorage = mock()
        mockSdkContext = mock()
        every { mockSdkContext.config } returns TestEngagementCloudSDKConfig(APPLICATION_CODE)
        eventSlot = slot()
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockLogger = mock(MockMode.autofill)
        pushContext = PushContext(mutableListOf())
        pushInternal = PushInternal(
            mockStringStorage,
            pushContext,
            mockSdkEventDistributor,
            mockSdkContext,
            mockLogger
        )
    }

    @AfterTest
    fun teardown() {
        pushContext.calls.clear()
        resetCalls()
        resetAnswers()
    }

    @Test
    fun testRegisterPushToken_shouldNotDoAnything_whenPushTokenStoredAlready() = runTest {
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(
            MockMode.autofill
        )
        everySuspend { mockStringStorage.get(PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        everySuspend { mockStringStorage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        everySuspend { mockStringStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
        everySuspend {
            mockStringStorage.put(
                LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                PUSH_TOKEN
            )
        } returns Unit

        pushInternal.registerPushToken(PUSH_TOKEN)

        eventSlot.isAbsent shouldBe true
    }

    @Test
    fun testRegisterPushToken_shouldRegisterPushToken_whenPushTokenWasNotStoredPreviously() =
        runTest {
            everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(
                MockMode.autofill
            )
            everySuspend { mockStringStorage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns null
            everySuspend { mockStringStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
            everySuspend {
                mockStringStorage.put(
                    LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                    PUSH_TOKEN
                )
            } returns Unit

            pushInternal.registerPushToken(PUSH_TOKEN)

            verifySuspend(VerifyMode.order) {
                mockStringStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN)
                mockStringStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN)
            }
            val emitted = eventSlot.get()
            (emitted is SdkEvent.Internal.Sdk.RegisterPushToken) shouldBe true
            (emitted as SdkEvent.Internal.Sdk.RegisterPushToken).pushToken shouldBe PUSH_TOKEN
        }

    @Test
    fun testRegisterPushToken_shouldRegisterPushToken_whenStoredPushTokenIsDifferent() = runTest {
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(
            MockMode.autofill
        )
        everySuspend { mockStringStorage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns "<differentTestPushToken>"
        everySuspend { mockStringStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
        everySuspend {
            mockStringStorage.put(
                LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                PUSH_TOKEN
            )
        } returns Unit

        pushInternal.registerPushToken(PUSH_TOKEN)

        verifySuspend(VerifyMode.order) {
            mockStringStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN)
            mockStringStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN)
        }
        val emitted = eventSlot.get()
        (emitted is SdkEvent.Internal.Sdk.RegisterPushToken) shouldBe true
        (emitted as SdkEvent.Internal.Sdk.RegisterPushToken).pushToken shouldBe PUSH_TOKEN
    }

    @Test
    fun testClearPushToken_shouldRegisterEventOnSdkDistributor_andClear_lastSentPushToken_fromStorage() =
        runTest {
            everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(
                MockMode.autofill
            )
            everySuspend {
                mockStringStorage.put(
                    LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                    null
                )
            } returns Unit

            pushInternal.clearPushToken()

            verifySuspend { mockStringStorage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, null) }
            val emitted = eventSlot.get()
            (emitted is SdkEvent.Internal.Sdk.ClearPushToken) shouldBe true
            (emitted as SdkEvent.Internal.Sdk.ClearPushToken).applicationCode shouldBe APPLICATION_CODE
        }

    @Test
    fun testRegisterPushToken_shouldRegisterTheToken_ifNoStoredTokens_areFound() = runTest {
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mock(MockMode.autofill)
        everySuspend { mockStringStorage.get(PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        everySuspend { mockStringStorage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns null
        everySuspend { mockStringStorage.put(PUSH_TOKEN_STORAGE_KEY, PUSH_TOKEN) } returns Unit
        everySuspend {
            mockStringStorage.put(
                LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                PUSH_TOKEN
            )
        } returns Unit

        pushInternal.registerPushToken(PUSH_TOKEN)
        verifySuspend { mockSdkEventDistributor.registerEvent(any()) }
        pushInternal.getPushToken() shouldBe PUSH_TOKEN
    }

    @Test
    fun testActivate_should_sendCalls_toPushClient() = runTest {
        val eventContainer = Capture.container<OnlineSdkEvent>()
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventContainer)) } returns mock(
            MockMode.autofill
        )

        pushContext.calls.addAll(expectedCalls)

        pushInternal.activate()

        val emittedValues = eventContainer.values
        emittedValues.first { it is SdkEvent.Internal.Sdk.RegisterPushToken }.apply {
            (this as SdkEvent.Internal.Sdk.RegisterPushToken).pushToken shouldBe PUSH_TOKEN
        }
        emittedValues.firstOrNull { it is SdkEvent.Internal.Sdk.ClearPushToken }.apply {
            this shouldNotBe null
            (this as SdkEvent.Internal.Sdk.ClearPushToken).applicationCode shouldBe APPLICATION_CODE
        }
    }
}
