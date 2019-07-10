package com.statsup

interface Value {
    fun totalOfYear(position: Int): Double
    fun averageOfYear(position: Int): Double
    fun ofYear(position: Int): List<Float>
    fun max(): Float
    fun average(): Double
}
