package com.sap.ec.mobileengage.inapp.iframe

import web.html.HTMLIFrameElement

internal interface IframeFactoryApi {
    fun create(): HTMLIFrameElement
}