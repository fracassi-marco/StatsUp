package com.statsup

import java.util.*

class Activities(private val activities: List<Activity>, private val today: Calendar) {
    constructor(activities: List<Activity>) : this(activities, GregorianCalendar())

    fun ofYearInPosition(position: Int): Map<Month, List<Activity>> {
        val year = yearInPosition(position)
        return byMonth(activities.filter { it.date().year().get() == year })
    }

    fun yearInPosition(position: Int) = years()[position]

    fun years(): List<Int> {
        return activities
            .map { it.date().year().get() }
            .distinct()
            .sorted()
    }

    fun byMonth(): List<List<Activity>> {
        return byMonth(activities).values.toList()
    }

    private fun byMonth(activities: List<Activity>): Map<Month, List<Activity>> {
        if (activities.isEmpty()) {
            return emptyMap()
        }

        val ordered = activities.sortedBy { it.dateInMillis }

        val result = mutableMapOf<Month, List<Activity>>()
        val oldest = ordered.first().date()
        val newest = ordered.last().date()

        for (year in oldest.year..newest.year) {
            for (month in 1..12) {
                val filter = ordered.filter {
                    it.date().year == year && it.date().monthOfYear == month
                }
                result[Month(year, month)] = filter
            }
        }

        return result
    }

    fun frequencyByDay(): Map<Int, List<Activity>> {
        return activities
            .groupBy { it.date().dayOfWeek }
            .toSortedMap()
    }

    fun count(): Int {
        return activities.size
    }

    fun average(value: (List<Activity>) -> Double) : Double {
        val sum = byMonth()
            .sumByDouble { value.invoke(it) }
        return sum / numberOfMonths()
    }

    fun averageOfYear(position: Int, value: (List<Activity>) -> Double) : Double {
        val sum = ofYearInPosition(position)
            .values
            .sumByDouble { value.invoke(it) }
        return sum / numberOfMonths(position)
    }

    private fun numberOfMonths(): Int {
        return byMonth().size - (11 - actualMonth())
    }

    private fun numberOfMonths(position: Int): Int {
        if (yearInPosition(position) == actualYear()) {
            return actualMonth() + 1
        }
        return 12
    }

    private fun actualYear() = today.get(Calendar.YEAR)

    private fun actualMonth() = today.get(Calendar.MONTH)
}
