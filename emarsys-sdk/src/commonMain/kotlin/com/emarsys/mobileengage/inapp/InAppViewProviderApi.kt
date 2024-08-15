package com.emarsys.mobileengage.inapp

interface InAppViewProviderApi {
    suspend fun provide(): InAppViewApi
}
