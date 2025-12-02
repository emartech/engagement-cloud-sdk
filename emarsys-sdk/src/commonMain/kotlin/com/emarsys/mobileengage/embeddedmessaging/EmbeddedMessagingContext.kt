package com.emarsys.mobileengage.embeddedmessaging

import com.emarsys.networking.clients.embedded.messaging.model.MetaData

internal class EmbeddedMessagingContext(
    override var metaData: MetaData? = null,
    override var embeddedMessagingFrequencyCapSeconds: Int = 5
) : EmbeddedMessagingContextApi