package com.emarsys.mobileengage.inapp

interface InAppPresenterApi {

    suspend fun present(
        viewApi: InAppViewApi,
        webViewHolder: WebViewHolder,
        mode: InAppPresentationMode,
        animation: InAppPresentationAnimation? = InAppPresentationAnimation.Slide(
            0.3
        )
    )

}
