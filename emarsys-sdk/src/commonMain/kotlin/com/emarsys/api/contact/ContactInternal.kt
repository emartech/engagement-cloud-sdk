package com.emarsys.api.contact

import com.emarsys.api.contact.ContactCall.LinkAuthenticatedContact
import com.emarsys.api.contact.ContactCall.LinkContact
import com.emarsys.api.contact.ContactCall.UnlinkContact
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.collections.dequeue
import com.emarsys.core.log.Logger
import com.emarsys.event.SdkEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ContactInternal(
    private val contactContext: ContactContextApi,
    private val sdkLogger: Logger,
    private val sdkEventDistributor: SdkEventDistributorApi
) : ContactInstance {
    override suspend fun link(contactFieldValue: String) {
        sdkLogger.debug("ContactInternal - linkContact")
        sdkEventDistributor.registerEvent(
            SdkEvent.Internal.Sdk.LinkContact(
                contactFieldValue = contactFieldValue
            )
        )
    }

    override suspend fun linkAuthenticated(openIdToken: String) {
        sdkLogger.debug("ContactInternal - linkAuthenticatedContact")
        sdkEventDistributor.registerEvent(
            SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
                openIdToken = openIdToken
            )
        )
    }

    override suspend fun unlink() {
        sdkLogger.debug("ContactInternal - linkContact")
        sdkEventDistributor.registerEvent(SdkEvent.Internal.Sdk.UnlinkContact())
    }

    override suspend fun activate() {
        sdkLogger.debug("ContactInternal - activate")
        contactContext.calls.dequeue {
            when (it) {
                is LinkContact -> sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Sdk.LinkContact(
                        contactFieldValue = it.contactFieldValue
                    )
                )
                is LinkAuthenticatedContact -> sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
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