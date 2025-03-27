package com.emarsys.mobileengage.inapp

interface InAppPresenterApi {

    suspend fun present(
        inAppView: InAppViewApi,
        webViewHolder: WebViewHolder,
        mode: InAppPresentationMode,
        animation: InAppPresentationAnimation? = InAppPresentationAnimation.Slide(
            0.3
        )
    )

}
