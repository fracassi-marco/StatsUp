package com.statsup.domain

import java.time.Month
import java.time.Year
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now

class Trainings(
    private val trainings: List<Training>,
    private val now: ZonedDateTime = now(),
    private val provider: (List<Training>) -> Double) {

    fun overMonth() = provider(ofMonth())

    fun ofMonth() = trainings.filter { it.date.month == now.month && it.date.year == now.year }

    /*fun byYear(): Map<Year, Double> {
        val minYear = trainings.minBy { it.date }.date.year
        val maxYear = trainings.maxBy { it.date }.date.year

        return (minYear..maxYear).map { Year.of(it) }.associateWith {
            val ts = trainings.filter { t -> t.date.year == it.value }
            provider(ts)
        }
    }*/

    /*fun byMonth(year: Year): Map<Month, Double> {
        val ofYear = trainings.filter { t -> t.date.year == year.value }

        val minMonth = JANUARY.value
        val maxMonth = if(year.isCurrent()) now.month.value else DECEMBER.value

        return (minMonth..maxMonth).map { Month.of(it) }.associateWith {
            val ts = ofYear.filter { t -> t.date.month == it }
            provider(ts)
        }
    }*/

    fun byDay(): LinkedHashMap<Int, Double> {
        val ofMonth = trainings
            .filter { t -> t.date.year == now.year }
            .filter { t -> t.date.month == now.month }

        val result = LinkedHashMap<Int, Double>()
        (1..now.month.maxLength()).forEach {
            val ts = ofMonth.filter { t -> t.date.dayOfMonth == it }
            result[it] = provider(ts)
        }

        return result
    }

    /*fun cumulativeMonths(year: Year): LinkedHashMap<Month, Double> {
        val result = LinkedHashMap<Month, Double>()
        val byMonth: Map<Month, Double> = byMonth(year)
        val max = if(year.isCurrent()) now.month.value else 12
        (1..max).forEach {
            result[Month.of(it)] = byMonth.filter { bm -> bm.key.value <= it }.values.sum()
        }
        return result
    }*/

    fun cumulativeDays(): LinkedHashMap<Int, Double> {
        val result = LinkedHashMap<Int, Double>()
        val byDay: Map<Int, Double> = byDay()
        val max = if(Year.of(now.year).isCurrent() && now.month.isCurrent()) now.dayOfMonth else 31
        (1..max).forEach {
            result[it] = byDay.filter { bm -> bm.key <= it }.values.sum()
        }
        return result
    }
}
