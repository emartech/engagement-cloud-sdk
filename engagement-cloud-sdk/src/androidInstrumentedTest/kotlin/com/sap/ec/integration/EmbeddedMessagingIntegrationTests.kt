package com.sap.ec.integration

import com.sap.ec.AndroidEngagementCloud
import com.sap.ec.api.SdkState
import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.context.copyWith
import com.sap.ec.core.channel.SdkEventDistributor
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.exceptions.SdkException.RetryLimitReachedException
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.di.SdkKoinIsolationContext
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.sap.ec.mobileengage.embeddedmessaging.models.MessageTagUpdate
import com.sap.ec.mobileengage.embeddedmessaging.models.TagOperation
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
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class EmbeddedMessagingIntegrationTests {
    private companion object {
        const val STAGING_APP_CODE = "14C19-A121F"
        const val STAGING_UNIVERSAL_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjdXN0b21lcklkIjoyMTg1MjQ1MzAsImlhdCI6MTc1NzU5MjQ1MX0.M7o-tF8Akdf5WBubc_WHLmr2PoGbICL3i4DKUMtkrWA"
    }

    private lateinit var sdkContext: SdkContextApi
    private lateinit var sdkEventDistributor: SdkEventDistributorApi
    private lateinit var embeddedMessagingContext: EmbeddedMessagingContextApi

    @BeforeTest
    fun setup() = runTest {
        AndroidEngagementCloud.initialize()
        val requestContext = SdkKoinIsolationContext.koin.get<RequestContextApi>()
        requestContext.clientId = STAGING_UNIVERSAL_TOKEN
        requestContext.contactToken = STAGING_UNIVERSAL_TOKEN
        requestContext.clientState = STAGING_UNIVERSAL_TOKEN
        sdkContext = SdkKoinIsolationContext.koin.get<SdkContextApi>()
        sdkContext.defaultUrls =
            sdkContext.defaultUrls.copyWith(
                embeddedMessagingBaseUrl = "https://embedded-messaging-staging.gservice.emarsys.com/embedded-messaging/fake-api"
            )
        sdkContext.config = AndroidEngagementCloudSDKConfig(applicationCode = STAGING_APP_CODE)
        sdkContext.setSdkState(SdkState.Active)
        sdkEventDistributor = SdkKoinIsolationContext.koin.get<SdkEventDistributor>()
        embeddedMessagingContext = SdkKoinIsolationContext.koin.get<EmbeddedMessagingContextApi>()
        embeddedMessagingContext.embeddedMessagingFrequencyCapSeconds = 0
    }

    @Ignore("Fake API is discontinued, will update tests for the real API")
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

    @Ignore("Fake API is discontinued, will update tests for the real API")
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

    @Ignore("Fake API is discontinued, will update tests for the real API")
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

    @Ignore("Fake API is discontinued, will update tests for the real API")
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