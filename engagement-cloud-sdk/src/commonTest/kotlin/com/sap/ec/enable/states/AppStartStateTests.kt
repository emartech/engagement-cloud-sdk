package com.sap.ec.enable.states

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.channel.SdkEventWaiterApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.event.SdkEvent
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.every
import dev.mokkery.everySuspend
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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AppStartStateTests {
    private companion object {
        val timestamp = Clock.System.now()
        const val UUID = "testUUID"
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mockUuidProvider: UuidProviderApi
    private lateinit var appStartState: AppStartState
    private lateinit var mockSdkEventWaiter: SdkEventWaiterApi

    @BeforeTest
    fun setUp() {
        mockSdkEventDistributor = mock()
        mockTimestampProvider = mock()
        mockSdkEventWaiter = mock()
        mockUuidProvider = mock()
        every { mockTimestampProvider.provide() } returns timestamp
        every { mockUuidProvider.provide() } returns UUID

        appStartState =
            AppStartState(mockSdkEventDistributor, mockTimestampProvider, mockUuidProvider)
    }

    @Test
    fun testActivate_should_send_appStartEvent_with_eventClient_when_it_was_not_completed_yet_andReturn_Success() =
        runTest {
            val response = SdkEvent.Internal.Sdk.Answer.Response(
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
            everySuspend { mockSdkEventWaiter.await<Response>() } returns response
            val expectedEvent = SdkEvent.Internal.Sdk.AppStart(id = UUID, timestamp = timestamp)
            everySuspend { mockSdkEventDistributor.registerEvent(expectedEvent) } returns mockSdkEventWaiter

            val result = appStartState.active()

            everySuspend {
                mockSdkEventDistributor.registerEvent(expectedEvent)
            }

            result shouldBe Result.success(Unit)
        }

    @Test
    fun testActivate_should_send_appStartEvent_with_eventClient_when_it_was_not_completed_yet_andReturn_Failure() =
        runTest {
            val testException = Exception("test exception")
            val response = SdkEvent.Internal.Sdk.Answer.Response<Response>(
                "0",
                Result.failure(testException)
            )
            everySuspend { mockSdkEventWaiter.await<Response>() } returns response
            val expectedEvent = SdkEvent.Internal.Sdk.AppStart(id = UUID, timestamp = timestamp)
            everySuspend { mockSdkEventDistributor.registerEvent(expectedEvent) } returns mockSdkEventWaiter

            val result = appStartState.active()

            everySuspend {
                mockSdkEventDistributor.registerEvent(expectedEvent)
            }

            result shouldBe Result.failure(testException)
        }

    @Test
    fun testActivate_should_not_send_appStartEvent_with_eventClient_when_it_was_already_completed() =
        runTest {
            val expectedEvent = SdkEvent.Internal.Sdk.AppStart(id = UUID, timestamp = timestamp)
            val response = SdkEvent.Internal.Sdk.Answer.Response(
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
            everySuspend { mockSdkEventWaiter.await<Response>() } returns response
            everySuspend { mockSdkEventDistributor.registerEvent(expectedEvent) } returns mockSdkEventWaiter

            val firstResult = appStartState.active()
            val secondResult = appStartState.active()

            verifySuspend(VerifyMode.exactly(1)) {
                mockSdkEventDistributor.registerEvent(expectedEvent)
            }

            firstResult shouldBe Result.success(Unit)
            secondResult shouldBe Result.success(Unit)
        }

    @Test
    fun testActivate_should_send_theSecond_appStartEvent_with_eventClient_when_the_firstAppStartEventRegistration_fails() =
        runTest {
            val testException = Exception("failed")
            val expectedEvent = SdkEvent.Internal.Sdk.AppStart(id = UUID, timestamp = timestamp)
            val response = SdkEvent.Internal.Sdk.Answer.Response(
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
            val failedResponse = response.copy(result = Result.failure(testException))
            everySuspend { mockSdkEventWaiter.await<Response>() } sequentiallyReturns listOf(
                failedResponse, response
            )
            everySuspend { mockSdkEventDistributor.registerEvent(expectedEvent) } returns mockSdkEventWaiter

            val firstResult = appStartState.active()
            val secondResult = appStartState.active()

            verifySuspend(VerifyMode.exactly(2)) {
                mockSdkEventDistributor.registerEvent(expectedEvent)
            }

            firstResult shouldBe Result.failure(testException)
            secondResult shouldBe Result.success(Unit)
        }
}