package com.sap.ec.core.actions.clipboard

internal interface ClipboardHandlerApi {

    suspend fun copyToClipboard(text: String)

}