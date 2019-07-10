package com.statsup

class Durations(private val activities: Activities) : Value {
    override fun average(): Double {
        return activities.average { it.sumByDouble { activity -> activity.durationInHours() } }
    }

    override fun max(): Float {
        return activities
            .byMonth()
            .map { it.sumByDouble { activity -> activity.durationInHours() }.toFloat() }
            .max()!!
    }

    override fun ofYear(position: Int): List<Float> {
        return activities
            .ofYearInPosition(position)
            .values
            .map { it.sumByDouble { activity -> activity.durationInHours() }.toFloat() }
    }

    override fun averageOfYear(position: Int): Double {
        return activities.averageOfYear(position) { it.sumByDouble { activity -> activity.durationInHours() } }
    }

    override fun totalOfYear(position: Int): Double {
        return activities.totalOfYear(position) { it.sumByDouble { activity -> activity.durationInHours() } }
    }

}