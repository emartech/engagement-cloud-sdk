package com.emarsys.core.providers

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

internal class TimestampProvider: InstantProvider {
    @OptIn(ExperimentalTime::class)
    override fun provide(): Instant {
        return Clock.System.now()
    }
}