package com.emarsys.networking.clients.error

import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.SdkException.FailedRequestException
import com.emarsys.core.exceptions.SdkException.MissingApplicationCodeException
import com.emarsys.core.exceptions.SdkException.RetryLimitReachedException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class DefaultClientExceptionHandlerTests {
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var exceptionHandler: ClientExceptionHandler

    private companion object {
        val RESPONSE = Response(
            UrlRequest(
                Url("testUrl"),
                HttpMethod.Post
            ),
            HttpStatusCode.Unauthorized, Headers.Empty, bodyAsText = "testBody"
        )
    }

    @BeforeTest
    fun setUp() {
        mockEventsDao = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)

        exceptionHandler = DefaultClientExceptionHandler(mockEventsDao, mockSdkLogger)
    }

    @Test
    fun testHandleException_shouldAckEvent_onKnownError() = forAll(
        table(
            headers("exception"),
            listOf(
                row(FailedRequestException(RESPONSE)),
                row(RetryLimitReachedException(
                    "Retry limit reached",
                    RESPONSE)
                ),
                row(MissingApplicationCodeException("Missing app code")),
            )
        )
    ) { testException ->
        runTest {
            val trackDeepLinkEvent = SdkEvent.Internal.Sdk.TrackDeepLink(
                id = "testId",
                trackingId = "testTrackingId",
                nackCount = 0
            )

            exceptionHandler.handleException(
                throwable = testException,
                errorMessage = "Test error message",
                trackDeepLinkEvent
            )
            advanceUntilIdle()

            verifySuspend { mockEventsDao.removeEvent(trackDeepLinkEvent) }
        }
    }

    @Test
    fun testHandleException_shouldNackEvent_onUnknownException() = runTest {
        val trackDeepLinkEvent = SdkEvent.Internal.Sdk.TrackDeepLink(
            id = "testId",
            trackingId = "testTrackingId",
            nackCount = 1
        )
        val expectedErrorMessage = "Test error message"

        val testException = Exception("Unknown error")

        exceptionHandler.handleException(
            throwable = testException,
            errorMessage = expectedErrorMessage,
            trackDeepLinkEvent
        )
        advanceUntilIdle()

        verifySuspend { mockEventsDao.upsertEvent(trackDeepLinkEvent) }
        verifySuspend { mockSdkLogger.error(expectedErrorMessage, testException) }
    }

    @Test
    fun testHandleException_shouldNackEvent_onUnknownException_whenCalledWithMultipleEvents() =
        runTest {
            val trackDeepLinkEvent = SdkEvent.Internal.Sdk.TrackDeepLink(
                id = "testId",
                trackingId = "testTrackingId",
                nackCount = 1
            )
            val trackDeepLinkEvent2 = SdkEvent.Internal.Sdk.TrackDeepLink(
                id = "testId2",
                trackingId = "testTrackingId2",
                nackCount = 1
            )
            val expectedErrorMessage = "Test error message"

            val testException = Exception("Unknown error")

            exceptionHandler.handleException(
                throwable = testException,
                errorMessage = expectedErrorMessage,
                *arrayOf(trackDeepLinkEvent, trackDeepLinkEvent2)
            )
            advanceUntilIdle()

            verifySuspend { mockEventsDao.upsertEvent(trackDeepLinkEvent) }
            verifySuspend { mockEventsDao.upsertEvent(trackDeepLinkEvent2) }
            verifySuspend { mockSdkLogger.error(expectedErrorMessage, testException) }
        }
}