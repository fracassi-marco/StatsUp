package com.statsup

import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import org.joda.time.DateTime
import java.util.*

class BalanceChart(private val chart: LineChart) {

    private var xFormatter : ValueFormatter = DefaultAxisValueFormatter(1)

    fun withXFormatter(value: ValueFormatter): BalanceChart {
        xFormatter = value
        return this
    }

    fun refresh(weights: List<Weight>) {
        if (weights.isEmpty()) {
            chart.clear()
        } else {
            chart.data = LineData(createDataSet(weights))
        }
        styleGraph(weights)
        chart.marker = ValueMarker(chart.context, xFormatter, DefaultAxisValueFormatter(1))
        chart.invalidate()
    }

    private fun createDataSet(weights: List<Weight>): LineDataSet {
        val dataSet = LineDataSet(generateEntries(weights), "Weight")
        dataSet.color = Color.rgb(255, 185, 97)
        dataSet.setCircleColor(Color.rgb(255, 185, 97))
        dataSet.circleRadius = 6f
        dataSet.lineWidth = 3.5f
        dataSet.setDrawValues(false)
        dataSet.valueTextSize = 12f
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        return dataSet
    }

    private fun generateEntries(weights: List<Weight>): List<Entry> {
        val base = weights.first().dateInMillis
        return weights.map { Entry(it.dateInMillis.toFloat() - base, it.kilograms.toFloat()) }
    }

    private fun getLongDateWithoutTime(): Float {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        return cal.timeInMillis.toFloat()
    }

    private fun styleGraph(weights: List<Weight>) {
        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)

        if(weights.size > 20) {
            val timeWindow = (weights.last().dateInMillis - weights.first().dateInMillis) / 3f
            chart.setVisibleXRangeMaximum(timeWindow)
            chart.moveViewToX(getLongDateWithoutTime() - timeWindow)
        }
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setScaleMinima(1f, 1f)

        val xAxis = chart.xAxis
        xAxis.valueFormatter = xFormatter
        xAxis.textSize = 12f
        xAxis.granularity = 86400000f
        xAxis.labelCount = 5
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        chart.axisRight.isEnabled = false

        val yAxis: YAxis = chart.axisLeft
        yAxis.valueFormatter = DefaultAxisValueFormatter(0)
        yAxis.textSize = 14f
        yAxis.granularity = 1f
        yAxis.labelCount = 10
        yAxis.setDrawAxisLine(true)
        yAxis.setDrawZeroLine(true)
        yAxis.setDrawGridLines(false)
        yAxis.axisMaximum = weights.maxOfOrNull { it.kilograms }!!.toFloat()
        yAxis.axisMinimum = weights.minOfOrNull { it.kilograms }!!.toFloat()

        chart.setTouchEnabled(true)
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)
        chart.isDoubleTapToZoomEnabled = false
    }
}



class DayAxisValueFormatter(private val offset: Long) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val date = DateTime(value.toLong() + offset)
        return Month(date).asString(date.dayOfMonth)
    }
}

class MonthAxisValueFormatter(private val offset: Long) : ValueFormatter() {
    override fun getFormattedValue(value: Float) = Month(DateTime(value.toLong() + offset)).asString()
}
