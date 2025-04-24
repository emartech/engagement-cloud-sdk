package com.emarsys.enable.states

import com.emarsys.api.push.PushConstants
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterPushTokenStateTests {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
        const val LAST_SENT_PUSH_TOKEN = "testLastSentPushToken"
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var registerPushTokenState: RegisterPushTokenState
    private lateinit var eventSlot: SlotCapture<SdkEvent>

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        eventSlot = slot()
        mockSdkEventDistributor = mock()
        mockStringStorage = mock()

        registerPushTokenState = RegisterPushTokenState(mockStringStorage, mockSdkEventDistributor)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testActive_whenLastSentPushTokenIsMissing_pushTokenIsAvailable() = runTest {
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(MockMode.autofill)
        every { mockStringStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        every { mockStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns null
        every {
            mockStringStorage.put(
                PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                PUSH_TOKEN
            )
        } returns Unit


        registerPushTokenState.active()

        verifySuspend {
            mockStringStorage.put(
                PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                PUSH_TOKEN
            )
        }
        val emitted = eventSlot.get()
        (emitted is SdkEvent.Internal.Sdk.RegisterPushToken) shouldBe true
        emitted.attributes?.get(PushConstants.PUSH_TOKEN_KEY)?.jsonPrimitive?.content shouldBe PUSH_TOKEN
    }

    @Test
    fun testActive_whenBothAvailable_butNotTheSame() = runTest {
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mock(MockMode.autofill)
        every { mockStringStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        every { mockStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns LAST_SENT_PUSH_TOKEN
        every { mockStringStorage.put(any(), any()) } returns Unit


        registerPushTokenState.active()

        verifySuspend {
            mockStringStorage.put(
                PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                PUSH_TOKEN
            )
        }
        val emitted = eventSlot.get()
        (emitted is SdkEvent.Internal.Sdk.RegisterPushToken) shouldBe true
        emitted.attributes?.get(PushConstants.PUSH_TOKEN_KEY)?.jsonPrimitive?.content shouldBe PUSH_TOKEN
    }

    @Test
    fun testActive_whenBothAvailable_andTheSame() = runTest {
        everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mock(MockMode.autofill)
        every { mockStringStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        every { mockStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
        every { mockStringStorage.put(any(), any()) } returns Unit

        registerPushTokenState.active()

        verifySuspend(VerifyMode.exactly(0)) { mockSdkEventDistributor.registerEvent(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockStringStorage.put(any(), any()) }
    }
}
