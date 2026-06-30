package com.statsup.domain

import android.text.format.DateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

fun formatLocal(date: ZonedDateTime): String {
    val locale = Locale.getDefault()
    val local = date.withZoneSameInstant(TimeZone.getDefault().toZoneId())
    // getBestDateTimePattern handles day/month order and 12h vs 24h per locale
    val datePart = local.format(DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "dMMM"), locale))
    val timePart = local.format(DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "Hm"), locale))
    return "$datePart · $timePart"
}
