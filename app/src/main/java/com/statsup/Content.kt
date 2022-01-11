package com.statsup

import android.view.View
import com.statsup.databinding.NoActivitiesLayoutBinding
import com.statsup.databinding.NoItemsLayoutBinding

object Content {
    fun showActivitiesOrEmptyPage(noItemLayout: NoActivitiesLayoutBinding, content: View) {
        if (ActivityRepository.anyActivities()) {
            noItemLayout.root.visibility = View.GONE
            content.visibility = View.VISIBLE
        } else {
            noItemLayout.root.visibility = View.VISIBLE
            noItemLayout.importButton.setOnClickListener {
                (noItemLayout.root.context as MainActivity).startActivitiesImport()
            }
            content.visibility = View.GONE
        }
    }

    fun showWeightsOrEmptyPage(noItemLayout: NoItemsLayoutBinding, viewPager: View) {
        if (WeightRepository.any()) {
            noItemLayout.root.visibility = View.GONE
            viewPager.visibility = View.VISIBLE
        } else {
            noItemLayout.root.visibility = View.VISIBLE
            viewPager.visibility = View.GONE
        }
    }
}