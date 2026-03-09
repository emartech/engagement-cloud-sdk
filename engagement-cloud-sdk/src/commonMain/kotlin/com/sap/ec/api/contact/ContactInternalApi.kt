package com.sap.ec.api.contact


internal interface ContactInternalApi {

    suspend fun link(contactFieldValue: String)

    suspend fun linkAuthenticated(openIdToken: String)

    suspend fun unlink()

}

