package com.emarsys.mobileengage.inapp

interface InAppViewApi {

    suspend fun load(message: InAppMessage)

}
