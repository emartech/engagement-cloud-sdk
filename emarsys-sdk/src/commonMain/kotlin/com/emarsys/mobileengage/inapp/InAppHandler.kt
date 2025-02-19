package com.emarsys.mobileengage.inapp

import com.emarsys.mobileengage.inapp.InAppPresentationMode.Overlay

class InAppHandler(
    private val inAppViewProvider: InAppViewProviderApi,
    private val inAppPresenter: InAppPresenterApi
) : InAppHandlerApi {

    override suspend fun handle(campaignId: String, html: String) {
        val view = inAppViewProvider.provide()
        val webViewHolder = view.load(InAppMessage(campaignId, html))
        inAppPresenter.present(view, webViewHolder, Overlay)
    }
}
