package com.sap.ec.core.clipboard

import com.sap.ec.core.actions.clipboard.ClipboardHandlerApi
import platform.UIKit.UIPasteboard

class IosClipboardHandler(private val uiPasteboard: UIPasteboard) : ClipboardHandlerApi {

    override suspend fun copyToClipboard(text: String) {
        uiPasteboard.setString(text)
    }

}