package com.emarsys.mobileengage.embedded.messages

import com.emarsys.networking.clients.embedded.messaging.model.MetaData

internal class EmbeddedMessagingContext : EmbeddedMessagingContextApi {
    override var metaData: MetaData? = null
    override var embeddedMessagingFrequencyCapSeconds: Int = 5
}