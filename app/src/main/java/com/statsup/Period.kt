package com.statsup

import android.content.Context

object Period {

    private const val FIRST_CALL = -2
    private const val CHANGED = -1
    private const val MONTHLY = 0
    private const val ANNUAL = 1

    private var current = FIRST_CALL

    fun change(position: Int): Boolean {
        val isChanged = current != position
        current = position

        return isChanged
    }

    fun pagerAdapter(context: Context): ActivityPagerAdapter {
        if (current == FIRST_CALL) {
            current = MONTHLY
        }

        return when (current) {
            MONTHLY -> MonthlyChartsPagerAdapter(context)
            ANNUAL -> AnnualChartsPagerAdapter(context)
            else -> AnnualChartsPagerAdapter(context)
        }
    }
}