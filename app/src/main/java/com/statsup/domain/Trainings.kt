package com.statsup.domain

import java.time.Month
import java.time.Month.JANUARY
import java.time.Month.DECEMBER
import java.time.Year
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now

class Trainings(
    private val trainings: List<Training>,
    private val now: ZonedDateTime = now(),
    private val provider: Provider
) {

    fun overMonth() = provider.cumulative(ofMonth())

    fun ofMonth() = trainings.filter { it.date.month == now.month && it.date.year == now.year }

    fun by12Month(): Map<Month, Double> {
        val minMonth = now.minusMonths(11)
        val ofYear = trainings.filter { t -> t.date > minMonth.withDayOfMonth(1) }

        return (11L downTo 0).map { Month.of(now.minusMonths(it).monthValue) }.associateWith {
            val ts = ofYear.filter { t -> t.date.month == it }
            provider.cumulative(ts)
        }
    }

    fun byMonth(): Map<Month, Double> {
        val year = Year.of(now.year)
        val ofYear = trainings.filter { t -> t.date.year == year.value }

        val minMonth = JANUARY.value
        val maxMonth = if (year.isCurrent()) now.month.value else DECEMBER.value

        return (minMonth..maxMonth).map { Month.of(it) }.associateWith {
            val ts = ofYear.filter { t -> t.date.month == it }
            provider.cumulative(ts)
        }
    }

    fun byMonthTrend(): Map<Month, Double> {
        val ofYear = trainings.filter { t -> t.date.year == now.year }
        val avg = provider.cumulative(ofYear) / now.monthValue

        val minMonth = JANUARY.value
        val maxMonth = DECEMBER.value

        return (minMonth..maxMonth).map { Month.of(it) }.associateWith {
            if (it.value > now.monthValue) {
                avg
            } else {
                val ts = ofYear.filter { t -> t.date.month == it }
                provider.cumulative(ts)
            }
        }
    }

    fun groupByDay(): LinkedHashMap<Int, Double> {
        val ofMonth = ofMonth()
        val result = LinkedHashMap<Int, Double>()
        (1..now.month.maxLength()).forEach {
            val ts = ofMonth.filter { t -> t.date.dayOfMonth == it }
            result[it] = provider.cumulative(ts)
        }

        return result
    }

    fun groupByDayTrend(): LinkedHashMap<Int, Double> {
        val ofMonth = ofMonth()
        val avg = provider.cumulative(ofMonth) / now.dayOfMonth
        val result = LinkedHashMap<Int, Double>()
        (1..31).forEach {
            if (it > now.dayOfMonth) {
                result[it] = avg
            } else {
                val ts = ofMonth.filter { t -> t.date.dayOfMonth == it }
                result[it] = provider.cumulative(ts)
            }
        }

        return result
    }

    fun groupBy31Day(): Map<ZonedDateTime, Double> {
        return (30L downTo 0).map { now.minusDays(it) }.associateWith {
            val ts =
                trainings.filter { t -> t.date.year == it.year && t.date.dayOfYear == it.dayOfYear }
            provider.cumulative(ts)
        }
    }

    fun cumulativeMonths(): LinkedHashMap<Month, Double> {
        val result = LinkedHashMap<Month, Double>()
        val byMonth: Map<Month, Double> = byMonth()
        val max = 12
        (1..max).forEach {
            result[Month.of(it)] = byMonth.filter { bm -> bm.key.value <= it }.values.sum()
        }
        return result
    }

    fun cumulativeMonthsTrend(): LinkedHashMap<Month, Double> {
        val result = LinkedHashMap<Month, Double>()
        val byMonth: Map<Month, Double> = byMonthTrend()
        val max = 12
        (1..max).forEach {
            result[Month.of(it)] = byMonth.filter { bm -> bm.key.value <= it }.values.sum()
        }
        return result
    }

    fun cumulativeDays(): LinkedHashMap<Int, Double> {
        val result = LinkedHashMap<Int, Double>()
        val byDay: Map<Int, Double> = groupByDay()
        val max =
            31 //if(Year.of(now.year).isCurrent() && now.month.isCurrent()) now.dayOfMonth else 31
        (1..max).forEach {
            result[it] = byDay.filter { bm -> bm.key <= it }.values.sum()
        }
        return result
    }

    fun cumulativeDaysTrend(): LinkedHashMap<Int, Double> {
        val result = LinkedHashMap<Int, Double>()
        val byDay: Map<Int, Double> = groupByDayTrend()
        (1..31).forEach {
            result[it] = byDay.filter { bm -> bm.key <= it }.values.sum()
        }
        return result
    }

    fun maxOfMonth(): Double {
        return provider.max(ofMonth())
    }
}
