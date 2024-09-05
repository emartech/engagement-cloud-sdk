package com.emarsys.mobileengage.inApp

import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.InAppPresentationMode.Overlay
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppViewProviderApi

class InAppHandler(
    private val inAppViewProvider: InAppViewProviderApi,
    private val inAppPresenter: InAppPresenterApi): InAppHandlerApi {

    override suspend fun handle(html: String) {
        val view = inAppViewProvider.provide()
        view.load(InAppMessage(html))
        inAppPresenter.present(view, Overlay)
    }
}
