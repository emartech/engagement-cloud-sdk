package com.sap.ec.core.actions.clipboard

interface ClipboardHandlerApi {

    suspend fun copyToClipboard(text: String)

}