package com.emarsys.api.contact

import com.emarsys.api.contact.ContactCall.LinkAuthenticatedContact
import com.emarsys.api.contact.ContactCall.LinkContact
import com.emarsys.api.contact.ContactCall.UnlinkContact
import com.emarsys.api.generic.ApiContext
import com.emarsys.networking.clients.contact.ContactClientApi

class ContactInternal(
    private val contactClient: ContactClientApi,
    private val contactContext: ApiContext<ContactCall>
) : ContactInstance {
    override suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        contactClient.linkContact(contactFieldId, contactFieldValue, null)
    }

    override suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        contactClient.linkContact(contactFieldId, null, openIdToken)
    }

    override suspend fun unlinkContact() {
        contactClient.unlinkContact()
    }

    override suspend fun activate() {
        val iterator = contactContext.calls.iterator()
        while (iterator.hasNext()) {
            when(val call = iterator.next()) {
                is LinkContact -> contactClient.linkContact(call.contactFieldId, call.contactFieldValue)
                is LinkAuthenticatedContact -> contactClient.linkContact(call.contactFieldId, null, call.openIdToken)
                is UnlinkContact -> contactClient.unlinkContact()
            }
            iterator.remove()
        }
    }

}