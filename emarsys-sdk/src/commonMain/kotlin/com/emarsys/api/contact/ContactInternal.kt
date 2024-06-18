package com.emarsys.api.contact

import com.emarsys.api.contact.ContactCall.LinkAuthenticatedContact
import com.emarsys.api.contact.ContactCall.LinkContact
import com.emarsys.api.contact.ContactCall.UnlinkContact
import com.emarsys.api.generic.ApiContext
import com.emarsys.core.collections.dequeue
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
        contactContext.calls.dequeue {
            when(it) {
                is LinkContact -> contactClient.linkContact(it.contactFieldId, it.contactFieldValue, null)
                is LinkAuthenticatedContact -> contactClient.linkContact(it.contactFieldId, null, it.openIdToken)
                is UnlinkContact -> contactClient.unlinkContact()
            }
        }
    }

}