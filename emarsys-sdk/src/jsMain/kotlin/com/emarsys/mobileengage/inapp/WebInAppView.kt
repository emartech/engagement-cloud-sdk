package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.Factory
import web.dom.document
import web.html.HTMLElement

class WebInAppView(
    private val inappScriptExtractor: InAppScriptExtractorApi,
    private val inAppJsBridgeFactory: Factory<String, InAppJsBridge>
) : InAppViewApi {

    private lateinit var mInAppMessage: InAppMessage
    override val inAppMessage: InAppMessage
        get() = mInAppMessage

    override suspend fun load(message: InAppMessage): WebViewHolder {
        mInAppMessage = message
        val jsBridge = inAppJsBridgeFactory.create(message.campaignId)
        jsBridge.register()
        val view = document.createElement("div")
        view.innerHTML = message.html

        val scriptContents = inappScriptExtractor.extract(view)
        createScriptElements(scriptContents).forEach { scriptElement ->
            view.appendChild(scriptElement)
        }

        return WebWebViewHolder(view)
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
