package com.emarsys.enable.states

import com.emarsys.api.push.PushConstants
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.channel.SdkEventWaiterApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
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
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterPushTokenStateTests {
    private companion object {
        const val PUSH_TOKEN = "testPushToken"
        const val LAST_SENT_PUSH_TOKEN = "testLastSentPushToken"
        val testException = Exception("test exception")
        val successResult = SdkEvent.Internal.Sdk.Answer.Response(
            "0",
            Result.success(
                Response(
                    originalRequest = UrlRequest(Url("https://example.com"), HttpMethod.Post),
                    status = HttpStatusCode.OK,
                    headers = headersOf(),
                    bodyAsText = "testBody"
                )
            )
        )
        val failureResult = successResult.copy(result = Result.failure(testException))
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var registerPushTokenState: RegisterPushTokenState
    private lateinit var eventSlot: SlotCapture<SdkEvent>

    private lateinit var mockWaiter: SdkEventWaiterApi

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockWaiter = mock()
        everySuspend { mockWaiter.await<Response>() } returns successResult
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
    fun active_shouldStorePushToken_andCallRegisterEvent_withRegisterPushToken_whenLastSentPushTokenIsMissing_pushTokenIsAvailable() =
        runTest {
            everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mockWaiter
            every { mockStringStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
            every { mockStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns null
            every {
                mockStringStorage.put(
                    PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                    PUSH_TOKEN
                )
            } returns Unit


            val result = registerPushTokenState.active()

            result shouldBe Result.success(Unit)
            verifySuspend {
                mockStringStorage.put(
                    PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                    PUSH_TOKEN
                )
            }
            val emitted = eventSlot.get()
            (emitted is SdkEvent.Internal.Sdk.RegisterPushToken) shouldBe true
            (emitted as SdkEvent.Internal.Sdk.RegisterPushToken).pushToken shouldBe PUSH_TOKEN
        }

    @Test
    fun active_shouldStorePushToken_andCallRegisterEvent_withRegisterPushToken_whenBothAvailable_butNotTheSame() =
        runTest {
            everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mockWaiter
            every { mockStringStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
            every { mockStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns LAST_SENT_PUSH_TOKEN
            every { mockStringStorage.put(any(), any()) } returns Unit


            val result = registerPushTokenState.active()

            result shouldBe Result.success(Unit)
            verifySuspend {
                mockStringStorage.put(
                    PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                    PUSH_TOKEN
                )
            }
            val emitted = eventSlot.get()
            (emitted is SdkEvent.Internal.Sdk.RegisterPushToken) shouldBe true
            (emitted as SdkEvent.Internal.Sdk.RegisterPushToken).pushToken shouldBe PUSH_TOKEN
        }

    @Test
    fun testActive_shouldNotStorePushToken_andNotCallRegisterEvent_withRegisterPushToken_whenBothAvailable_andTheSame() =
        runTest {
            everySuspend { mockSdkEventDistributor.registerEvent(any()) } returns mock(MockMode.autofill)
            every { mockStringStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
            every { mockStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
            every { mockStringStorage.put(any(), any()) } returns Unit

            registerPushTokenState.active() shouldBe Result.success(Unit)

            verifySuspend(VerifyMode.exactly(0)) { mockSdkEventDistributor.registerEvent(any()) }
            verifySuspend(VerifyMode.exactly(0)) { mockStringStorage.put(any(), any()) }
        }

    @Test
    fun active_shouldReturnFailure_when_registerPushTokenFails_andShouldNotStoreToken() =
        runTest {
            everySuspend { mockWaiter.await<Response>() } returns failureResult
            everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mockWaiter
            every { mockStringStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
            every { mockStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns null
            every {
                mockStringStorage.put(
                    PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                    PUSH_TOKEN
                )
            } returns Unit


            val result = registerPushTokenState.active()

            result shouldBe Result.failure(testException)
            verifySuspend(VerifyMode.exactly(0)) {
                mockStringStorage.put(
                    PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                    PUSH_TOKEN
                )
            }
            val emitted = eventSlot.get()
            (emitted is SdkEvent.Internal.Sdk.RegisterPushToken) shouldBe true
            (emitted as SdkEvent.Internal.Sdk.RegisterPushToken).pushToken shouldBe PUSH_TOKEN
        }

    @Test
    fun active_shouldReturnFailure_when_storingFails() =
        runTest {
            everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mockWaiter
            every { mockStringStorage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN
            every { mockStringStorage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns null
            every {
                mockStringStorage.put(
                    PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                    PUSH_TOKEN
                )
            } throws testException


            val result = registerPushTokenState.active()

            result shouldBe Result.failure(testException)
            verifySuspend {
                mockStringStorage.put(
                    PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY,
                    PUSH_TOKEN
                )
            }
            val emitted = eventSlot.get()
            (emitted is SdkEvent.Internal.Sdk.RegisterPushToken) shouldBe true
            (emitted as SdkEvent.Internal.Sdk.RegisterPushToken).pushToken shouldBe PUSH_TOKEN
        }
}
