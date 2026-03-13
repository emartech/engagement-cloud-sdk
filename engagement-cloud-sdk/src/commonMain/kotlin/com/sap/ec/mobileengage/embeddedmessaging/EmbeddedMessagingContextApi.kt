package com.sap.ec.mobileengage.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MetaData
import kotlinx.coroutines.flow.StateFlow

internal interface EmbeddedMessagingContextApi {
    val metaData: StateFlow<MetaData?>
    var embeddedMessagingFrequencyCapSeconds: Int
    var tagUpdateBatchSize: Int
    var tagUpdateFrequencyCapSeconds: Int

    fun setMetaData(data: MetaData?)
}