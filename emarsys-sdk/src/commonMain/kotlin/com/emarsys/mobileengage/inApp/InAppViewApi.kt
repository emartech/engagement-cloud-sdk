package com.emarsys.mobileengage.inApp

interface InAppViewApi {

    suspend fun load(message: InAppMessage)

}
