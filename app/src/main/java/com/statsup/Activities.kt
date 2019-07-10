package com.statsup

class Activities(private val activities: List<Activity>) {

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

    fun all(): List<Activity> {
        return activities
    }
}
