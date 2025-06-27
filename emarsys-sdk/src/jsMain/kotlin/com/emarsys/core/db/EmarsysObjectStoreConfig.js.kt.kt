package com.emarsys.core.db

import com.emarsys.event.SdkEvent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

sealed interface EmarsysObjectStoreConfig<T> {
    val name: String
    val serializer: KSerializer<T>

    data object Events : EmarsysObjectStoreConfig<SdkEvent> {
        override val name = "events"
        override val serializer = SdkEvent.serializer()
    }

    data object ClientId : EmarsysObjectStoreConfig<String> {
        override val name = "clientId"
        override val serializer = String.serializer()
    }
}
