package com.sap.ec.mobileengage.inapp.jsbridge

interface ContentReplacerApi {

    suspend fun replace(content: String): String
}