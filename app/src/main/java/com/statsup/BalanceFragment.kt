package com.statsup

import android.graphics.Color.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.balance_fragment.view.*
import kotlinx.android.synthetic.main.overview_item.view.*
import lecho.lib.hellocharts.view.LineChartView
import org.joda.time.DateTime

class BalanceFragment : Fragment() {

    private fun updateOverviews(
        weights: List<Weight>,
        monthVariationOverviewItem: View,
        yearVariationOverviewItem: View,
        fullVariationOverviewItem: View,
        minMaxOverviewItem: View
    ) {
        val today = DateTime()
        val finalValue = weights.last().kilograms

        updateOverview(weights, today.minusMonths(1), finalValue, monthVariationOverviewItem, "Variazione ultimi 30 giorni")
        updateOverview(weights, today.minusYears(1), finalValue, yearVariationOverviewItem, "Variazione ultimo anno")
        updateOverview(weights, weights.first().date(), finalValue, fullVariationOverviewItem, "Variazione totale")
        updateMinMaxOverview(weights, minMaxOverviewItem)
    }

    private fun updateMinMaxOverview(
        weights: List<Weight>,
        minMaxOverviewItem: View
    ) {
        minMaxOverviewItem.left_value.text =
            Measure.of(weights.minBy { it.kilograms }!!.kilograms, "Kg", "")
        minMaxOverviewItem.left_value.textSize = 21f
        minMaxOverviewItem.left_text.text = "Peso minimo"

        minMaxOverviewItem.center_value.text = Measure.of(weights.last().kilograms, "Kg", "")
        minMaxOverviewItem.center_value.textSize = 26f
        minMaxOverviewItem.center_text.text = "Peso attuale"

        minMaxOverviewItem.right_value.text =
            Measure.of(weights.maxBy { it.kilograms }!!.kilograms, "Kg", "")
        minMaxOverviewItem.right_value.textSize = 21f
        minMaxOverviewItem.right_text.text = "Peso massimo"
    }

    private fun updateOverview(weights: List<Weight>, fromDate: DateTime, finalValue: Double, view: View, label: String) {
        val initialValue = weights.lastOrNull { it.date() <= fromDate }
        if (initialValue != null) {
            val percentage = (finalValue / initialValue.kilograms * 100) - 100
            view.left_value.text = Measure.of(finalValue - initialValue.kilograms, "Kg")
            view.right_value.text = Measure.of(percentage, "%")
            view.right_value.setTextColor(if(percentage > 0) RED else GREEN)
        }
        else {
            view.left_value.text = "-"
            view.right_value.text = "-"
        }
        view.left_text.text = label
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.balance_fragment, container, false)
        val lineChart = view.line_chart
        val monthVariationOverviewItem = view.month_variation_overview_item
        val yearVariationOverviewItem = view.year_variation_overview_item
        val fullVariationOverviewItem = view.full_variation_overview_item
        val minMaxOverviewItem = view.bmi_oxford_min_max_overview_item

        val weights = WeightRepository.all().sortedBy { it.dateInMillis }
        updateChart(weights, lineChart)
        updateOverviews(weights, monthVariationOverviewItem, yearVariationOverviewItem, fullVariationOverviewItem, minMaxOverviewItem)

        return view
    }

    private fun updateChart(weights: List<Weight>, lineChart: LineChartView) {
        WeightChart(lineChart).refresh(weights)
    }
}