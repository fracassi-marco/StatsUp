package com.statsup

class Frequencies(private val activities: Activities) : Value {

    override fun average(): Double {
        return activities.average { it.size.toDouble() }
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

    override fun averageOfYear(position: Int): Double {
        return activities.averageOfYear(position) { it.size.toDouble() }
    }

    override fun totalOfYear(position: Int): Double {
        return activities.totalOfYear(position) { it.size.toDouble() }
    }
}