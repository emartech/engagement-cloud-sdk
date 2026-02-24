@file:OptIn(ExperimentalJsExport::class)

package com.sap.ec.api.setup

import com.sap.ec.config.LinkContactData
import js.objects.Object

@JsExport
sealed external interface JsLinkContactData

@JsExport
external interface JsContactFieldValueData : JsLinkContactData {
    val contactFieldValue: String
}


@JsExport
external interface JsOpenIdTokenData : JsLinkContactData {
    val openIdToken: String
}

@OptIn(ExperimentalWasmJsInterop::class)
fun JsLinkContactData.toLinkContactData(): LinkContactData? {
    return if (Object.hasOwn(this, "contactFieldValue")) {
        LinkContactData.ContactFieldValueData(asDynamic().contactFieldValue)
    } else if (Object.hasOwn(this, "openIdToken")) {
        LinkContactData.OpenIdTokenData(asDynamic().openIdToken)
    } else null
}