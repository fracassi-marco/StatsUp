package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class FrequencyFragment : Fragment() {

    private var leftValue: Float = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frequency_fragment, container, false)
        val yearBarChart = view.findViewById<BarChart>(R.id.year_frequency_bar_chart)

        val data = mutableListOf<BarEntry>()
        for (i in 0..35) {
            data.add(BarEntry(i.toFloat(), i.toFloat()))
        }

        val barDataSet = BarDataSet(data, "Ciao").apply {
            valueFormatter = BarChartValueFormatter()
        }
        //barDataSet.valueTextSize = 10f
        //barDataSet.color = resources.getColor(R.color.DarkOrange)
        yearBarChart.data = BarData(barDataSet)

        yearBarChart.setVisibleXRangeMaximum(12.toFloat())
        leftValue = yearBarChart.xChartMax
        moveIndex(-12, yearBarChart)

        yearBarChart.axisLeft.isEnabled = false
        yearBarChart.axisLeft.setDrawGridLines(false)
        yearBarChart.axisRight.isEnabled = false
        yearBarChart.axisRight.setDrawGridLines(false)
        yearBarChart.xAxis.granularity = 1f
        yearBarChart.xAxis.setDrawGridLines(false)
        yearBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        yearBarChart.xAxis.valueFormatter = MonthLabels()
        yearBarChart.setTouchEnabled(false)

        yearBarChart.invalidate()

        val previousYearButton = view.findViewById<ImageView>(R.id.previous_year_button)
        previousYearButton.setOnClickListener {
            moveIndex(-12, yearBarChart)
        }
        val nextYearButton = view.findViewById<ImageView>(R.id.next_year_button)
        nextYearButton.setOnClickListener {
            moveIndex(12, yearBarChart)
        }

        return view
    }

    private fun moveIndex(
        amount: Int,
        yearBarChart: BarChart
    ) {
        val newIndex = leftValue + amount
        if (newIndex >= yearBarChart.xChartMin && newIndex <= yearBarChart.xChartMax -12) {
            leftValue = newIndex
            yearBarChart.moveViewToX(leftValue)
        }
    }
}
