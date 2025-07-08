package com.emarsys.core.providers

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
internal interface InstantProvider: Provider<Instant> {
    override fun provide(): Instant
}