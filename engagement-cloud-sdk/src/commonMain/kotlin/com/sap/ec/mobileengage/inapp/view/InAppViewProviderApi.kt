package com.sap.ec.mobileengage.inapp.view

internal interface InAppViewProviderApi {
    suspend fun provide(): InAppViewApi
}
