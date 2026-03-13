package com.sap.ec.api.contact

import com.sap.ec.api.contact.ContactCall.LinkAuthenticatedContact
import com.sap.ec.api.contact.ContactCall.LinkContact
import com.sap.ec.api.contact.ContactCall.UnlinkContact
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger

internal class ContactGatherer(
    val context: ContactContextApi,
    val sdkContext: SdkContextApi,
    private val sdkLogger: Logger
) :
    ContactInstance {
    override suspend fun link(contactFieldValue: String) {
        sdkLogger.debug("ContactGatherer - linkContact")
        context.calls.add(LinkContact(contactFieldValue))
    }

    override suspend fun linkAuthenticated(openIdToken: String) {
        sdkLogger.debug("ContactGatherer - linkAuthenticatedContact")
        context.calls.add(LinkAuthenticatedContact(openIdToken))
    }

    override suspend fun unlink() {
        sdkLogger.debug("ContactGatherer - unlinkContact")
        context.calls.add(UnlinkContact(sdkContext.getSdkConfig()?.applicationCode))
    }

    override suspend fun activate() {
        sdkLogger.debug("ContactGatherer - activate")
    }

}