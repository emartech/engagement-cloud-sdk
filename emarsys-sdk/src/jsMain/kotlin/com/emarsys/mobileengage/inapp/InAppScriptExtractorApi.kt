package com.emarsys.mobileengage.inapp

import web.html.HTMLElement

interface InAppScriptExtractorApi {
    fun extract(htmlElement: HTMLElement): List<String>
}