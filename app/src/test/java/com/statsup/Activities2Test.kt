package com.statsup

import com.statsup.Sports.RUN
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.junit.Test
import java.util.*

class Activities2Test {

    @Test
    fun `first month`() {
        val activities = listOf(on(2020, 2), on(2020, 1))

        val selectedActivities = Activities2(activities) { _ -> 1.0 }.filterByMonth(0)
        val month = selectedActivities.month()

        assertThat(month).isEqualTo(Month(2020, 1))
        assertThat(selectedActivities.isFirstMonth()).isEqualTo(true)
        assertThat(selectedActivities.isCurrentMonth()).isEqualTo(false)
    }

    @Test
    fun `any month`() {
        val activities = listOf(on(2020, 12), on(2019, 1))

        val selectedActivities = Activities2(activities) { 1.0 }.filterByMonth(1)
        val month = selectedActivities.month()

        assertThat(month).isEqualTo(Month(2019, 2))
        assertThat(selectedActivities.isFirstMonth()).isEqualTo(false)
        assertThat(selectedActivities.isCurrentMonth()).isEqualTo(false)
    }

    @Test
    fun `current month`() {
        val now =  DateTime()
        val oneMonthAgo = now.minusMonths(1)
        val activities = listOf(on(now.year, now.monthOfYear), on(oneMonthAgo.year, oneMonthAgo.monthOfYear))

        val selectedActivities = Activities2(activities) { 1.0 }.filterByMonth(1)
        val month = selectedActivities.month()

        assertThat(month).isEqualTo(Month(now.year, now.monthOfYear))
        assertThat(selectedActivities.isFirstMonth()).isEqualTo(false)
        assertThat(selectedActivities.isCurrentMonth()).isEqualTo(true)
    }

    @Test
    fun `absolute total`() {
        val activities = listOf(on(2020, 2), on(2020, 1))

        val selectedActivities = Activities2(activities) { list -> list.size.toDouble() }

        assertThat(selectedActivities.total()).isEqualTo(2.0)
    }

    @Test
    fun `monthly total`() {
        val activities = listOf(on(2020, 2), on(2020, 1), on(2020, 1))

        val selectedActivities = Activities2(activities) { list -> list.size.toDouble() }.filterByMonth(0)

        assertThat(selectedActivities.total()).isEqualTo(2.0)
    }

    @Test
    fun `monthly average`() {
        val activities = listOf(on(2020, 2), on(2020, 2), on(2020, 1))

        val selectedActivities = Activities2(activities) { list -> list.size.toDouble() }.filterByMonth(1)

        assertThat(selectedActivities.average()).isEqualTo(2.0 / 29)
    }

    @Test
    fun `group by day contains all days of month`() {
        val activities = listOf(on(2020, 2, 1), on(2020, 2, 1))

        val selectedActivities = Activities2(activities) { list -> list.size.toDouble() }.filterByMonth(0)

        assertThat(selectedActivities.byDay().first()).isEqualTo(2.0)
        assertThat(selectedActivities.byDay().count { it == 0.0 }).isEqualTo(28)
    }

    @Test
    fun `cumulative by day`() {
        val activities = listOf(on(2020, 2, 1), on(2020, 2, 1), on(2020, 2, 2))

        val selectedActivities = Activities2(activities) { list -> list.size.toDouble() }.filterByMonth(0)

        val result = selectedActivities.cumulativeByDay()
        assertThat(result[0]).isEqualTo(2.0)
        assertThat(result[1]).isEqualTo(3.0)
        assertThat(result[2]).isEqualTo(3.0)
        assertThat(result[30]).isEqualTo(3.0)
    }

    private fun on(year: Int, month: Int, day: Int = 1): Activity {
        val dateInMillis = GregorianCalendar(year, month - 1, day).time.time
        return Activity(1, RUN, 0f, 0, 0, dateInMillis, "", 1.0, 1.0, 0.0,0.0, "")
    }
}