package com.sap.ec.config

import com.sap.ec.event.SdkEvent

sealed interface LinkContactData {
    fun toLinkContactEvent(): SdkEvent.Internal.Sdk.LinkContactEvent

    data class ContactFieldValueData(val contactFieldValue: String) : LinkContactData {
        override fun toLinkContactEvent(): SdkEvent.Internal.Sdk.LinkContactEvent =
            SdkEvent.Internal.Sdk.LinkContact(
                contactFieldValue = contactFieldValue
            )
    }

    data class OpenIdTokenData(val openIdToken: String) : LinkContactData {
        override fun toLinkContactEvent(): SdkEvent.Internal.Sdk.LinkContactEvent =
            SdkEvent.Internal.Sdk.LinkAuthenticatedContact(
                openIdToken = openIdToken
            )
    }
}
