package com.statsup

class Group(private val activities: List<Activity>) {

    fun byMonths() : Map<Month, List<Activity>> {

        if(activities.isEmpty()) {
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

    fun ofYear(year: Int): List<Activity> {
        return activities.filter { it.date().year().get() == year }
    }

    fun maxMonthlyFrequency(): Float {
        if(activities.isEmpty())
            return 0f

        return byMonths().values.map { it.size }.max()!!.toFloat()
    }

    fun averageMonthlyFrequency() : Float {
        if(activities.isEmpty())
            return 0f

        return byMonths().values.map { it.size }.average().toFloat()
    }

    fun years(): List<Int> {
        return activities
            .map { it.date().year().get() }
            .distinct()
            .sorted()
    }
}