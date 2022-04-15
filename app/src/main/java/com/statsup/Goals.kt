package com.statsup

import com.statsup.Measure.hm
import com.statsup.Measure.km
import com.statsup.Measure.times
import com.statsup.Variation.percentage

class Goals(private val activities:  List<Activity>, private val curMonth: Month) {

    private val currentFrequency = totalOfMonth(curMonth, Stats.FREQUENCY.provider)
    private val currentDuration = totalOfMonth(curMonth, Stats.DURATION.provider)
    private val currentDistance = totalOfMonth(curMonth, Stats.DISTANCE.provider)
    private val past3MonthsAverageFrequency = averageOfPastMonths(3, Stats.FREQUENCY.provider)
    private val past3MonthsAverageDuration = averageOfPastMonths(3, Stats.DURATION.provider)
    private val past3MonthsAverageDistance = averageOfPastMonths(3, Stats.DISTANCE.provider)

    fun frequencyRatio() = percentage(currentFrequency, past3MonthsAverageFrequency).toFloat()

    fun durationRatio() = percentage(currentDuration, past3MonthsAverageDuration).toFloat()

    fun distanceRatio() = percentage(currentDistance, past3MonthsAverageDistance).toFloat()

    fun staminaRatio() = ((frequencyRatio() * 2f) + durationRatio() + (distanceRatio() * 0.5f)) / 3.5f

    fun frequencyProgress() = currentFrequency() + "/" + times(past3MonthsAverageFrequency)

    fun durationProgress() = currentDuration() + "/" + hm(past3MonthsAverageDuration * 3600)

    fun distanceProgress() = currentDistance() + "/" + km(past3MonthsAverageDistance)

    fun currentFrequency() = times(currentFrequency)

    fun currentDuration() = hm(currentDuration * 3600)

    fun currentDistance() = km(currentDistance)

    private fun totalOfMonth(month: Month, provider: (List<Activity>) -> Double) = provider(ofMonth(month))

    private fun ofMonth(month: Month) = activities.filter { Month(it.date()) == month }

    private fun averageOfPastMonths(months: Int, provider: (List<Activity>) -> Double): Double {
        val sumOf = (1..months).sumOf {
            totalOfMonth(curMonth.previous(it), provider)
        }
        return sumOf / months
    }
}
