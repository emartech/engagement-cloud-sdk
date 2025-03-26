package com.emarsys.core.providers

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class TimestampProvider: InstantProvider {
    override fun provide(): Instant {
        return Clock.System.now()
    }
}