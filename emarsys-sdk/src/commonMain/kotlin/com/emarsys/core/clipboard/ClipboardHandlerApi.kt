package com.emarsys.core.clipboard

interface ClipboardHandlerApi {

    suspend fun copyToClipboard(text: String)

}