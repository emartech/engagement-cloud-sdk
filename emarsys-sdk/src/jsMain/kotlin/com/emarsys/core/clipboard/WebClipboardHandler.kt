package com.emarsys.core.clipboard

import kotlinx.coroutines.await
import org.w3c.dom.clipboard.Clipboard

class WebClipboardHandler(private val clipboard: Clipboard) : ClipboardHandlerApi {

    override suspend fun copyToClipboard(text: String) {
        clipboard.writeText(text).await()
    }

}