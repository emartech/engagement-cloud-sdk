package com.emarsys.api.contact

import com.emarsys.api.contact.ContactCall.LinkAuthenticatedContact
import com.emarsys.api.contact.ContactCall.LinkContact
import com.emarsys.api.contact.ContactCall.UnlinkContact
import com.emarsys.core.collections.dequeue
import com.emarsys.core.log.Logger
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class ContactInternal(
    private val contactContext: ContactContextApi,
    private val sdkLogger: Logger,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>
) : ContactInstance {
    override suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        sdkLogger.debug("ContactInternal - linkContact")
        sdkEventFlow.emit(
            SdkEvent.Internal.Sdk.LinkContact(
                attributes = buildJsonObject {
                    put("contactFieldId", contactFieldId)
                    put("contactFieldValue", contactFieldValue)
                })
        )
    }

    override suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        sdkLogger.debug("ContactInternal - linkAuthenticatedContact")
        sdkEventFlow.emit(
            SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
                attributes = buildJsonObject {
                    put("contactFieldId", contactFieldId)
                    put("openIdToken", openIdToken)
                })
        )
    }

    override suspend fun unlinkContact() {
        sdkLogger.debug("ContactInternal - linkContact")
        sdkEventFlow.emit(SdkEvent.Internal.Sdk.UnlinkContact())
    }

    override suspend fun activate() {
        sdkLogger.debug("ContactInternal - activate")
        contactContext.calls.dequeue {
            when (it) {
                is LinkContact -> sdkEventFlow.emit(
                    SdkEvent.Internal.Sdk.LinkContact(
                        attributes = buildJsonObject {
                            put("contactFieldId", it.contactFieldId)
                            put("contactFieldValue", it.contactFieldValue)
                        })
                )

                is LinkAuthenticatedContact -> sdkEventFlow.emit(
                    SdkEvent.Internal.Sdk.LinkContact(
                        attributes = buildJsonObject {
                            put("contactFieldId", it.contactFieldId)
                            put("openIdToken", it.openIdToken)
                        })
                )

                is UnlinkContact -> sdkEventFlow.emit(
                    SdkEvent.Internal.Sdk.UnlinkContact()
                )
            }
        }
    }

}