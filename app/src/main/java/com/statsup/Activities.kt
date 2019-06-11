package com.statsup

class Activities(private val activities: List<Activity>) {

    fun ofYearInPosition(position: Int): Map<Month, List<Activity>> {
        val year = yearInPosition(position)
        return byMonth(activities.filter { it.date().year().get() == year })
    }

    fun yearInPosition(position: Int) = years()[position]

    fun maxMonthlyFrequency(): Float {
        return byMonth(activities).values.map {
            it.size
        }.max()!!.toFloat()
    }

    fun maxMonthlyDistance(): Float {
        return byMonth(activities).values.map {
            it.sumByDouble { it.distanceInKilometers() }
        }.max()!!.toFloat()
    }

    fun maxMonthlyDuration(): Float {
        return byMonth(activities).values.map {
            it.sumByDouble { it.durationInHours() }
        }.max()!!.toFloat()
    }

    fun averageMonthlyFrequency() : Float {
        return byMonth(activities).values.map {
            it.size
        }.average().toFloat()
    }

    fun averageMonthlyDistance() : Float {
        return byMonth(activities).values.map {
            it.sumByDouble { it.distanceInKilometers() }
        }.average().toFloat()
    }

    fun averageMonthlyDuration() : Float {
        return byMonth(activities).values.map {
            it.sumByDouble { it.durationInHours() }
        }.average().toFloat()
    }

    fun years(): List<Int> {
        return activities
            .map { it.date().year().get() }
            .distinct()
            .sorted()
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
        return activities.groupBy { it.date().dayOfWeek }
    }
}
