package com.statsup

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import lecho.lib.hellocharts.view.ColumnChartView

class YearlyChartsPagerAdapter(private val context: Context,
                               private val allActivities: Activities,
                               private val color: Int,
                               private val labelText: String,
                               private val maxValue: Float,
                               private val averageValue: Float,
                               private val valueProvider: (List<Activity>) -> Float) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.chart_pager_item, container, false)
        val label = view.findViewById<TextView>(R.id.year_label)
        val chart = view.findViewById<ColumnChartView>(R.id.year_bar_chart)

        label.text = allActivities.yearInPosition(position).toString()

        YearlyChart(chart, color, labelText, maxValue) {
            valueProvider.invoke(it)
        }.refresh(allActivities, position)

        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    }

    override fun isViewFromObject(view: View, instance: Any): Boolean {
        return view == instance
    }

    override fun getCount(): Int {
        return allActivities.years().size
    }

}