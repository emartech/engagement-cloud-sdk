package com.sap.ec.remoteConfig

import kotlinx.serialization.Serializable

@Serializable
data class RemoteConfigFeatures(
    val mobileEngage: Boolean? = null
)
