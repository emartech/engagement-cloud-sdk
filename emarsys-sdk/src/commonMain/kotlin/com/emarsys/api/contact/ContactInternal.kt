package com.emarsys.api.contact

import com.emarsys.api.contact.ContactCall.LinkAuthenticatedContact
import com.emarsys.api.contact.ContactCall.LinkContact
import com.emarsys.api.contact.ContactCall.UnlinkContact
import com.emarsys.core.collections.dequeue
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.contact.ContactClientApi

internal class ContactInternal(
    private val contactClient: ContactClientApi,
    private val contactContext: ContactContextApi,
    private val sdkLogger: Logger
) : ContactInstance {
    override suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        sdkLogger.debug("ContactInternal - linkContact")
        contactClient.linkContact(contactFieldId, contactFieldValue, null)
    }

    override suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        sdkLogger.debug("ContactInternal - linkContact")
        contactClient.linkContact(contactFieldId, null, openIdToken)
    }

    override suspend fun unlinkContact() {
        sdkLogger.debug("ContactInternal - linkContact")
        contactClient.unlinkContact()
    }

    override suspend fun activate() {
        sdkLogger.debug("ContactInternal - activate")
        contactContext.calls.dequeue {
            when(it) {
                is LinkContact -> contactClient.linkContact(it.contactFieldId, it.contactFieldValue, null)
                is LinkAuthenticatedContact -> contactClient.linkContact(it.contactFieldId, null, it.openIdToken)
                is UnlinkContact -> contactClient.unlinkContact()
            }
        }
    }

}