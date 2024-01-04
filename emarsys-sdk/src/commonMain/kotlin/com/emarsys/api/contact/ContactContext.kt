package com.emarsys.api.contact

import com.emarsys.api.generic.ApiContext

class ContactContext: ApiContext<ContactCall> {

    override var calls = mutableListOf<ContactCall>()
}

sealed interface ContactCall {
    data class LinkContact(val contactFieldId: Int, val contactFieldValue: String): ContactCall
    data class LinkAuthenticatedContact(val contactFieldId: Int, val openIdToken: String): ContactCall
    class UnlinkContact(): ContactCall {
        override fun equals(other: Any?): Boolean {
            return other is UnlinkContact
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }
}