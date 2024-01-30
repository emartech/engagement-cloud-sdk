package com.emarsys.api.contact

import com.emarsys.api.generic.ApiContext
import kotlinx.serialization.Serializable

class ContactContext(override val calls: MutableList<ContactCall>) : ApiContext<ContactCall>

@Serializable
sealed interface ContactCall {
    @Serializable
    data class LinkContact(val contactFieldId: Int, val contactFieldValue: String) : ContactCall

    @Serializable
    data class LinkAuthenticatedContact(val contactFieldId: Int, val openIdToken: String) :
        ContactCall

    @Serializable
    class UnlinkContact: ContactCall {
        override fun equals(other: Any?): Boolean {
            return other is UnlinkContact
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }
}