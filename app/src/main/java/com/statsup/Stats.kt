package com.statsup

fun List<Activity>.cumulativeByDay(month: Month, provider: (List<Activity>) -> Double): List<Double> {
    val byDay = activitiesByDay(month, this.filter { Month(it.date()) == month }).map(provider)
    var currentMonthTotal = 0.0

    return (0 until month.numberOfDays()).map { i ->
        if (i < byDay.size)
            currentMonthTotal += byDay[i]

        currentMonthTotal
    }
}

private fun activitiesByDay(month: Month, list: List<Activity>): List<List<Activity>> {
    val byDay = list.groupBy { it.date().dayOfMonth }
    return (1..month.maxNumberOfDays()).map {
        byDay[it] ?: emptyList()
    }
}
