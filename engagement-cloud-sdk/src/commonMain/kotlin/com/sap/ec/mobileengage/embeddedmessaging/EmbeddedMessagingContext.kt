package com.sap.ec.mobileengage.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MetaData

internal class EmbeddedMessagingContext(
    override var metaData: MetaData? = null,
    override var embeddedMessagingFrequencyCapSeconds: Int = 5
) : EmbeddedMessagingContextApi