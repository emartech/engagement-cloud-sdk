package com.emarsys.providers

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class TimestampProvider: Provider<Instant> {
    override fun provide(): Instant {
        return Clock.System.now()
    }
}