package com.emarsys.mobileengage.inapp

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.event.model.SdkEvent
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
        campaignId: String,
        loadingMetric: InAppLoadingMetric,
        onScreenTimeStart: Long,
        onScreenTimeEnd: Long
    ) {
        logger.metric(
            message = "InAppMetric",
            data = buildJsonObject {
                put("campaignId", JsonPrimitive(campaignId))
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
        view: InAppViewApi,
        webViewHolder: WebViewHolder,
        mode: InAppPresentationMode,
        animation: InAppPresentationAnimation?
    ) {
        val inappView = (webViewHolder as WebWebViewHolder).webView
        val styledInappView = inappView.let {
            if (mode == InAppPresentationMode.Overlay) {
                applyOverlayStyle(inappView)
            } else {
                applyRibbonStyle(inappView)
            }
        }
        CoroutineScope(sdkDispatcher).launch {
            sdkEventDistributor.sdkEventFlow.first { it is SdkEvent.Internal.Sdk.Dismiss && it.id == view.inAppMessage.campaignId }
            styledInappView.remove()
        }

        styledInappView.let { document.body.appendChild(it) }
    }

    private fun applyOverlayStyle(viewContainer: HTMLElement): HTMLElement {
        return viewContainer.apply {
            this.style.position = "fixed"
            this.style.display = "true"
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
            this.style.display = "true"
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