package com.emarsys.mobileengage.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import com.emarsys.core.clipboard.ClipboardHandlerApi

class AndroidClipboardHandler(private val clipboardManager: ClipboardManager) :
    ClipboardHandlerApi {

    companion object {
        const val TEXT_LABEL = "copiedFromInapp"
    }

    override suspend fun copyToClipboard(text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText(TEXT_LABEL, text))
    }

}