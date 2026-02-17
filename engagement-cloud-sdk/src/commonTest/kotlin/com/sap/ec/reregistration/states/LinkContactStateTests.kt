package com.sap.ec.reregistration.states

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.channel.SdkEventWaiterApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.event.SdkEvent
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
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class LinkContactStateTests {

    companion object {
        private const val TEST_CONTACT_FIELD_VALUE = "testContactFieldValue"
        private const val TEST_OPEN_ID_TOKEN = "testOpenIdToken"
        val testException = Exception("failed")
        val successResponse = SdkEvent.Internal.Sdk.Answer.Response(
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
        val failedResponse = successResponse.copy(result = Result.failure(testException))
    }

    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var eventSlot: SlotCapture<SdkEvent>
    private lateinit var linkContactState: LinkContactState
    private lateinit var mockSdkEventWaiter: SdkEventWaiterApi

    @BeforeTest
    fun setUp() {
        eventSlot = slot()
        mockSdkEventWaiter = mock()
        mockSdkContext = mock(MockMode.autofill)
        mockSdkEventDistributor = mock(MockMode.autofill)
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mockSdkEventWaiter

        linkContactState =
            LinkContactState(
                mockSdkContext,
                mockSdkEventDistributor,
                sdkLogger = mock(MockMode.autofill)
            )
    }

    @Test
    fun active_shouldRegisterLinkContactEvent_throughSdkEventDistributor_whenContactFieldValue_areAvailable() =
        runTest {
            every { mockSdkContext.contactFieldValue } returns TEST_CONTACT_FIELD_VALUE
            everySuspend { mockSdkEventWaiter.await<Response>() } returns successResponse

            val result = linkContactState.active()

            result shouldBe Result.success(Unit)
            val registeredEvent = eventSlot.get()
            (registeredEvent is SdkEvent.Internal.Sdk.LinkContact) shouldBe true
            (registeredEvent as SdkEvent.Internal.Sdk.LinkContact).contactFieldValue shouldBe TEST_CONTACT_FIELD_VALUE
        }

    @Test
    fun active_shouldRegisterLinkAuthenticatedContactEvent_throughSdkEventDistributor_whenOpenIdToken_areAvailable() =
        runTest {
            every { mockSdkContext.contactFieldValue } returns null
            every { mockSdkContext.openIdToken } returns TEST_OPEN_ID_TOKEN
            everySuspend { mockSdkEventWaiter.await<Response>() } returns successResponse

            val result = linkContactState.active()

            result shouldBe Result.success(Unit)
            val registeredEvent = eventSlot.get()
            (registeredEvent is SdkEvent.Internal.Sdk.LinkAuthenticatedContact) shouldBe true
            (registeredEvent as SdkEvent.Internal.Sdk.LinkAuthenticatedContact).openIdToken shouldBe TEST_OPEN_ID_TOKEN
        }

    @Test
    fun active_shouldNotEmitEvent_whenNeitherContactFieldValue_norOpenIdToken_areAvailable() =
        runTest {
            every { mockSdkContext.contactFieldValue } returns null
            every { mockSdkContext.openIdToken } returns null

            val result = linkContactState.active()

            result shouldBe Result.success(Unit)
            verifySuspend(VerifyMode.exactly(0)) {
                mockSdkEventDistributor.registerEvent(any())
            }
        }

    @Test
    fun active_RegisterLinkAuthenticatedContactEvent_throughSdkEventDistributor_evenWhenContactFieldValue_isNotAvailable() =
        runTest {
            every { mockSdkContext.contactFieldValue } returns TEST_CONTACT_FIELD_VALUE
            every { mockSdkContext.openIdToken } returns null
            everySuspend { mockSdkEventWaiter.await<Response>() } returns successResponse

            val result = linkContactState.active()

            result shouldBe Result.success(Unit)
            val registeredEvent = eventSlot.get()
            (registeredEvent is SdkEvent.Internal.Sdk.LinkContact) shouldBe true
            (registeredEvent as SdkEvent.Internal.Sdk.LinkContact).contactFieldValue shouldBe TEST_CONTACT_FIELD_VALUE
        }

    @Test
    fun active_shouldReturnFailure_whenLinkContactEvent_returnsError() =
        runTest {
            every { mockSdkContext.contactFieldValue } returns TEST_CONTACT_FIELD_VALUE
            everySuspend { mockSdkEventWaiter.await<Response>() } returns failedResponse

            val result = linkContactState.active()

            result shouldBe Result.failure(testException)
            val registeredEvent = eventSlot.get()
            (registeredEvent is SdkEvent.Internal.Sdk.LinkContact) shouldBe true
            (registeredEvent as SdkEvent.Internal.Sdk.LinkContact).contactFieldValue shouldBe TEST_CONTACT_FIELD_VALUE
        }

    @Test
    fun active_shouldReturnFailure_whenLinkAuthenticatedContactEvent_returnsError() =
        runTest {
            every { mockSdkContext.contactFieldValue } returns null
            every { mockSdkContext.openIdToken } returns TEST_OPEN_ID_TOKEN
            everySuspend { mockSdkEventWaiter.await<Response>() } returns failedResponse

            val result = linkContactState.active()

            result shouldBe Result.failure(testException)
            val registeredEvent = eventSlot.get()
            (registeredEvent is SdkEvent.Internal.Sdk.LinkAuthenticatedContact) shouldBe true
            (registeredEvent as SdkEvent.Internal.Sdk.LinkAuthenticatedContact).openIdToken shouldBe TEST_OPEN_ID_TOKEN
        }

}