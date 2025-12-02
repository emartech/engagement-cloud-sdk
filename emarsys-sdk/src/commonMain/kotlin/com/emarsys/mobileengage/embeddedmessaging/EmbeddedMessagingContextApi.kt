package com.emarsys.mobileengage.embeddedmessaging

import com.emarsys.networking.clients.embedded.messaging.model.MetaData

internal interface EmbeddedMessagingContextApi {
    var metaData: MetaData?
    var embeddedMessagingFrequencyCapSeconds: Int
}