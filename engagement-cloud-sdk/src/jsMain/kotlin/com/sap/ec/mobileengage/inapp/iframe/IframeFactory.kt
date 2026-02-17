package com.sap.ec.mobileengage.inapp.iframe

import web.dom.document
import web.html.HTMLIFrameElement

class IframeFactory : IframeFactoryApi {
    override fun create(): HTMLIFrameElement {
        return document.createElement("iframe").unsafeCast<HTMLIFrameElement>().apply {
            style.width = "100%"
            style.padding = "0px"
            style.display = "block"
            style.border = "none"
            setAttribute("sandbox", "allow-scripts")
            setAttribute("scrolling", "no")
        }
    }
}