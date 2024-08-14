package com.statsup.domain

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.TimeZone

fun formatLocal(date: ZonedDateTime): String {
    val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withLocale(Locale.getDefault())
    return date.withZoneSameInstant(TimeZone.getDefault().toZoneId()).format(dateFormatter)
}
