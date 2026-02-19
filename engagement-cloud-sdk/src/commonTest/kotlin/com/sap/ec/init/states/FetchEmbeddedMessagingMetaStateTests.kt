package com.sap.ec.init.states

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.channel.SdkEventWaiterApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.enable.states.FetchEmbeddedMessagingMetaState
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.sap.ec.networking.clients.embedded.messaging.model.MetaData
import com.sap.ec.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
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
  "version" : "v1",
  "labels" : {
    "allMessagesHeader" : "All Messages",
    "unreadMessagesHeader" : "Unread Messages",
    "filterCategories" : "Filter Categories",
    "pinnedMessagesTitle" : "Pinned Messages",
    "detailedMessageCloseButton" : "Close",
    "deleteDetailedMessageButton" : "Delete",
    "emptyStateTitle" : "No Messages",
    "emptyStateDescription" : "You have no messages at the moment."
  },
  "design" : {
    "fillColor" : {
      "primary" : "#FF526525",
      "onPrimary" : "#FFFFFFFF",
      "primaryContainer" : "#FFE8F5E8",
      "onPrimaryContainer" : "#FF1B5E20",
      "secondary" : "#FF2E7D32",
      "onSecondary" : "#FFFFFFFF",
      "secondaryContainer" : "#FFDFE6C5",
      "onSecondaryContainer" : "#FF181E09",
      "tertiary" : "#FF388E3C",
      "onTertiary" : "#FFFFFFFF",
      "tertiaryContainer" : "#FFA5D6A7",
      "onTertiaryContainer" : "#FF1B5E20",
      "error" : "#FFD32F2F",
      "onError" : "#FFFFFFFF",
      "errorContainer" : "#FFFFEBEE",
      "onErrorContainer" : "#FFD32F2F",
      "background" : "#FFF4F4E8",
      "onBackground" : "#FF1B5E20",
      "surface" : "#FFEFEFE2",
      "onSurface" : "#FF45483C",
      "surfaceVariant" : "#FFF6F5E9",
      "onSurfaceVariant" : "#FF2E7D32",
      "surfaceContainer" : "#FFF5F5F5",
      "surfaceContainerHigh" : "#FFE9E9DD",
      "surfaceContainerHighest" : "#FFE8F5E8",
      "surfaceContainerLow" : "#FFF4F4E8",
      "surfaceContainerLowest" : "#FFFFFFFF",
      "surfaceDim" : "#FFE0E0E0",
      "surfaceBright" : "#FFFFFFFF",
      "outline" : "#FF76786B",
      "outlineVariant" : "#FFA5D6A7",
      "inverseSurface" : "#FF2E7D32",
      "inverseOnSurface" : "#FFFFFFFF",
      "inversePrimary" : "#FF4CAF50",
      "scrim" : "#80000000",
      "surfaceTint": "#526525",
      "primaryFixed": "#ffffff",
      "primaryFixedDim": "#e4edcf",
      "onPrimaryFixed": "#000000",
      "onPrimaryFixedVariant": "#151a0a",
      "secondaryFixed": "#ffffff",
      "secondaryFixedDim": "#d7ead2",
      "onSecondaryFixed": "#000000",
      "onSecondaryFixedVariant": "#0e190b",
      "tertiaryFixed": "#ffffff",
      "tertiaryFixedDim": "#eae5d2",
      "onTertiaryFixed": "#000000",
      "onTertiaryFixedVariant": "#19160b"
    },
    "text" : {
      "displayLargeFontSize" : 57,
      "displayMediumFontSize" : 45,
      "displaySmallFontSize" : 36,
      "headlineLargeFontSize" : 32,
      "headlineMediumFontSize" : 28,
      "headlineSmallFontSize" : 24,
      "titleLargeFontSize" : 22,
      "titleMediumFontSize" : 16,
      "titleSmallFontSize" : 14,
      "bodyLargeFontSize" : 16,
      "bodyMediumFontSize" : 14,
      "bodySmallFontSize" : 12,
      "labelLargeFontSize" : 14,
      "labelMediumFontSize" : 12,
      "labelSmallFontSize" : 11
    },
    "misc" : {
        "messageItemMargin": 8,
        "messageItemElevation": 8,
        "buttonElevation": 8,
        "listContentPadding": 8,
        "listItemSpacing": 8,
        "compactOverlayWidth": 8,
        "compactOverlayMaxHeight": 8,
        "compactOverlayCornerRadius": 8,
        "compactOverlayElevation": 8,
        "messageItemCardCornerRadius": 8,
        "messageItemCardElevation": 8,
        "messageItemImageHeight": 8,
        "messageItemImageClipShape": "rectangle",
        "messageItemImageCornerRadius": 8
    }
  }
}"""
        val expectedMetaData = JsonUtil.json.decodeFromString<MetaData>(metaDataResponse)
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
        verifySuspend { mockEmbeddedMessagingContext.metaData = null }
        verifySuspend { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>()) }
        verifySuspend(VerifyMode.exactly(0)) { mockEmbeddedMessagingContext.metaData = any() }
    }
}