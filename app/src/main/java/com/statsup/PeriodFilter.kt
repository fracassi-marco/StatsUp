package com.statsup

import android.content.Context
import com.statsup.Period.*

enum class Period(val label: Int) {
    MONTHLY(R.string.period_monthly),
    ANNUAL(R.string.period_annual),
    EVER(R.string.period_ever)
}

object PeriodFilter {
    private var current = MONTHLY

    fun change(position: Int): Boolean {
        val isChanged = current != Period.values()[position]
        current = Period.values()[position]

        return isChanged
    }

    fun pagerAdapter(context: Context): ActivityPagerAdapter {
        return when (current) {
            MONTHLY -> MonthlyChartsPagerAdapter(context)
            ANNUAL -> AnnualChartsPagerAdapter(context)
            EVER -> EverChartsPagerAdapter(context)
        }
    }
}