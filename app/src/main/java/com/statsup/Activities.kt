package com.statsup

import java.util.*
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR

class Activities(private val activities: List<Activity>, private val today: Calendar) {
    constructor(activities: List<Activity>) : this(activities, GregorianCalendar())

    private fun ofYearInPosition(position: Int): Map<Month, List<Activity>> {
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

    private fun byMonth(): List<List<Activity>> {
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

    fun average(value: (List<Activity>) -> Double): Double {
        val sum = byMonth()
            .sumByDouble { value.invoke(it) }
        return sum / numberOfMonths()
    }

    fun averageOfYear(position: Int, value: (List<Activity>) -> Double): Double {
        val sum = ofYearInPosition(position)
            .values
            .sumByDouble { value.invoke(it) }
        return sum / numberOfMonths(position)
    }

    fun totalOfYear(position: Int, value: (List<Activity>) -> Double): Double {
        return ofYearInPosition(position)
            .values
            .sumByDouble { value.invoke(it) }
    }

    fun total(value: (List<Activity>) -> Double): Double {
        return value.invoke(activities)
    }

    fun ofYear(position: Int, value: (List<Activity>) -> Double): List<Double> {
        return ofYearInPosition(position)
            .values
            .map { value.invoke(it) }
    }

    fun max(value: (List<Activity>) -> Double): Double {
        return byMonth()
            .map { value.invoke(it) }
            .max()!!
    }

    fun cumulativeOfCurrentMont(value: (List<Activity>) -> Double): List<Double> {
        return cumulativeOfMonth(12 - currentMonth(), value)
    }

    fun cumulativeOfPreviousMont(value: (List<Activity>) -> Double): List<Double> {
        return cumulativeOfMonth(13 - currentMonth(), value)
    }

    fun groupByDay(provider: (List<Activity>) -> Double): Map<Days, Double> {
        val byDay = activities.groupBy { it.date().dayOfWeek }

        return Days.values().map { day ->
            day to provider.invoke(byDay.getOrElse(day.index) { emptyList() })
        }.toMap()
    }

    private fun cumulativeOfMonth(index: Int, value: (List<Activity>) -> Double): List<Double> {
        val byMonth = byMonth()
        val currentMonthActivities = byMonth[byMonth.size - index]
        var currentMonthTotal = 0.0
        return (1..31).map { i ->
            currentMonthTotal += value.invoke(currentMonthActivities.filter { it.date().dayOfMonth == i })
            currentMonthTotal
        }
    }

    private fun numberOfMonths(): Int {
        return byMonth().size - (11 - currentMonth())
    }

    private fun numberOfMonths(position: Int): Int {
        if (yearInPosition(position) == actualYear()) {
            return currentMonth() + 1
        }
        return 12
    }

    private fun actualYear() = today.get(YEAR)

    private fun currentMonth() = today.get(MONTH)

}
