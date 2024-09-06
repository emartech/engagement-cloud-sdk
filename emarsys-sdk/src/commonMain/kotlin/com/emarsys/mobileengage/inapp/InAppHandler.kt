package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.inapp.InAppPresentationMode.Overlay

class InAppHandler(
    private val inAppViewProvider: InAppViewProviderApi,
    private val inAppPresenter: InAppPresenterApi): InAppHandlerApi {

    override suspend fun handle(html: String) {
        val view = inAppViewProvider.provide()
        view.load(InAppMessage(html))
        inAppPresenter.present(view, Overlay)
    }
}
