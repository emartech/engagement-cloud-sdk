package com.emarsys.integration

import com.emarsys.AndroidEmarsys
import com.emarsys.AndroidEmarsysConfig
import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.context.copyWith
import com.emarsys.core.channel.SdkEventDistributor
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.exceptions.SdkException
import com.emarsys.core.exceptions.SdkException.RetryLimitReachedException
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.networking.model.Response
import com.emarsys.di.SdkKoinIsolationContext
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.embedded.messages.MessageTagUpdate
import com.emarsys.mobileengage.embedded.messages.TagOperation
import io.kotest.data.Table2
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmbeddedMessagingIntegrationTests {
    private companion object {
        const val STAGING_APP_CODE = "14C19-A121F"
        const val STAGING_UNIVERSAL_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjdXN0b21lcklkIjoyMTg1MjQ1MzAsImlhdCI6MTc1NzU5MjQ1MX0.M7o-tF8Akdf5WBubc_WHLmr2PoGbICL3i4DKUMtkrWA"
    }

    private lateinit var sdkContext: SdkContextApi
    private lateinit var sdkEventDistributor: SdkEventDistributorApi

    @BeforeTest
    fun setup() = runTest {
        AndroidEmarsys.initialize()
        val requestContext = SdkKoinIsolationContext.koin.get<RequestContextApi>()
        requestContext.clientId = STAGING_UNIVERSAL_TOKEN
        requestContext.contactToken = STAGING_UNIVERSAL_TOKEN
        requestContext.clientState = STAGING_UNIVERSAL_TOKEN
        sdkContext = SdkKoinIsolationContext.koin.get<SdkContextApi>()
        sdkContext.defaultUrls =
            sdkContext.defaultUrls.copyWith(
                embeddedMessagingBaseUrl = "https://embedded-messaging-staging.gservice.emarsys.com/embedded-messaging/fake-api"
            )
        sdkContext.config = AndroidEmarsysConfig(applicationCode = STAGING_APP_CODE)
        sdkContext.setSdkState(SdkState.active)
        sdkEventDistributor = SdkKoinIsolationContext.koin.get<SdkEventDistributor>()
        sdkContext.embeddedMessagingFrequencyCapSeconds = 0
    }

    @Test
    fun testFetchBadgeCount_should_handle_requests() = runTest {
        forAll(
            provideExpectedStatusesForAppCodes()
        ) { applicationCode, expectedStatus ->
            sdkContext.config = sdkContext.config?.copyWith(applicationCode = applicationCode)

            val response: SdkEvent.Internal.Sdk.Answer.Response<Response> =
                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(
                        nackCount = 0
                    )
                ).await()

            assertResponse(response, expectedStatus)
        }
    }

    @Test
    fun testFetchMessages_should_handle_requests() = runTest(UnconfinedTestDispatcher()) {
        forAll(
            provideExpectedStatusesForAppCodes()
        ) { applicationCode, expectedStatus ->
            sdkContext.config = sdkContext.config?.copyWith(applicationCode = applicationCode)

            val response: SdkEvent.Internal.Sdk.Answer.Response<Response> =
                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                        offset = 0,
                        nackCount = 0,
                        categoryIds = emptyList()
                    )
                ).await()

            assertResponse(response, expectedStatus)
        }
    }

    @Test
    fun testFetchMeta_should_handle_requests() = runTest {
        forAll(
            provideExpectedStatusesForAppCodes()
        ) { applicationCode, expectedStatus ->
            sdkContext.config = sdkContext.config?.copyWith(applicationCode = applicationCode)

            val response: SdkEvent.Internal.Sdk.Answer.Response<Response> =
                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.EmbeddedMessaging.FetchMeta(
                        nackCount = 0,
                    )
                ).await()

            assertResponse(response, expectedStatus)
        }
    }

    @Test
    fun testUpdateTags_should_handle_requests() = runTest {
        forAll(
            provideExpectedStatusesForAppCodes()
        ) { applicationCode, expectedStatus ->
            sdkContext.config = sdkContext.config?.copyWith(applicationCode = applicationCode)

            val response: SdkEvent.Internal.Sdk.Answer.Response<Response> =
                sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.EmbeddedMessaging.UpdateTagsForMessages(
                        nackCount = 0,
                        updateData = listOf(
                            MessageTagUpdate(
                                "testId",
                                TagOperation.Add,
                                tag = "testTag",
                                trackingInfo = ""
                            )
                        )
                    )
                ).await()

            assertResponse(response, expectedStatus)
        }
    }

    private fun provideExpectedStatusesForAppCodes(): Table2<String, HttpStatusCode> = table(
        headers("applicationCode", "expectedStatus"),
        row(
            "EMS-200",
            HttpStatusCode.OK
        ),
        row(
            "EMS-400",
            HttpStatusCode.BadRequest
        ),
        row(
            "EMS-401",
            HttpStatusCode.Unauthorized
        ),
        row(
            "EMS-404",
            HttpStatusCode.NotFound
        ),
        row(
            "EMS-500",
            HttpStatusCode.InternalServerError
        ),
    )


    private fun assertResponse(
        response: SdkEvent.Internal.Sdk.Answer.Response<Response>,
        expectedStatus: HttpStatusCode
    ) {
        response.result
            .onSuccess {
                it.status shouldBe expectedStatus
            }
            .onFailure {
                when (it) {
                    is SdkException.FailedRequestException -> {
                        it.response.status shouldBe expectedStatus
                    }

                    is RetryLimitReachedException -> {
                        it.message shouldBe "Request retry limit reached!"
                        it.response.status shouldBe expectedStatus
                    }
                }
            }
    }
}