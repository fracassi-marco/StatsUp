package com.statsup

class Distances(private val activities: Activities) : Value {
    override fun average(): Float {
        return activities
            .byMonth()
            .map { it.sumByDouble { activity -> activity.distanceInKilometers() }.toFloat() }
            .average()
            .toFloat()
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

    override fun averageOfYear(position: Int): Float {
        return activities
            .ofYearInPosition(position)
            .values
            .map { it.sumByDouble { activity -> activity.distanceInKilometers() } }
            .average()
            .toFloat()
    }

    override fun totalOfYear(position: Int): Float {
        return activities
            .ofYearInPosition(position)
            .values
            .sumByDouble { it.sumByDouble { activity -> activity.distanceInKilometers() } }
            .toFloat()
    }
}