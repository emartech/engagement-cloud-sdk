package com.emarsys.mobileengage.inapp.iframe

import web.html.HTMLIFrameElement

interface IframeFactoryApi {
    fun create(): HTMLIFrameElement
}