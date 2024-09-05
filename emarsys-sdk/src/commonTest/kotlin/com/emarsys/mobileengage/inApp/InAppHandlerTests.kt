package com.emarsys.mobileengage.inApp

import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.InAppPresentationMode
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppViewApi
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
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

        val mockInAppView: InAppViewApi = mock()
        everySuspend { mockInAppView.load(InAppMessage(html)) } returns Unit
        everySuspend { mockInAppViewProvider.provide() } returns mockInAppView
        everySuspend { mockInAppPresenter.present(mockInAppView, InAppPresentationMode.Overlay) } returns Unit

        inAppHandler.handle(html)

        verifySuspend { mockInAppView.load(InAppMessage(html)) }
        verifySuspend { mockInAppViewProvider.provide() }
        verifySuspend { mockInAppPresenter.present(mockInAppView, InAppPresentationMode.Overlay) }
    }

}
