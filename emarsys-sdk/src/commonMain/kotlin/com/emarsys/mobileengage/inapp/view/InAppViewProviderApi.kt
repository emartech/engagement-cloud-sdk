package com.emarsys.mobileengage.inapp.view

interface InAppViewProviderApi {
    suspend fun provide(): InAppViewApi
}
