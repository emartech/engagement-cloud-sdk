package com.emarsys.remoteConfig

import kotlinx.serialization.Serializable

@Serializable
data class RemoteConfigFeatures(
    val mobileEngage: Boolean? = null,
    val predict: Boolean? = null
)
