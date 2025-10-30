package com.emarsys.init.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.channel.SdkEventWaiterApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.embedded.messages.EmbeddedMessagingContextApi
import com.emarsys.networking.clients.embedded.messaging.model.MetaData
import dev.mokkery.*
import dev.mokkery.answering.returns
import dev.mokkery.matcher.any
import dev.mokkery.verify.VerifyMode
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class FetchEmbeddedMessagingMetaStateTests {
    private lateinit var mockEmbeddedMessagingContext: EmbeddedMessagingContextApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockSdkEventWaiter: SdkEventWaiterApi

    @BeforeTest
    fun setup() {
        mockEmbeddedMessagingContext = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
    }

    @Test
    fun testActive_should_EmitFetchMetaEvent_and_storeMetaDataInEmbeddedMessagingContext_when_MetaResponseResult_IsSuccess() = runTest {
        val state = FetchEmbeddedMessagingMetaState(
            embeddedMessagingContext = mockEmbeddedMessagingContext,
            sdkEventDistributor = mockSdkEventDistributor,
            sdkLogger = mockSdkLogger
        )
        val metaDataResponse = """{
  "version": "v1",
  "design": {
    "fillColor": {
      "primaryBackground": "rgba(255, 255, 255, 1)",
      "secondaryBackground": "rgba(255, 255, 255, 1)",
      "buttonDefaultState": "rgba(255, 255, 255, 1)",
      "buttonToggledState": "rgba(255, 255, 255, 1)",
      "chipsButtonDefaultState": "rgba(255, 255, 255, 1)",
      "chipsButtonDisabledState": "rgba(255, 255, 255, 1)",
      "chipsButtonToggledState": "rgba(255, 255, 255, 1)",
      "listMessageSelectedState": "rgba(255, 255, 255, 1)",
      "dialogOverlayShade": "rgba(255, 255, 255, 1)",
      "snackbarBackground": "rgba(255, 255, 255, 1)"
    },
    "text": {
      "defaultFontType": "M3",
      "defaultFontColor": "rgba(255, 255, 255, 1)",
      "defaultFontSize": 14,
      "selectedTabFontColor": "rgba(255, 255, 255, 1)",
      "filterButtonToggledStateFontColor": "rgba(255, 255, 255, 1)",
      "listMessageLeadDialogTextFontColor": "rgba(255, 255, 255, 1)",
      "listMessageLeadDialogTextFontSize": 14,
      "listMessageDescriptionFontColor": "rgba(255, 255, 255, 1)",
      "listMessageDescriptionFontSize": 14,
      "emptyStateTitleFontSize": 14,
      "emptyStateFontColor": "rgba(255, 255, 255, 1)",
      "emptyStateDescriptionFontSize": 14,
      "emptyStateDescriptionFontColor": "rgba(255, 255, 255, 1)",
      "chipsButtonDisabledStateFontColor": "rgba(255, 255, 255, 1)",
      "chipsButtonSelectedStateFontColor": "rgba(255, 255, 255, 1)"
    },
    "misc": {
      "dividerWidth": 20,
      "dividerColor": "rgba(255, 255, 255, 1)",
      "dialogCornerRadius": 12,
      "filterButtonCornerRadius": 12,
      "chipsButtonCornerRadius": 12,
      "chipsButtonDefaultStateStrokeColor": "rgba(255, 255, 255, 1)",
      "chipsButtonDefaultStateStrokeSize": 2
    }
    }
  }"""
        val expectedMetaData = Json.decodeFromString<MetaData>(metaDataResponse)
        val response = SdkEvent.Internal.Sdk.Answer.Response(
            "0",
            Result.success(
                Response(
                    originalRequest = UrlRequest(Url("https://noonecares.com/api/v1/testAppCode/meta"), HttpMethod.Get),
                    status = HttpStatusCode.OK,
                    headers = headersOf(),
                    bodyAsText = metaDataResponse
                )
            )
        )
        everySuspend { mockSdkEventWaiter.await<Response>() } returns response
        everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>()) } returns mockSdkEventWaiter

        val result = state.active()

        result shouldBe Result.success(Unit)
        verifySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>()) }
        verify { mockEmbeddedMessagingContext.metaData = expectedMetaData }
    }

    @Test
    fun testActive_should_EmitFetchMetaEvent_and_notStoreAnything_when_MetaResponseResult_IsFailure() = runTest {
        val state = FetchEmbeddedMessagingMetaState(
            embeddedMessagingContext = mockEmbeddedMessagingContext,
            sdkEventDistributor = mockSdkEventDistributor,
            sdkLogger = mockSdkLogger
        )
        val exception = Exception("any exception happened")
        val response = SdkEvent.Internal.Sdk.Answer.Response(
            "0",
            Result.failure<Response>(exception)
        )
        everySuspend { mockSdkEventWaiter.await<Response>() } returns response
        everySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>()) } returns mockSdkEventWaiter

        val result = state.active()

        result shouldBe Result.failure(exception)
        verifySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>()) }
        verifySuspend(VerifyMode.exactly(0)) { mockEmbeddedMessagingContext.metaData = any() }
    }
}