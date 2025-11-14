package com.emarsys.core.providers

import kotlin.time.Instant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal interface InstantProvider: Provider<Instant> {
    override fun provide(): Instant
}