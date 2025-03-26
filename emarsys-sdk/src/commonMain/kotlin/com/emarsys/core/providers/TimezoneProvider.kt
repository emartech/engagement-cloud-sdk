package com.emarsys.core.providers

import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.offsetAt

internal class TimezoneProvider(private val timestampProvider: InstantProvider) : TimezoneProviderApi {
    private val offSetFormat = UtcOffset.Format { offsetHours(); offsetMinutesOfHour() }

    override fun provide(): String {
        val now = timestampProvider.provide()
        val timeZone = TimeZone.currentSystemDefault().offsetAt(now)
        return timeZone.format(offSetFormat)
    }
}