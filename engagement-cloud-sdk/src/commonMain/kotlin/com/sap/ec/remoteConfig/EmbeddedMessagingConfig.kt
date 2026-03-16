package com.sap.ec.remoteConfig

import kotlinx.serialization.Serializable

@Serializable
internal data class EmbeddedMessagingConfig(
    val tagUpdateBatchSize: Int? = null,
    val tagUpdateFrequencyCapSeconds: Int? = null
)
