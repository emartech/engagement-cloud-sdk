package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.InstantProvider
import web.dom.document
import web.html.HTMLElement

internal class WebInAppView(
    private val inappScriptExtractor: InAppScriptExtractorApi,
    private val webInAppJsBridgeFactory: Factory<String, WebInAppJsBridge>,
    private val timestampProvider: InstantProvider,
) : InAppViewApi {

    private lateinit var mInAppMessage: InAppMessage
    private var loadingStarted: Long? = null

    override val inAppMessage: InAppMessage
        get() = mInAppMessage

    private fun inAppLoadingMetric(): InAppLoadingMetric {
        return InAppLoadingMetric(
            loadingStarted = loadingStarted ?: 0,
            loadingEnded = timestampProvider.provide().toEpochMilliseconds()
        )
    }

    override suspend fun load(message: InAppMessage): WebViewHolder {
        loadingStarted = timestampProvider.provide().toEpochMilliseconds()

        mInAppMessage = message
        val jsBridge = webInAppJsBridgeFactory.create(message.campaignId)
        jsBridge.register()
        val view = document.createElement("div")
        view.innerHTML = message.html

        val scriptContents = inappScriptExtractor.extract(view)
        createScriptElements(scriptContents).forEach { scriptElement ->
            view.appendChild(scriptElement)
        }

        return WebWebViewHolder(view, inAppLoadingMetric())
    }

    private fun createScriptElements(scriptTexts: List<String>): List<HTMLElement> {
        return scriptTexts.map { scriptContent ->
            val element = document.createElement("script")
            element.setAttribute("type", "text/javascript")
            element.textContent = scriptContent
            element
        }
    }
}
