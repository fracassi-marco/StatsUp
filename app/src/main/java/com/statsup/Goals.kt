package com.statsup

import com.statsup.Measure.hm
import com.statsup.Measure.km
import com.statsup.Measure.times
import com.statsup.Variation.percentage
import kotlin.math.roundToInt

class Goals(private val activities:  List<Activity>, private val curMonth: Month) {

    fun frequencyRatio(): Float {
        val current = totalOfMonth(curMonth, Stats.FREQUENCY.provider).roundToInt()
        val averageOfPast3Months = averageOfPastMonths(3, Stats.FREQUENCY.provider).roundToInt()
        return percentage(current, averageOfPast3Months).toFloat()
    }

    fun durationRatio(): Float {
        val current = totalOfMonth(curMonth, Stats.DURATION.provider)
        val averageOfPast3Months = averageOfPastMonths(3, Stats.DURATION.provider)
        return percentage(current, averageOfPast3Months).toFloat()
    }

    fun distanceRatio(): Float {
        val current = totalOfMonth(curMonth, Stats.DISTANCE.provider)
        val averageOfPast3Months = averageOfPastMonths(3, Stats.DISTANCE.provider)
        return percentage(current, averageOfPast3Months).toFloat()
    }

    fun staminaRatio(): Float {
        return ((frequencyRatio() * 2f) + durationRatio() + (distanceRatio() * 0.5f)) / 3.5f
    }

    fun frequencyProgress(): String {
        return times(totalOfMonth(curMonth, Stats.FREQUENCY.provider)) + "/" + times(averageOfPastMonths(3, Stats.FREQUENCY.provider))
    }

    fun durationProgress(): String {
        return hm(totalOfMonth(curMonth, Stats.DURATION.provider) * 3600) + "/" + hm(averageOfPastMonths(3, Stats.DURATION.provider) * 3600)
    }

    fun distanceProgress(): String {
        return km(totalOfMonth(curMonth, Stats.DISTANCE.provider)) + "/" + km(averageOfPastMonths(3, Stats.DISTANCE.provider))
    }

    fun currentFrequency(): String {
        return times(totalOfMonth(curMonth, Stats.FREQUENCY.provider))
    }

    fun currentDuration(): String {
        return hm(totalOfMonth(curMonth, Stats.DURATION.provider) * 3600)
    }

    fun currentDistance(): String {
        return km(totalOfMonth(curMonth, Stats.DISTANCE.provider))
    }

    private fun totalOfMonth(month: Month, provider: (List<Activity>) -> Double) = provider(ofMonth(month))

    private fun ofMonth(month: Month) = activities.filter { Month(it.date()) == month }

    private fun averageOfPastMonths(months: Int, provider: (List<Activity>) -> Double): Double {
        val sumOf = (1..months).sumOf {
            totalOfMonth(curMonth.previous(it), provider)
        }
        return sumOf / months
    }
}
