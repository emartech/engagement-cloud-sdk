package com.emarsys.mobileengage.inApp

import web.html.HTMLElement

interface InappScriptExtractorApi {
    fun extract(htmlElement: HTMLElement): List<String>
}