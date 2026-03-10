package com.sap.ec.mobileengage.inapp

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.inapp.presentation.InAppPresentationAnimation
import com.sap.ec.mobileengage.inapp.presentation.InAppPresentationMode
import com.sap.ec.mobileengage.inapp.presentation.InAppPresenterApi
import com.sap.ec.mobileengage.inapp.presentation.InAppType
import com.sap.ec.mobileengage.inapp.reporting.InAppLoadingMetric
import com.sap.ec.mobileengage.inapp.view.InAppViewApi
import com.sap.ec.mobileengage.inapp.webview.WebViewHolder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import web.dom.document
import web.html.HTMLElement

internal class WebInAppPresenter(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkDispatcher: CoroutineDispatcher,
    private val logger: Logger
) : InAppPresenterApi {
    override suspend fun trackMetric(
        trackingInfo: String,
        loadingMetric: InAppLoadingMetric,
        onScreenTimeStart: Long,
        onScreenTimeEnd: Long
    ) {
        logger.metric(
            message = "InAppMetric",
            data = buildJsonObject {
                put("trackingInfo", JsonPrimitive(trackingInfo))
                put(
                    "loadingTimeStart",
                    JsonPrimitive(loadingMetric.loadingStarted)
                )
                put(
                    "loadingTimeEnd",
                    JsonPrimitive(loadingMetric.loadingEnded)
                )
                put(
                    "loadingTimeDuration",
                    JsonPrimitive(loadingMetric.loadingEnded - loadingMetric.loadingStarted)
                )
                put("onScreenTimeStart", JsonPrimitive(onScreenTimeStart))
                put("onScreenTimeEnd", JsonPrimitive(onScreenTimeEnd))
                put(
                    "onScreenTimeDuration",
                    JsonPrimitive(onScreenTimeEnd - onScreenTimeStart)
                )
            }
        )
    }

    override suspend fun present(
        inAppView: InAppViewApi,
        webViewHolder: WebViewHolder,
        mode: InAppPresentationMode,
        animation: InAppPresentationAnimation?
    ) {
        val inAppMessage = inAppView.inAppMessage
        if (inAppMessage.type == InAppType.INLINE || inAppMessage.type.name == "INLINE") {
            return
        }

        val view = (webViewHolder as WebWebViewHolder).webView
        val styledInAppView = view.let {
            if (mode is InAppPresentationMode.Overlay) {
                applyOverlayStyle(view)
            } else {
                applyRibbonStyle(view)
            }
        }
        sdkEventDistributor.registerEvent(
            SdkEvent.Internal.InApp.Viewed(
                trackingInfo = inAppView.inAppMessage.trackingInfo,
                attributes = null
            )
        )
        CoroutineScope(sdkDispatcher).launch {
            sdkEventDistributor.sdkEventFlow.first { it is SdkEvent.Internal.Sdk.Dismiss && it.id == inAppView.inAppMessage.dismissId }
            styledInAppView.remove()
        }

        styledInAppView.let { document.body.appendChild(it) }
    }

    private fun applyOverlayStyle(viewContainer: HTMLElement): HTMLElement {
        return viewContainer.apply {
            this.style.position = "fixed"
            this.style.width = "100%"
            this.style.height = "100%"
            this.style.top = "0"
            this.style.left = "0"
            this.style.right = "0"
            this.style.bottom = "0"
            this.style.backgroundColor = "rgba(0,0,0,0.5)"
            this.style.zIndex = "2"
            this.style.cursor = "pointer"
        }
    }

    private fun applyRibbonStyle(viewContainer: HTMLElement): HTMLElement {
        return viewContainer.apply {
            this.style.position = "fixed"
            this.style.width = "100%"
            this.style.height = "20%"
            this.style.top = "0"
            this.style.left = "0"
            this.style.right = "0"
            this.style.bottom = "0"
            this.style.backgroundColor = "rgba(0,0,0,0.5)"
            this.style.zIndex = "2"
            this.style.cursor = "pointer"
        }
    }
}