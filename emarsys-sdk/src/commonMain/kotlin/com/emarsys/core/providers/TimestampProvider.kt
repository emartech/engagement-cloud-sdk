package com.emarsys.core.providers


import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal class TimestampProvider: InstantProvider {
    @OptIn(ExperimentalTime::class)
    override fun provide(): Instant {
        return Clock.System.now()
    }
}