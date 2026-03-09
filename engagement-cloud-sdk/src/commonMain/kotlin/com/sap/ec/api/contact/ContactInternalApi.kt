package com.sap.ec.api.contact

import com.sap.ec.InternalSdkApi

@InternalSdkApi
interface ContactInternalApi {

    suspend fun link(contactFieldValue: String)

    suspend fun linkAuthenticated(openIdToken: String)

    suspend fun unlink()

}

