package com.emarsys.api.contact

import com.emarsys.api.contact.ContactCall.LinkAuthenticatedContact
import com.emarsys.api.contact.ContactCall.LinkContact
import com.emarsys.api.contact.ContactCall.UnlinkContact
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.collections.dequeue
import com.emarsys.core.log.Logger
import com.emarsys.event.SdkEvent

internal class ContactInternal(
    private val contactContext: ContactContextApi,
    private val sdkLogger: Logger,
    private val sdkEventDistributor: SdkEventDistributorApi
) : ContactInstance {
    override suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        sdkLogger.debug("ContactInternal - linkContact")
        sdkEventDistributor.registerEvent(
            SdkEvent.Internal.Sdk.LinkContact(
                contactFieldId = contactFieldId,
                contactFieldValue = contactFieldValue
            )
        )
    }

    override suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        sdkLogger.debug("ContactInternal - linkAuthenticatedContact")
        sdkEventDistributor.registerEvent(
            SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
                contactFieldId = contactFieldId,
                openIdToken = openIdToken
            )
        )
    }

    override suspend fun unlinkContact() {
        sdkLogger.debug("ContactInternal - linkContact")
        sdkEventDistributor.registerEvent(SdkEvent.Internal.Sdk.UnlinkContact())
    }

    override suspend fun activate() {
        sdkLogger.debug("ContactInternal - activate")
        contactContext.calls.dequeue {
            when (it) {
                is LinkContact -> sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Sdk.LinkContact(
                        contactFieldId = it.contactFieldId,
                        contactFieldValue = it.contactFieldValue
                    )
                )
                is LinkAuthenticatedContact -> sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
                        contactFieldId = it.contactFieldId,
                        openIdToken = it.openIdToken
                    )
                )
                is UnlinkContact -> sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Sdk.UnlinkContact()
                )
            }
        }
    }

}