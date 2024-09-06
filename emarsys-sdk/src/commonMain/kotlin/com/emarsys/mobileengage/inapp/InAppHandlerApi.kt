package com.emarsys.mobileengage.inapp

interface InAppHandlerApi {

    suspend fun handle(html: String)

}