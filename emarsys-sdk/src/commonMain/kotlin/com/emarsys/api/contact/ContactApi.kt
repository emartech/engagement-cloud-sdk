package com.emarsys.api.contact

interface ContactApi {

    suspend fun linkContact(contactFieldId: Int, contactFieldValue: String)

    suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String)

    suspend fun unlinkContact()

}

