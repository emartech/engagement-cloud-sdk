package com.emarsys.disable.states

import com.emarsys.TestEmarsysConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.channel.SdkEventWaiterApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test


@OptIn(ExperimentalCoroutinesApi::class)
class ClearPushTokenOnDisableStateTests {
    private companion object {
        const val TEST_APPLICATION_CODE = "testAppCode"
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
        val testException = Exception("testExceptionMessage")
        val failureResult =
            successResult.copy(result = Result.failure(testException))
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockWaiter: SdkEventWaiterApi
    private lateinit var slot: SlotCapture<SdkEvent>
    private lateinit var clearPushTokenOnDisableState: ClearPushTokenOnDisableState

    @BeforeTest
    fun setup() {
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkContext = mock()
        every { mockSdkContext.config } returns TestEmarsysConfig(TEST_APPLICATION_CODE)
        mockWaiter = mock(MockMode.autofill)
        slot = slot()
        everySuspend { mockWaiter.await<Response>() } returns successResult
        clearPushTokenOnDisableState = ClearPushTokenOnDisableState(mockSdkEventDistributor, mockSdkContext)
    }

    @Test
    fun active_shouldRegisterClearPushTokenEvent_without_deletingPushTokenFromStorage_onSuccess() =
        runTest {
            everySuspend { mockSdkEventDistributor.registerEvent(capture(slot)) } returns mockWaiter

            val result = clearPushTokenOnDisableState.active()

            result shouldBe Result.success(Unit)
            val emittedEvent = slot.get()
            (emittedEvent is SdkEvent.Internal.Sdk.ClearPushToken) shouldBe true
            (emittedEvent as SdkEvent.Internal.Sdk.ClearPushToken).applicationCode shouldBe TEST_APPLICATION_CODE
        }

    @Test
    fun active_shouldRegisterClearPushTokenEvent_without_deletingPushTokenFromStorage_onFailure() =
        runTest {
            everySuspend { mockSdkEventDistributor.registerEvent(capture(slot)) } returns mockWaiter
            everySuspend { mockWaiter.await<Response>() } returns failureResult

            val result = clearPushTokenOnDisableState.active()

            val emittedEvent = slot.get()
            (emittedEvent is SdkEvent.Internal.Sdk.ClearPushToken) shouldBe true
            result shouldBe Result.failure(testException)
        }
}