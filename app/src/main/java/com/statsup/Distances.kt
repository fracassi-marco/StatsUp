package com.statsup

class Distances(private val activities: Activities) : Value {
    override fun average(): Double {
        return activities.average { it.sumByDouble { activity -> activity.distanceInKilometers() } }
    }

    override fun max(): Float {
        return activities
            .byMonth()
            .map { it.sumByDouble { activity -> activity.distanceInKilometers() }.toFloat() }
            .max()!!
    }

    override fun ofYear(position: Int): List<Float> {
        return activities
            .ofYearInPosition(position)
            .values
            .map { it.sumByDouble { activity -> activity.distanceInKilometers() }.toFloat() }
    }

    override fun averageOfYear(position: Int): Double {
        return activities.averageOfYear(position) { it.sumByDouble { activity -> activity.distanceInKilometers() } }
    }

    override fun totalOfYear(position: Int): Double {
        return activities
            .ofYearInPosition(position)
            .values
            .sumByDouble { it.sumByDouble { activity -> activity.distanceInKilometers() } }
    }
}