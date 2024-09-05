package com.emarsys.mobileengage.inApp

interface InAppHandlerApi {

    suspend fun handle(html: String)

}