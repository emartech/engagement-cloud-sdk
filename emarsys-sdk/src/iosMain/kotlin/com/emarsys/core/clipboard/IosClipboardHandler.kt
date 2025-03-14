package com.emarsys.core.clipboard

import com.emarsys.core.actions.clipboard.ClipboardHandlerApi
import platform.UIKit.UIPasteboard

class IosClipboardHandler(private val uiPasteboard: UIPasteboard) : ClipboardHandlerApi {

    override suspend fun copyToClipboard(text: String) {
        uiPasteboard.setString(text)
    }

}