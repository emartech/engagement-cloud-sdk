package com.sap.ec.api.event.model

import com.sap.ec.event.SdkEvent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalSerializationApi::class)
sealed interface TrackedEvent {
    @OptIn(ExperimentalTime::class)
    fun toSdkEvent(uuid: String, timestamp: Instant): SdkEvent
}

