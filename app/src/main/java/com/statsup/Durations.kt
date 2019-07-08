package com.statsup

class Durations(private val activities: Activities) : Value {
    override fun average(): Float {
        return activities
            .byMonth()
            .map { it.sumByDouble { activity -> activity.durationInHours() }.toFloat() }
            .average()
            .toFloat()
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

    override fun averageOfYear(position: Int): Float {
        return activities
            .ofYearInPosition(position)
            .values
            .map { it.sumByDouble { activity -> activity.durationInHours() } }
            .average()
            .toFloat()
    }

    override fun totalOfYear(position: Int): Float {
        return activities
            .ofYearInPosition(position)
            .values
            .sumByDouble { it.sumByDouble { activity -> activity.durationInHours() } }
            .toFloat()
    }

}