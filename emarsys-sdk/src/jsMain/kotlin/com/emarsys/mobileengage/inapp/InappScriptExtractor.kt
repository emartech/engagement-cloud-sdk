package com.emarsys.mobileengage.inApp

import web.html.HTMLElement

class InappScriptExtractor: InappScriptExtractorApi {

    override fun extract(htmlElement: HTMLElement): List<String> {
        return htmlElement.querySelectorAll("script").iterator().asSequence().map { element ->
            element.textContent
        }.filterNotNull().toList()
    }
}