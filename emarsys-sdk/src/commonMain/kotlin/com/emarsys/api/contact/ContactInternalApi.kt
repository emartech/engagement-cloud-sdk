package com.emarsys.api.contact

interface ContactInternalApi {

    suspend fun linkContact(contactFieldId: Int, contactFieldValue: String)

    suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String)

    suspend fun unlinkContact()

}

