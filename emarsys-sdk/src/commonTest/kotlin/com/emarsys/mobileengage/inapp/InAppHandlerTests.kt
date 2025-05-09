package com.emarsys.mobileengage.inapp

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class InAppHandlerTests {
    private lateinit var inAppHandler: InAppHandler
    private lateinit var mockInAppPresenter: InAppPresenterApi
    private lateinit var mockInAppViewProvider: InAppViewProviderApi

    @BeforeTest
    fun setup() = runTest {
        mockInAppPresenter = mock()
        mockInAppViewProvider = mock()

        inAppHandler = InAppHandler(mockInAppViewProvider, mockInAppPresenter)
    }

    @Test
    fun handle_shouldPresentInAppHtml() = runTest {
        val content = """<html></html>"""
        val trackingInfo = """{"key":"value"}"""
        val mockInAppView: InAppViewApi = mock()
        val mockWebViewHolder: WebViewHolder = mock()
        val inAppMessage =
            InAppMessage(
                dismissId = "dismissId",
                type = InAppType.OVERLAY,
                trackingInfo = trackingInfo,
                content = content
            )
        everySuspend { mockInAppView.load(inAppMessage) } returns mockWebViewHolder
        everySuspend { mockInAppViewProvider.provide() } returns mockInAppView
        everySuspend {
            mockInAppPresenter.present(
                mockInAppView, mockWebViewHolder,
                InAppPresentationMode.Overlay
            )
        } returns Unit

        inAppHandler.handle(inAppMessage)

        verifySuspend { mockInAppView.load(inAppMessage) }
        verifySuspend { mockInAppViewProvider.provide() }
        verifySuspend {
            mockInAppPresenter.present(
                mockInAppView,
                mockWebViewHolder,
                InAppPresentationMode.Overlay
            )
        }
    }

}
