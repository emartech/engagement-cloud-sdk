package com.sap.ec.remoteConfig

import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteConfigFeatures(
    val mobileEngage: Boolean? = null,
    val embeddedMessaging: Boolean? = null,
    val jsBridgeSignatureCheck: Boolean? = null,
)
