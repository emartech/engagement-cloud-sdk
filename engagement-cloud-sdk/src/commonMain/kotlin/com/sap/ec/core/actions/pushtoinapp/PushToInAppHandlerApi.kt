package com.sap.ec.core.actions.pushtoinapp

internal interface PushToInAppHandlerApi {

    suspend fun handle(url: String)
}