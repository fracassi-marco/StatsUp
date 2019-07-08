package com.statsup

class Frequencies(private val activities: Activities) : Value {
    override fun average(): Float {
        return activities
            .byMonth()
            .map { it.size.toFloat() }
            .average()
            .toFloat()
    }

    override fun max(): Float {
        return activities
                .byMonth()
                .map { it.size.toFloat() }
                .max()!!
    }

    override fun ofYear(position: Int): List<Float> {
        return activities
            .ofYearInPosition(position)
            .values
            .map { it.size.toFloat() }
    }

    override fun averageOfYear(position: Int): Float {
        return activities
            .ofYearInPosition(position)
            .values
            .map { it.size }
            .average()
            .toFloat()
    }

    override fun totalOfYear(position: Int): Float {
        return activities
            .ofYearInPosition(position)
            .values
            .sumByDouble { it.size.toDouble() }
            .toFloat()
    }

}

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

interface Value {
    fun totalOfYear(position: Int): Float
    fun averageOfYear(position: Int): Float
    fun ofYear(position: Int): List<Float>
    fun max(): Float
    fun average(): Float
}
