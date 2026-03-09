package com.sap.ec.core.datetime

internal expect fun Long.asLocaleFormattedHoursAndMinutes(): String

internal expect fun Long.asLocaleFormattedMonthsAndDays(): String

internal expect fun Long.asLocaleFormattedFullDate(): String