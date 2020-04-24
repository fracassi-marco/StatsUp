package com.statsup

interface Value {
    fun total(): Double
    fun filter(year: Int, month: Int): Value
    fun totalOfYear(position: Int): Double
    fun averageOfYear(position: Int): Double
    fun ofYear(position: Int): List<Double>
    fun max(): Double
    fun average(): Double
    fun cumulativeOfCurrentMont(): List<Double>
    fun cumulativeOfPreviousMont(): List<Double>
    fun years(): List<Int>
    fun groupByDayOfWeek(): Map<DayOfWeek, Double>
    fun yearInPosition(position: Int): Int
}
