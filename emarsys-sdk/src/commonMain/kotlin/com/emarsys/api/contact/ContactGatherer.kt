package com.emarsys.api.contact

import com.emarsys.api.contact.ContactCall.LinkAuthenticatedContact
import com.emarsys.api.contact.ContactCall.LinkContact
import com.emarsys.api.contact.ContactCall.UnlinkContact
import com.emarsys.core.log.Logger

internal class ContactGatherer(val context: ContactContextApi, private val sdkLogger: Logger) :
    ContactInstance {
    override suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        sdkLogger.debug("ContactGatherer - linkContact")
        context.calls.add(LinkContact(contactFieldId, contactFieldValue))
    }

    override suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        sdkLogger.debug("ContactGatherer - linkAuthenticatedContact")
        context.calls.add(LinkAuthenticatedContact(contactFieldId, openIdToken))
    }

    override suspend fun unlinkContact() {
        sdkLogger.debug("ContactGatherer - unlinkContact")
        context.calls.add(UnlinkContact())
    }

    override suspend fun activate() {
        sdkLogger.debug("ContactGatherer - activate")
    }

}