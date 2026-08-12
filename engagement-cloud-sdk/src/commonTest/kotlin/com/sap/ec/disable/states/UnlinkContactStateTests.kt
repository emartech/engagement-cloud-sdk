package com.sap.ec.disable.states

import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.channel.SdkEventWaiterApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.context.RequestContextApi
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
import dev.mokkery.matcher.capture.isAbsent
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

class UnlinkContactStateTests {
    private companion object {
        const val APPLICATION_CODE = "testAppCode"
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

    private lateinit var mockLogger: Logger
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockRequestContext: RequestContextApi
    private lateinit var unlinkContactState: UnlinkContactState
    private lateinit var mockSdkEventWaiter: SdkEventWaiterApi
    private lateinit var eventSlot: SlotCapture<SdkEvent>

    @BeforeTest
    fun setup() {
        mockLogger = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        everySuspend { mockSdkContext.getSdkConfig() } returns TestEngagementCloudSDKConfig(
            APPLICATION_CODE
        )
        mockRequestContext = mock(MockMode.autofill)
        mockSdkEventDistributor = mock(MockMode.autofill)
        eventSlot = slot()
        everySuspend { mockSdkEventDistributor.registerEvent(capture(eventSlot)) } returns mockSdkEventWaiter
        unlinkContactState = UnlinkContactState(
            mockSdkEventDistributor,
            mockRequestContext,
            mockSdkContext,
            mockLogger
        )
    }

    @Test
    fun activate_shouldRegister_unlinkContactEvent_andReturnSuccess_ifIsContactLinked_isTrue() =
        runTest {
            everySuspend { mockSdkEventWaiter.await<Response>() } returns successResponse
            every { mockRequestContext.isContactLinked } returns true

            val result = unlinkContactState.active()

            (eventSlot.get() is SdkEvent.Internal.Sdk.UnlinkContact) shouldBe true

            result shouldBe Result.success(Unit)
        }

    @Test
    fun activate_shouldNotRegister_unlinkContactEvent_andReturnSuccess_ifIsContactLinked_isFalse() =
        runTest {
            every { mockRequestContext.isContactLinked } returns false

            val result = unlinkContactState.active()

            verifySuspend(VerifyMode.exactly(0)) { mockSdkEventDistributor.registerEvent(any()) }
            eventSlot.isAbsent shouldBe true

            result shouldBe Result.success(Unit)
        }

    @Test
    fun activate_shouldRegister_unlinkContactEvent_andReturnFailure_ifErrorHappened() = runTest {
        every { mockRequestContext.isContactLinked } returns true
        everySuspend { mockSdkEventWaiter.await<Response>() } returns failedResponse

        val result = unlinkContactState.active()

        (eventSlot.get() is SdkEvent.Internal.Sdk.UnlinkContact) shouldBe true

        result.isFailure shouldBe true
        result.exceptionOrNull() shouldBe testException
    }
}