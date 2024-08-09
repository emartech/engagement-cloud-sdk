package com.emarsys.mobileengage.inapp

import web.dom.document
import web.html.HTMLElement

class WebInappView(private val inappScriptExtractor: InappScriptExtractorApi) : InAppViewApi {
    var inappView: HTMLElement? = null

    override suspend fun load(message: InAppMessage) {
        val view = document.createElement("div")
        view.innerHTML = message.content()

        val scriptContents = inappScriptExtractor.extract(view)
        createScriptElements(scriptContents).forEach { scriptElement ->
            view.appendChild(scriptElement)
        }

        inappView = view
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
