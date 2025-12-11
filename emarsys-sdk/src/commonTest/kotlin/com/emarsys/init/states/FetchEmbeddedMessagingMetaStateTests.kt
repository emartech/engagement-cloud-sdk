package com.emarsys.init.states

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.channel.SdkEventWaiterApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.enable.states.FetchEmbeddedMessagingMetaState
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.embeddedmessaging.EmbeddedMessagingContextApi
import com.emarsys.networking.clients.embedded.messaging.model.MetaData
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
      "selectedState" : "#FFC8E6C9",
      "disabledState" : "#FFE0E0E0",
      "hoverState" : "#FF4CAF50",
      "pressedState" : "#FF388E3C",
      "focusState" : "#FF4CAF50",
      "warning" : "#FFF57C00",
      "onWarning" : "#FFFFFFFF",
      "warningContainer" : "#FFFFF3E0",
      "onWarningContainer" : "#FFF57C00",
      "success" : "#FF388E3C",
      "onSuccess" : "#FFFFFFFF",
      "successContainer" : "#FFE8F5E8",
      "onSuccessContainer" : "#FF388E3C",
      "info" : "#FF1976D2",
      "onInfo" : "#FFFFFFFF",
      "infoContainer" : "#FFE3F2FD",
      "onInfoContainer" : "#FF1976D2"
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
      "dialogCornerRadius" : 16,
      "categoryButtonCornerRadius" : 8,
      "messageItemCornerRadius" : 0,
      "filterButtonCornerRadius" : 20,
      "actionButtonCornerRadius" : 8,
      "chipCornerRadius" : 8,
      "detailViewCornerRadius" : 8,
      "headerCornerRadius" : 0,
      "footerCornerRadius" : 0,
      "modalCornerRadius" : 16,
      "snackbarCornerRadius" : 8,
      "tooltipCornerRadius" : 4,
      "badgeCornerRadius" : 12,
      "avatarCornerRadius" : 20,
      "imageCornerRadius" : 8,
      "messageItemPadding" : 16,
      "messageItemMargin" : 0,
      "messageItemSpacing" : 8,
      "dialogPadding" : 24,
      "dialogMargin" : 16,
      "dialogSpacing" : 16,
      "categoryButtonPadding" : 12,
      "categoryButtonMargin" : 4,
      "categoryButtonSpacing" : 8,
      "filterButtonPadding" : 16,
      "filterButtonMargin" : 8,
      "actionButtonPadding" : 16,
      "actionButtonMargin" : 8,
      "headerPadding" : 16,
      "headerMargin" : 0,
      "headerSpacing" : 8,
      "footerPadding" : 16,
      "footerMargin" : 0,
      "footerSpacing" : 8,
      "listPadding" : 0,
      "listMargin" : 0,
      "listSpacing" : 4,
      "detailViewPadding" : 16,
      "detailViewMargin" : 0,
      "detailViewSpacing" : 16,
      "dividerWidth" : 1,
      "dividerColor" : "#FFF57C00",
      "categoryButtonStrokeColor" : "#FFC8E6C9",
      "categoryButtonStrokeSize" : 0.5,
      "messageItemStrokeColor" : "#FFE0E0E0",
      "messageItemStrokeSize" : 1,
      "filterButtonStrokeColor" : "#FFC8E6C9",
      "filterButtonStrokeSize" : 1,
      "actionButtonStrokeColor" : "#FFC8E6C9",
      "actionButtonStrokeSize" : 1,
      "messageItemElevation" : 2,
      "dialogElevation" : 8,
      "categoryButtonElevation" : 0,
      "filterButtonElevation" : 0,
      "actionButtonElevation" : 0,
      "headerElevation" : 4,
      "footerElevation" : 4,
      "modalElevation" : 16,
      "snackbarElevation" : 6,
      "tooltipElevation" : 4,
      "messageItemIconSize" : 20,
      "categoryButtonIconSize" : 16,
      "filterButtonIconSize" : 20,
      "actionButtonIconSize" : 16,
      "headerIconSize" : 24,
      "footerIconSize" : 20,
      "dialogIconSize" : 20,
      "snackbarIconSize" : 24,
      "tooltipIconSize" : 16,
      "listContentPadding" : 0,
      "listItemSpacing" : 0,
      "listItemMargin" : 0,
      "tabButtonPadding" : 16,
      "tabButtonSpacing" : 8,
      "filterButtonSpacing" : 8,
      "actionButtonSpacing" : 8,
      "compactOverlayWidth" : 400,
      "compactOverlayMaxHeight" : 600,
      "compactOverlayPadding" : 8,
      "compactOverlaySpacing" : 4,
      "compactOverlayCornerRadius" : 12,
      "compactOverlayElevation" : 8,
      "emptyStatePadding" : 32,
      "emptyStateSpacing" : 8,
      "emptyStateIconPadding" : 16,
      "detailViewImageHeight" : 200,
      "detailViewImageCornerRadius" : 8,
      "detailViewTagPadding" : 8,
      "detailViewTagSpacing" : 8,
      "detailViewActionSpacing" : 8,
      "swipeDeleteBackgroundCornerRadius" : 12,
      "swipeDeleteButtonSize" : 40,
      "swipeDeleteButtonCornerRadius" : 20,
      "swipeDeleteIconSize" : 16
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