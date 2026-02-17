package com.sap.ec.mobileengage.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import com.sap.ec.core.actions.clipboard.ClipboardHandlerApi

class AndroidClipboardHandler(private val clipboardManager: ClipboardManager) :
    ClipboardHandlerApi {

    companion object {
        const val TEXT_LABEL = "copiedFromInapp"
    }

    override suspend fun copyToClipboard(text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText(TEXT_LABEL, text))
    }

}