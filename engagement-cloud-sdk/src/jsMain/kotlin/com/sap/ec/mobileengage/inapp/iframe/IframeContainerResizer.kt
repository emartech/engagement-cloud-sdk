package com.sap.ec.mobileengage.inapp.iframe

import web.dom.ElementId
import web.dom.document

internal class IframeContainerResizer: IframeContainerResizerApi {

    override fun resize(id: String, height: Int) {
        document.getElementById(ElementId(id))?.style?.height = "${height}px"
    }

}