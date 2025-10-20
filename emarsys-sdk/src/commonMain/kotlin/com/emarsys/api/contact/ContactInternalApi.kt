package com.emarsys.api.contact

interface ContactInternalApi {

    suspend fun link(contactFieldId: Int, contactFieldValue: String)

    suspend fun linkAuthenticated(contactFieldId: Int, openIdToken: String)

    suspend fun unlink()

}

