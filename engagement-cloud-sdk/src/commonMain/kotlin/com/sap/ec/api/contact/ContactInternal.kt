package com.sap.ec.api.contact

import com.sap.ec.api.contact.ContactCall.LinkAuthenticatedContact
import com.sap.ec.api.contact.ContactCall.LinkContact
import com.sap.ec.api.contact.ContactCall.UnlinkContact
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.OperationalEventDistributorApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.crypto.CryptoApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ContactInternal(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val operationalEventDistributor: OperationalEventDistributorApi,
    private val sdkContext: SdkContextApi,
    private val threadSafePersistentStore: ThreadSafePersistentStoreApi<ContactCall>,
    private val requestContext: RequestContextApi,
    private val crypto: CryptoApi,
    private val sdkLogger: Logger
) : ContactInstance {
    override suspend fun link(contactFieldValue: String) {
        sdkLogger.debug("link")
        if (isAlreadyLinked(contactFieldValue)) {
            sdkLogger.debug("Contact already linked, skipping LinkContact.")
            return
        }
        sdkEventDistributor.registerEvent(
            SdkEvent.Internal.Sdk.LinkContact(
                contactFieldValue = contactFieldValue
            )
        )
    }

    override suspend fun linkAuthenticated(openIdToken: String) {
        sdkLogger.debug("linkAuthenticated")
        if (isAlreadyLinked(openIdToken)) {
            sdkLogger.debug("Contact already linked, skipping LinkAuthenticatedContact.")
            return
        }
        sdkEventDistributor.registerEvent(
            SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
                openIdToken = openIdToken
            )
        )
    }

    private suspend fun isAlreadyLinked(value: String): Boolean {
        return requestContext.isContactLinked == true &&
            crypto.hash(value) == requestContext.linkedContactHash
    }

    override suspend fun unlink() {
        sdkLogger.debug("unlink")
        if (requestContext.isContactLinked ?: false) {
            operationalEventDistributor.registerOperationalEvent(SdkEvent.Internal.Sdk.UnlinkContact(applicationCode = sdkContext.getSdkConfig()?.applicationCode))
        }
    }

    override suspend fun activate() {
        sdkLogger.debug("activate")
        threadSafePersistentStore.dequeue {
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

                is UnlinkContact -> operationalEventDistributor.registerOperationalEvent(
                    SdkEvent.Internal.Sdk.UnlinkContact(applicationCode = it.applicationCode)
                )
            }
        }
    }

}