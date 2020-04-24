package com.statsup

import org.joda.time.DateTime
import java.util.*
import java.util.Calendar.*

data class Year(val year: Int) {
    fun asString() = year.toString()

    fun isBeforeOrEqual(other: Year): Boolean {
        if(other.year >= year)
            return true

        return false
    }

    fun next() = Year(year + 1)

    fun previous() = Year(year - 1)

    fun numberOfMonths(): Int {
        if(DateTime().year == year) {
            return DateTime().monthOfYear
        }

        return maxNumberOfMonths()
    }

    fun maxNumberOfMonths() = 12
}

data class Month(val year: Int, val monthOfYear: Int) {
    fun asString() = "${Months.labelOf(monthOfYear)} ${year}"

    fun previous(): Month {
        val previous = calendar().apply { add(MONTH, -1) }
        return build(previous)
    }

    fun next(): Month {
        val next = calendar().apply { add(MONTH, 1) }
        return build(next)
    }

    private fun build(previous: GregorianCalendar) =
        Month(previous.get(YEAR), previous.get(MONTH) + 1)

    fun isBeforeOrEqual(other: Month): Boolean {
        if(other.year > year)
            return true

        if(other.year == year && other.monthOfYear >= monthOfYear)
            return true

        return false
    }

    fun numberOfDays(): Int {
        if(DateTime().year == year && DateTime().monthOfYear == monthOfYear) {
            return DateTime().dayOfMonth
        }

        return maxNumberOfDays()
    }

    fun maxNumberOfDays() = calendar().getActualMaximum(DAY_OF_MONTH)

    private fun calendar() = GregorianCalendar(year, monthOfYear - 1, 1)
}