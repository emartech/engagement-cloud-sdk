package com.emarsys.integration

import com.emarsys.AndroidEmarsysConfig
import com.emarsys.Emarsys
import com.emarsys.context.SdkContextApi
import com.emarsys.context.copyWith
import com.emarsys.core.channel.SdkEventDistributor
import com.emarsys.core.exceptions.SdkException
import com.emarsys.core.networking.model.Response
import com.emarsys.di.SdkKoinIsolationContext
import com.emarsys.event.SdkEvent
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmbeddedMessagingIntegrationTests {

    @BeforeTest
    fun setup() {
        val sdkContext = SdkKoinIsolationContext.koin.get<SdkContextApi>()

        sdkContext.defaultUrls =
            sdkContext.defaultUrls.copyWith(
                embeddedMessagingBaseUrl = "https://embedded-messaging-staging.gservice.emarsys.com/embedded-messaging/fake-api",
                clientServiceBaseUrl = "https://me-client-staging.eservice.emarsys.com",
                eventServiceBaseUrl = "https://mobile-events-staging.eservice.emarsys.com"
            )
    }

    @AfterTest
    fun tearDown() = runTest {
        Emarsys.disableTracking()

    }

    @Test
    fun testEmbeddedMessagingFetchMessages() = runTest {
        val sdkEventDistributor = SdkKoinIsolationContext.koin.get<SdkEventDistributor>()
        Emarsys.enableTracking(
            AndroidEmarsysConfig(
                applicationCode = "14C19-A121F",
            )
        )

        advanceUntilIdle()

        val sdkContext = SdkKoinIsolationContext.koin.get<SdkContextApi>()
        sdkContext.defaultUrls =
            sdkContext.defaultUrls.copyWith(embeddedMessagingBaseUrl = "https://embedded-messaging-staging.gservice.emarsys.com/embedded-messaging/fake-api")
        sdkContext.config = sdkContext.config?.copyWith(applicationCode = "EMS-200")
        val response: SdkEvent.Internal.Sdk.Answer.Response<Response> = sdkEventDistributor.registerEvent(
            SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                offset = 0,
                nackCount = 0,
                categoryIds = emptyList()
            )
        ).await()
        response.result.onFailure { error ->
            if (error is SdkException.FailedRequestException) {
                println(error.response.originalRequest.headers)
            }
        }
        response.result.isSuccess shouldBe true
    }

    @Test
    fun testEmbeddedMessagingFetchBadgeCount() = runTest {
        val sdkEventDistributor = SdkKoinIsolationContext.koin.get<SdkEventDistributor>()
        val result = Emarsys.enableTracking(
            AndroidEmarsysConfig(
                applicationCode = "14C19-A121F",
            )
        )

        result.onFailure { error ->
            println(error.localizedMessage)
        }

        advanceUntilIdle()

        val sdkContext = SdkKoinIsolationContext.koin.get<SdkContextApi>()

        sdkContext.config = sdkContext.config?.copyWith(applicationCode = "EMS-200")
        //  val requestContext = SdkKoinIsolationContext.koin.get<RequestContextApi>()
        //  requestContext.contactToken =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjdXN0b21lcklkIjoyMTg1MjQ1MzAsImlhdCI6MTc1NzU5MjQ1MX0.M7o-tF8Akdf5WBubc_WHLmr2PoGbICL3i4DKUMtkrWA"
        //  requestContext.clientState =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjdXN0b21lcklkIjoyMTg1MjQ1MzAsImlhdCI6MTc1NzU5MjQ1MX0.M7o-tF8Akdf5WBubc_WHLmr2PoGbICL3i4DKUMtkrWA"
        val response: SdkEvent.Internal.Sdk.Answer.Response<Response> = sdkEventDistributor.registerEvent(
            SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(
                nackCount = 0,
            )
        ).await()
        response.result.onFailure { error ->
            error.printStackTrace()
            if (error is SdkException.FailedRequestException) {
                println(error.response.originalRequest.headers)
            }
        }
        response.result.isSuccess shouldBe true
    }
}