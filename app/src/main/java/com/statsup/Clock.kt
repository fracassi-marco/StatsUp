package com.statsup

import java.util.*

object Clock {
    fun currentYear() = GregorianCalendar().get(Calendar.YEAR)

    fun currentMonth() = GregorianCalendar().get(Calendar.MONTH) + 1
}