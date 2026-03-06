package com.sap.ec.mobileengage.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MetaData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class EmbeddedMessagingContext(
    frequencyCapSeconds: Int = 5
) : EmbeddedMessagingContextApi {
    private val _metaData = MutableStateFlow<MetaData?>(null)
    override val metaData: StateFlow<MetaData?> = _metaData.asStateFlow()

    override var embeddedMessagingFrequencyCapSeconds: Int = frequencyCapSeconds

    override fun setMetaData(data: MetaData?) {
        _metaData.value = data
    }
}
