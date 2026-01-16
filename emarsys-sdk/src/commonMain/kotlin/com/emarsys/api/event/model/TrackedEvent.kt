package com.emarsys.api.event.model

import com.emarsys.event.SdkEvent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalSerializationApi::class)
sealed interface TrackedEvent {
    @OptIn(ExperimentalTime::class)
    fun toSdkEvent(uuid: String, timestamp: Instant): SdkEvent
}

