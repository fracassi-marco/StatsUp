package com.statsup

import org.joda.time.DateTime


class Activities(
    private val activities: List<Activity>,
    private val provider: (List<Activity>) -> Double
) {
    private var monthPosition = -1
    private var yearPosition = -1

    fun filterByMonth(position: Int): Activities {
        monthPosition = position
        return this
    }

    fun filterByYear(position: Int): Activities {
        yearPosition = position
        return this
    }

    // 0 vecchio
    // N nuovo
    fun month(): Month {
        return months()[monthPosition]
    }

    // 0 vecchio
    // N nuovo
    fun year(): Year {
        return years()[yearPosition]
    }

    fun months(): List<Month> {
        val now = DateTime()
        val currentMonth = Month(now.year, now.monthOfYear)
        val firstMonth = Month(activities.last().date().year, activities.last().date().monthOfYear)
        val months = mutableListOf<Month>()
        var month = firstMonth
        while (month.isBeforeOrEqual(currentMonth)) {
            months.add(month)
            month = month.next()
        }
        return months
    }

    fun years(): List<Year> {
        val now = DateTime()
        val currentYear = Year(now.year)
        val firstMonth = Year(activities.last().date().year)
        val years = mutableListOf<Year>()
        var year = firstMonth
        while (year.isBeforeOrEqual(currentYear)) {
            years.add(year)
            year = year.next()
        }
        return years
    }

    fun isFirstMonth() = monthPosition == 0

    fun isFirstYear() = yearPosition == 0

    fun isCurrentMonth() = monthPosition == months().size - 1

    fun isCurrentYear() = yearPosition == years().size - 1

    fun total() = provider.invoke(selected())

    private fun selected(): List<Activity> {
        if (monthPosition != -1) {
            return ofMonth(months()[monthPosition])
        }

        if (yearPosition != -1) {
            return ofYear(years()[yearPosition])
        }

        return activities
    }

    private fun ofMonth(month: Month): List<Activity> {
        return activities.filter {
            it.date().year == month.year && it.date().monthOfYear == month.monthOfYear
        }
    }

    private fun ofYear(year: Year): List<Activity> {
        return activities.filter {
            it.date().year == year.year
        }
    }

    fun average(): Double {
        if (monthPosition != -1)
            return total() / month().numberOfDays()
        if (yearPosition != -1)
            return total() / year().numberOfMonths()

        return total() / months().size
    }

    fun byDay(): List<Double> {
        return activitiesByDay().map(provider)
    }

    fun byMonth(): List<Double> {
        return activitiesByMonth().map(provider)
    }

    fun byYear(): Map<Int, Double> {
        return activitiesByYear().map {it.key to provider(it.value)}.toMap().toSortedMap()
    }

    private fun activitiesByDay(): List<List<Activity>> {
        val byDay = selected().groupBy { it.date().dayOfMonth }
        return (1..month().maxNumberOfDays()).map {
            byDay[it] ?: emptyList()
        }
    }

    private fun activitiesByMonth(): List<List<Activity>> {
        val byMonth = selected().groupBy { it.date().monthOfYear }
        return (1..year().maxNumberOfMonths()).map {
            byMonth[it] ?: emptyList()
        }
    }

    private fun activitiesByYear(): Map<Int, List<Activity>> {
        val byYear = selected().groupBy { it.date().year }
        val minYear = byYear.minBy { it.key }!!.key
        val maxYear = byYear.maxBy { it.key }!!.key
        return (minYear..maxYear).map {
            it to (byYear[it] ?: emptyList())
        }.toMap().toSortedMap()
    }

    fun maxByDay(): Double {
        return months()
            .map { month ->
                ofMonth(month).groupBy { it.date().dayOfMonth }.values.map { provider(it) }.max()
                    ?: 0.0
            }.max() ?: 0.0
    }

    fun maxByMonth(): Double {
        return years()
            .map { year ->
                ofYear(year).groupBy { it.date().monthOfYear() }.values.map { provider(it) }.max()
                    ?: 0.0
            }.max() ?: 0.0
    }

    fun maxByYear(): Double {
        return activitiesByYear().map { provider(it.value) }.max() ?: 0.0
    }

    fun addMonths(amount: Int) =Activities(activities, provider).filterByMonth(monthPosition + amount)

    fun addYears(amount: Int) = Activities(activities, provider).filterByYear(yearPosition + amount)

    fun cumulativeByDay(): List<Double> {
        val byDay = byDay()
        var currentMonthTotal = 0.0
        return (0..30).map { i ->
            if (i < byDay.size)
                currentMonthTotal += byDay[i]

            currentMonthTotal
        }
    }

    fun cumulativeByMonth(): List<Double> {
        val byMonth = byMonth()
        var total = 0.0
        return (0..11).map { i ->
            if (i < byMonth.size)
                total += byMonth[i]

            total
        }
    }

    fun byDayOfWeek(): Map<DayOfWeek, Double> {
        val byDay = selected().groupBy { it.date().dayOfWeek }

        return DayOfWeek.values().map { day ->
            day to provider(byDay.getOrElse(day.index) { emptyList() })
        }.toMap()
    }
}
