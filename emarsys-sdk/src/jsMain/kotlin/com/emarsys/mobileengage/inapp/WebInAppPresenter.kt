package com.emarsys.mobileengage.inapp

import com.emarsys.core.message.MsgHubApi
import web.dom.document
import web.html.HTMLElement

class WebInAppPresenter(private val msgHubApi: MsgHubApi) : InAppPresenterApi {
    override suspend fun present(
        view: InAppViewApi,
        mode: InAppPresentationMode,
        animation: InAppPresentationAnimation?
    ) {
        val inappView = (view as WebInAppView).inappView
        val styledInappView = inappView?.let {
            if (mode == InAppPresentationMode.Overlay) {
                applyOverlayStyle(inappView)
            } else {
                applyRibbonStyle(inappView)
            }
        }
        msgHubApi.subscribe("dismiss") {
            styledInappView?.remove()
        }
        styledInappView?.let { document.body.appendChild(it) }
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