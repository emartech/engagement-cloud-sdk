package com.emarsys.mobileengage.inapp

import web.html.HTMLElement

interface InappScriptExtractorApi {
    fun extract(htmlElement: HTMLElement): List<String>
}