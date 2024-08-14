package com.emarsys.mobileengage.inapp

import web.html.HTMLElement

class InAppScriptExtractor: InAppScriptExtractorApi {

    override fun extract(htmlElement: HTMLElement): List<String> {
        return htmlElement.querySelectorAll("script").iterator().asSequence().map { element ->
            element.textContent
        }.filterNotNull().toList()
    }
}