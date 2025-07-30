package com.emarsys.core.actions.pushtoinapp

interface PushToInAppHandlerApi {

    suspend fun handle(url: String)
}