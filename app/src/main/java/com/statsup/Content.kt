package com.statsup

import android.view.View
import kotlinx.android.synthetic.main.no_activities_layout.view.*

object Content {
    fun showActivitiesOrEmptyPage(noItemLayout: View, content: View) {
        if (ActivityRepository.anyActivities()) {
            noItemLayout.visibility = View.GONE
            content.visibility = View.VISIBLE
        } else {
            noItemLayout.visibility = View.VISIBLE
            noItemLayout.import_button.setOnClickListener {
                (noItemLayout.context as MainActivity).startImportFromStrava()
            }
            content.visibility = View.GONE
        }
    }

    fun showWeightsOrEmptyPage(noItemLayout: View, viewPager: View) {
        if (WeightRepository.any()) {
            noItemLayout.visibility = View.GONE
            viewPager.visibility = View.VISIBLE
        } else {
            noItemLayout.visibility = View.VISIBLE
            viewPager.visibility = View.GONE
        }
    }
}