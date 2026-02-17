package com.sap.ec.mobileengage.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MetaData

internal interface EmbeddedMessagingContextApi {
    var metaData: MetaData?
    var embeddedMessagingFrequencyCapSeconds: Int
}