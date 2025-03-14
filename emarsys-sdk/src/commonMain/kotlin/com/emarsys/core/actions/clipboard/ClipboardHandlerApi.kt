package com.emarsys.core.actions.clipboard

interface ClipboardHandlerApi {

    suspend fun copyToClipboard(text: String)

}