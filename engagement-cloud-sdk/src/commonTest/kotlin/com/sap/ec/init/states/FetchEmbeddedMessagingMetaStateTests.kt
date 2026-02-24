package com.sap.ec.init.states

import com.sap.ec.context.Features
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.channel.SdkEventWaiterApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.enable.states.FetchEmbeddedMessagingMetaState
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
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

class FetchEmbeddedMessagingMetaStateTests {
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockSdkEventWaiter: SdkEventWaiterApi
    private lateinit var state: FetchEmbeddedMessagingMetaState

    @BeforeTest
    fun setup() {
        mockSdkLogger = mock(MockMode.autofill)
        mockSdkEventDistributor = mock(MockMode.autofill)
        mockSdkEventWaiter = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)

        state = FetchEmbeddedMessagingMetaState(
            sdkEventDistributor = mockSdkEventDistributor,
            sdkContext = mockSdkContext,
            sdkLogger = mockSdkLogger
        )
    }

    @Test
    fun testActive_shouldEmitFetchMetaEvent_andReturnSuccessUnit_whenMetaResponseResultIsSuccess() =
        runTest {
            every { mockSdkContext.features } returns mutableSetOf(Features.EmbeddedMessaging)
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
            val response = SdkEvent.Internal.Sdk.Answer.Response(
                "0",
                Result.success(
                    Response(
                        originalRequest = UrlRequest(
                            Url("https://sap.com/api/v1/testAppCode/meta"),
                            HttpMethod.Get
                        ),
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
        }

    @Test
    fun testActive_shouldEmitFetchMetaEvent_andReturnFailure_whenMetaResponseResultIsFailure() =
        runTest {
            every { mockSdkContext.features } returns mutableSetOf(Features.EmbeddedMessaging)
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
        }

    @Test
    fun testActive_shouldNotCallEmitFetchMetaEvent_andReturnSuccess_whenEmbeddedMessagingContextIsDisabled() =
        runTest {
            every { mockSdkContext.features } returns mutableSetOf()

            val result = state.active()

            result shouldBe Result.success(Unit)
            verifySuspend(VerifyMode.exactly(0)) { mockSdkEventDistributor.registerEvent(any<SdkEvent.Internal.EmbeddedMessaging.FetchMeta>()) }
        }
}