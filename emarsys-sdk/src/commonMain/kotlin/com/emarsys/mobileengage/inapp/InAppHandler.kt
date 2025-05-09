package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.inapp.InAppPresentationMode.Overlay

class InAppHandler(
    private val inAppViewProvider: InAppViewProviderApi,
    private val inAppPresenter: InAppPresenterApi
) : InAppHandlerApi {

    override suspend fun handle(inAppMessage: InAppMessage) {
        val view = inAppViewProvider.provide()
        val webViewHolder = view.load(inAppMessage)
        inAppPresenter.present(view, webViewHolder, Overlay)
    }
}
