package com.statsup

class Frequencies(private val activities: Activities) : Value {
    private val provider =  { list: List<Activity> -> list.size.toDouble() }

    override fun average(): Double {
        return activities.average(provider)
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

    override fun totalOfYear(position: Int): Double {
        return activities.totalOfYear(position, provider)
    }
}