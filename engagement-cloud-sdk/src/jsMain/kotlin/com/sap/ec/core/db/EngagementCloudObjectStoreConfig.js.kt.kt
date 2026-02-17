package com.sap.ec.core.db

import com.sap.ec.event.SdkEvent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

sealed interface EngagementCloudObjectStoreConfig<T> {
    val name: String
    val serializer: KSerializer<T>

    data object Events : EngagementCloudObjectStoreConfig<SdkEvent> {
        override val name = "events"
        override val serializer = SdkEvent.serializer()
    }

    data object ClientId : EngagementCloudObjectStoreConfig<String> {
        override val name = "clientId"
        override val serializer = String.serializer()
    }
}
