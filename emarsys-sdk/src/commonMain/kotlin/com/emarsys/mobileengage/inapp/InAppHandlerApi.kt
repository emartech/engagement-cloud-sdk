package com.emarsys.mobileengage.inapp

interface InAppHandlerApi {

    suspend fun handle(campaignId:String, html: String)

}