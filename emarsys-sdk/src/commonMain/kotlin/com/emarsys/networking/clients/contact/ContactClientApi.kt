package com.emarsys.networking.clients.contact

import com.emarsys.core.networking.model.Response

interface ContactClientApi {

    suspend fun linkContact(
        contactFieldId: Int,
        contactFieldValue: String? = null,
        openIdToken: String? = null
    ): Response

    suspend fun unLinkContact(): Response
}