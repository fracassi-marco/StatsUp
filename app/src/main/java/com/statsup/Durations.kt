package com.statsup

class Durations(private val activities: Activities) : Value {

    private val provider =
        { list: List<Activity> -> list.sumByDouble { activity -> activity.durationInHours() } }

    override fun average(): Double {
        return activities.average(provider)
    }

    override fun cumulativeOfCurrentMont(): List<Double> {
        return activities.cumulativeOfCurrentMont(provider)
    }

    override fun cumulativeOfPreviousMont(): List<Double> {
        return activities.cumulativeOfPreviousMont(provider)
    }

    override fun yearInPosition(position: Int) = activities.yearInPosition(position)

    override fun years(): List<Int> = activities.years()

    override fun groupByDayOfWeek(): Map<DayOfWeek, Double> {
        return activities.groupByDayOfWeek(provider)
    }

    override fun max(): Double {
        return activities.max(provider)
    }

    override fun ofYear(position: Int): List<Double> {
        return activities.ofYear(position, provider)
    }

    override fun averageOfYear(position: Int): Double {
        return activities.averageOfYear(position, provider)
    }

    override fun total() = activities.total(provider)

    override fun filter(year: Int, month: Int): Value {
        return Durations(activities.filter(year, month))
    }

    override fun totalOfYear(position: Int): Double {
        return activities.totalOfYear(position, provider)
    }

}