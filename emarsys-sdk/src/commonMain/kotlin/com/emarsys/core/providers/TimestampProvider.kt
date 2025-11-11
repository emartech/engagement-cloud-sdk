package com.emarsys.core.providers


import kotlinx.datetime.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Instant

internal class TimestampProvider: InstantProvider {
    @OptIn(ExperimentalTime::class)
    override fun provide(): Instant {
        return Clock.System.now()
    }
}