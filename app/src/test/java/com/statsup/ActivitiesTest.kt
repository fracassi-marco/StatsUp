package com.statsup

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test

import java.util.*

class ActivitiesTest {

    @Test
    fun `average of one month`() {
        val activities = Activities(listOf(on(2019, 0, 1)), GregorianCalendar(2019, 0, 1))

        val average = activities.average { it.size.toDouble() }

        assertThat(average, `is`(1.0 / 1))
    }

    @Test
    fun `average of two month`() {
        val activities = Activities(listOf(
            on(2019, 0, 1),
            on(2019, 0, 1),
            on(2019, 1, 1)
        ), GregorianCalendar(2019, 1, 1))

        val average = activities.average { it.size.toDouble() }

        assertThat(average, `is`(3.0 / 2))
    }

    @Test
    fun `average of two years`() {
        val activities = Activities(listOf(
            on(2018, 7, 1),
            on(2018, 8, 1),
            on(2018, 9, 1),
            on(2018, 10, 1),
            on(2018, 11, 1),
            on(2019, 0, 1),
            on(2019, 1, 1)
        ), GregorianCalendar(2019, 1, 1))

        val average = activities.average { it.size.toDouble() }

        assertThat(average, `is`(7.0 / 14))
    }

    @Test
    fun `average of current year`() {
        val activities = Activities(listOf(
            on(2018, 11, 1),
            on(2019, 0, 1),
            on(2019, 0, 1)
        ), GregorianCalendar(2019, 1, 1))

        val average = activities.averageOfYear(1) { it.size.toDouble() }

        assertThat(average, `is`(2.0 / 2))
    }

    @Test
    fun `average of past year`() {
        val activities = Activities(listOf(
            on(2018, 11, 1),
            on(2019, 0, 1),
            on(2019, 0, 1)
        ), GregorianCalendar(2019, 1, 1))

        val average = activities.averageOfYear(0) { it.size.toDouble() }

        assertThat(average, `is`(1.0 / 12))
    }

    private fun on(year: Int, month: Int, day: Int) =
        Activity(Sports.WORKOUT, 0f, 0, GregorianCalendar(year, month, day).time.time, "")
}