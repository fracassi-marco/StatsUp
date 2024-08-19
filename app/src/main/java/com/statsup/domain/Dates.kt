package com.statsup.domain

import java.time.Month
import java.time.Year
import java.time.ZonedDateTime

fun Year.isCurrent(now: ZonedDateTime = ZonedDateTime.now()) = value == now.year

fun Month.isCurrent(now: ZonedDateTime = ZonedDateTime.now()) = this == now.month

fun ZonedDateTime.isCurrentMonth(now: ZonedDateTime = ZonedDateTime.now()): Boolean {
    return this.month == now.month && this.year == now.year
}