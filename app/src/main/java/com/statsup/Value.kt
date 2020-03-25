package com.statsup

interface Value {
    fun total(): Double
    fun totalOfYear(position: Int): Double
    fun averageOfYear(position: Int): Double
    fun ofYear(position: Int): List<Double>
    fun max(): Double
    fun average(): Double
    fun cumulativeOfCurrentMont(): List<Double>
    fun cumulativeOfPreviousMont(): List<Double>
    fun yearInPosition(position: Int): Int
    fun years(): List<Int>
    fun groupByDay(): Map<Days, Double>
}
