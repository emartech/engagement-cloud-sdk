package com.emarsys.api.contact

import com.emarsys.api.contact.ContactCall.LinkAuthenticatedContact
import com.emarsys.api.contact.ContactCall.LinkContact
import com.emarsys.api.contact.ContactCall.UnlinkContact
import com.emarsys.api.generic.ApiContext

class ContactGatherer(val context: ApiContext<ContactCall>): ContactInstance {
    override suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        context.calls.add(LinkContact(contactFieldId, contactFieldValue))
    }

    override suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        context.calls.add(LinkAuthenticatedContact(contactFieldId, openIdToken))
    }

    override suspend fun unlinkContact() {
        context.calls.add(UnlinkContact())
    }

    override suspend fun activate() {
    }

}