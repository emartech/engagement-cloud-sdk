package com.sap.ec.mobileengage.inapp.view

interface InAppViewProviderApi {
    suspend fun provide(): InAppViewApi
}
