package com.emarsys.core.providers

import kotlinx.datetime.Instant

internal interface InstantProvider: Provider<Instant> {
    override fun provide(): Instant
}