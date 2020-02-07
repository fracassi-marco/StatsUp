package com.statsup

interface Value {
    fun totalOfYear(position: Int): Double
    fun averageOfYear(position: Int): Double
    fun ofYear(position: Int): List<Double>
    fun max(): Double
    fun average(): Double
    fun cumulativeOfCurrentMont(): List<Double>
    fun cumulativeOfPreviousMont(): List<Double>
}
