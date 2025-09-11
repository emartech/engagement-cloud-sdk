package com.emarsys.integration

import com.emarsys.AndroidEmarsysConfig
import com.emarsys.Emarsys
import com.emarsys.context.SdkContextApi
import com.emarsys.context.copyWith
import com.emarsys.core.channel.SdkEventDistributor
import com.emarsys.core.networking.model.Response
import com.emarsys.di.SdkKoinIsolationContext
import com.emarsys.event.SdkEvent
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmbeddedMessagingIntegrationTests {

    @BeforeTest
    fun setup() {

    }

    @Ignore("Don't run these tests, WIP")
    @Test
    fun testEmbeddedMessagingFetchMessages() = runTest {
        val sdkEventDistributor = SdkKoinIsolationContext.koin.get<SdkEventDistributor>()
        Emarsys.enableTracking(
            AndroidEmarsysConfig(
                applicationCode = "EMS11-C3FD3",
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
        response.result.isSuccess shouldBe true
    }
}