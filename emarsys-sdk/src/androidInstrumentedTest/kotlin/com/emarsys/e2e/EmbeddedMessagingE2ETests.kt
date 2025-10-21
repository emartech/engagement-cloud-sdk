package com.emarsys.e2e

import com.emarsys.AndroidEmarsys
import com.emarsys.AndroidEmarsysConfig
import com.emarsys.api.SdkState
import com.emarsys.context.SdkContextApi
import com.emarsys.context.copyWith
import com.emarsys.core.channel.SdkEventDistributor
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.networking.model.Response
import com.emarsys.di.SdkKoinIsolationContext
import com.emarsys.event.SdkEvent
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EmbeddedMessagingE2ETests {
    private companion object {
        const val STAGING_APP_CODE = "14C19-A121F"
        const val STAGING_UNIVERSAL_TOKEN_EXPIRES_ON_2025_10_30 =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjdXN0b21lcklkIjoyMTg1MjQ1MzAsImNvbnRhY3RJZCI6MjE4NTI0NTMwLCJwbGF0Zm9ybSI6ImlvcyIsImxhbmd1YWdlIjoiZW4iLCJpYXQiOjE3NjE4MjU2MDB9.be1-49C46hYIOLNjAMSPIgNu8iVtH0DRC8zCzSgiVUA"
    }

    private lateinit var sdkContext: SdkContextApi
    private lateinit var sdkEventDistributor: SdkEventDistributorApi

    @BeforeTest
    fun setup() = runTest {
        AndroidEmarsys.initialize()
        val requestContext = SdkKoinIsolationContext.koin.get<RequestContextApi>()
        requestContext.clientId = STAGING_UNIVERSAL_TOKEN_EXPIRES_ON_2025_10_30
        requestContext.contactToken = STAGING_UNIVERSAL_TOKEN_EXPIRES_ON_2025_10_30
        requestContext.clientState = STAGING_UNIVERSAL_TOKEN_EXPIRES_ON_2025_10_30
        sdkContext = SdkKoinIsolationContext.koin.get<SdkContextApi>()
        sdkContext.defaultUrls =
            sdkContext.defaultUrls.copyWith(
                embeddedMessagingBaseUrl = "https://embedded-messaging-staging.gservice.emarsys.com/embedded-messaging/api"
            )
        sdkContext.config = AndroidEmarsysConfig(applicationCode = STAGING_APP_CODE)
        sdkContext.setSdkState(SdkState.active)
        sdkEventDistributor = SdkKoinIsolationContext.koin.get<SdkEventDistributor>()
        sdkContext.embeddedMessagingFrequencyCapSeconds = 0
    }

    @Test
    @Ignore
    fun testFetchBadgeCount_shouldEmitEvent_triggerRequest_and_returnHttpOkResponse() = runTest {
        val response: SdkEvent.Internal.Sdk.Answer.Response<Response> =
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(
                    nackCount = 0
                )
            ).await()

        assertResponse(response)
    }

    @Test
    @Ignore
    fun testFetchMessages_shouldEmitEvent_triggerRequest_and_returnHttpOkResponse() = runTest {
        val response: SdkEvent.Internal.Sdk.Answer.Response<Response> =
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                    offset = 0,
                    nackCount = 0,
                    categoryIds = emptyList()
                )
            ).await()

        assertResponse(response)
    }
}

private fun assertResponse(
    response: SdkEvent.Internal.Sdk.Answer.Response<Response>
) {
    response.result.isSuccess shouldBe true
    response.result
        .onSuccess {
            it.status shouldBe HttpStatusCode.OK
        }
}
