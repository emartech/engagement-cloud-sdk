package com.emarsys.mobileengage.inapp

interface InAppPresenterApi {
    suspend fun trackMetric(
        campaignId: String,
        loadingMetric: InAppLoadingMetric,
        onScreenTimeStart: Long,
        onScreenTimeEnd: Long
    )

    suspend fun present(
        inAppView: InAppViewApi,
        webViewHolder: WebViewHolder,
        mode: InAppPresentationMode,
        animation: InAppPresentationAnimation? = InAppPresentationAnimation.Slide(
            0.3
        )
    )

}
