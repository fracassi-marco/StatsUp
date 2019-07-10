package com.statsup

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import lecho.lib.hellocharts.view.ColumnChartView

class YearlyChartsPagerAdapter(
    private val context: Context,
    private val allActivities: Activities,
    private val color: Int,
    private val labelText: String,
    private val value: Value
) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.chart_pager_item, container, false)
        val label = view.findViewById<TextView>(R.id.year_label)
        val chart = view.findViewById<ColumnChartView>(R.id.year_bar_chart)
        val previousYearLabel = view.findViewById<TextView>(R.id.previous_year_label)
        val nextYearLabel = view.findViewById<TextView>(R.id.next_year_label)
        val previousYearImage = view.findViewById<ImageView>(R.id.previous_year_image)
        val nextYearImage = view.findViewById<ImageView>(R.id.next_year_image)

        val year = allActivities.yearInPosition(position)
        label.text = year.toString()
        previousYearLabel.text = if (position > 0) (year - 1).toString() else ""
        nextYearLabel.text = if (position < allActivities.years().size - 1) (year + 1).toString() else ""
        previousYearImage.visibility = if (position > 0) View.VISIBLE else View.INVISIBLE
        nextYearImage.visibility = if (position < allActivities.years().size - 1) View.VISIBLE else View.INVISIBLE

        val totalOverviewItem = view.findViewById<View>(R.id.total_overview_item)
        totalOverviewItem.findViewById<TextView>(R.id.overview_item_value).text = asString(value.totalOfYear(position))
        totalOverviewItem.findViewById<TextView>(R.id.overview_item_value_text).text = "Totale $year"

        val averageOverviewItem = view.findViewById<View>(R.id.average_overview_item)
        averageOverviewItem.findViewById<TextView>(R.id.overview_item_value).text = asString(value.averageOfYear(position))
        averageOverviewItem.findViewById<TextView>(R.id.overview_item_value_text).text = "Media mensile $year"

        val fullAverageOverviewItem = view.findViewById<View>(R.id.full_average_overview_item)
        fullAverageOverviewItem.findViewById<TextView>(R.id.overview_item_value).text = asString(value.average())
        fullAverageOverviewItem.findViewById<TextView>(R.id.overview_item_value_text).text = "Media di sempre"

        YearlyChart(chart, color, labelText).refresh(value, position)

        container.addView(view)

        return view
    }

    private fun asString(value: Double): String {
        return String.format("%.2f", value)
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