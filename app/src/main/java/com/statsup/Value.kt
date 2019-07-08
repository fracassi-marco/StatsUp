package com.statsup

interface Value {
    fun totalOfYear(position: Int): Float
    fun averageOfYear(position: Int): Float
    fun ofYear(position: Int): List<Float>
    fun max(): Float
    fun average(): Float
}
