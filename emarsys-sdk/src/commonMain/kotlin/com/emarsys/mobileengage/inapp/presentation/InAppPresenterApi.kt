package com.emarsys.mobileengage.inapp.presentation

import com.emarsys.mobileengage.inapp.reporting.InAppLoadingMetric
import com.emarsys.mobileengage.inapp.view.InAppViewApi
import com.emarsys.mobileengage.inapp.webview.WebViewHolder

interface InAppPresenterApi {
    suspend fun trackMetric(
        trackingInfo: String,
        loadingMetric: InAppLoadingMetric,
        onScreenTimeStart: Long,
        onScreenTimeEnd: Long
    )

    suspend fun present(
        inAppView: InAppViewApi,
        webViewHolder: WebViewHolder,
        mode: InAppPresentationMode,
        animation: InAppPresentationAnimation? = InAppPresentationAnimation.Slide(0.3)
    )
}
