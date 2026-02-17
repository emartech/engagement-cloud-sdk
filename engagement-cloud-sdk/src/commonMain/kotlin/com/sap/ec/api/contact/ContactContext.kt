package com.sap.ec.api.contact

import kotlinx.serialization.Serializable

internal class ContactContext(override val calls: MutableList<ContactCall>) : ContactContextApi

@Serializable
sealed interface ContactCall {
    @Serializable
    data class LinkContact(val contactFieldValue: String) : ContactCall

    @Serializable
    data class LinkAuthenticatedContact( val openIdToken: String) :
        ContactCall

    @Serializable
    class UnlinkContact : ContactCall {
        override fun equals(other: Any?): Boolean {
            return other is UnlinkContact
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }
}