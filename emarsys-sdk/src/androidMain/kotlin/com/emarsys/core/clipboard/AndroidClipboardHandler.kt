package com.emarsys.core.clipboard

import android.content.ClipData
import android.content.ClipboardManager

class AndroidClipboardHandler(private val clipboardManager: ClipboardManager) :
    ClipboardHandlerApi {

    companion object {
        const val TEXT_LABEL = "copiedFromInapp"
    }

    override suspend fun copyToClipboard(text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText(TEXT_LABEL, text))
    }

}