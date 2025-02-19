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
        val html = """<html></html>"""
        val campaignId = "testCampaignId"
        val mockInAppView: InAppViewApi = mock()
        val mockWebViewHolder: WebViewHolder = mock()
        everySuspend {
            mockInAppView.load(
                InAppMessage(
                    campaignId,
                    html
                )
            )
        } returns mockWebViewHolder
        everySuspend { mockInAppViewProvider.provide() } returns mockInAppView
        everySuspend {
            mockInAppPresenter.present(
                mockInAppView, mockWebViewHolder,
                InAppPresentationMode.Overlay
            )
        } returns Unit

        inAppHandler.handle(campaignId, html)

        verifySuspend { mockInAppView.load(InAppMessage(campaignId, html)) }
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
