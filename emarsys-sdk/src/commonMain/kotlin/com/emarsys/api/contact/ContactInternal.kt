package com.emarsys.api.contact

import com.emarsys.networking.clients.contact.ContactClientApi

class ContactInternal(private val contactClient: ContactClientApi) : ContactInstance {
    override suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        contactClient.linkContact(contactFieldId, contactFieldValue, null)
    }

    override suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        contactClient.linkContact(contactFieldId, null, openIdToken)
    }

    override suspend fun unlinkContact() {
        contactClient.unLinkContact()
    }

    override suspend fun activate() {
    }

}