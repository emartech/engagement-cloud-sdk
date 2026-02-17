package com.sap.ec.mobileengage.inapp.iframe

import web.html.HTMLIFrameElement

interface IframeFactoryApi {
    fun create(): HTMLIFrameElement
}