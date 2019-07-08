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