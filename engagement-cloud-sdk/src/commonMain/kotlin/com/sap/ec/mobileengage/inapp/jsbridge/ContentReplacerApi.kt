package com.sap.ec.mobileengage.inapp.jsbridge

internal interface ContentReplacerApi {

    suspend fun replace(content: String): String
}