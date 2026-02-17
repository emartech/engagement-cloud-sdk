package com.sap.ec.core.actions.pushtoinapp

interface PushToInAppHandlerApi {

    suspend fun handle(url: String)
}