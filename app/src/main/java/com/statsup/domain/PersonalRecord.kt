package com.statsup.domain

import java.time.ZonedDateTime

data class PersonalRecord(
    val label: String,
    val emoji: String,
    val formattedValue: String,
    val date: ZonedDateTime,
    val activityName: String
)
