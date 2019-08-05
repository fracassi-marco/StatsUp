package com.statsup

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.no_items_layout.view.*
import kotlinx.android.synthetic.main.overview_item.view.*
import kotlinx.android.synthetic.main.weight_stats_fragment.view.*
import lecho.lib.hellocharts.view.LineChartView
import org.joda.time.DateTime


class WeightStatsFragment : Fragment() {
    private lateinit var weights: List<Weight>
    private var height: Int = 0
    private lateinit var lineChart: LineChartView
    private lateinit var monthVariationOverviewItem: View
    private lateinit var yearVariationOverviewItem: View
    private lateinit var fullVariationOverviewItem: View
    private lateinit var minMaxOverviewItem: View
    private lateinit var minMaxBmiOverviewItem: View
    private lateinit var noItemListener: Listener<List<Weight>>
    private val listener = object : Listener<List<Weight>> {
        override fun update(subject: List<Weight>) {
            if (subject.isEmpty()) {
                return
            }

            weights = subject.sortedBy { it.dateInMillis }
            updateChart(weights)
            updateOverviews(weights)
            updateBmiOverviews()
        }
    }

    private val userListener = object : Listener<User> {
        override fun update(subject: User) {
            height = subject.height
            updateBmiOverviews()
        }
    }

    private fun updateBmiOverviews() {
        minMaxBmiOverviewItem.left_value.text = Bmi.of(weights.minBy { it.kilograms }!!, height)
        minMaxBmiOverviewItem.left_value.textSize = 21f
        minMaxBmiOverviewItem.left_text.text = "BMI minimo"

        minMaxBmiOverviewItem.center_value.text = Bmi.of(weights.last(), height)
        minMaxBmiOverviewItem.center_value.textSize = 26f
        minMaxBmiOverviewItem.center_text.text = "BMI attuale"

        minMaxBmiOverviewItem.right_value.text = Bmi.of(weights.maxBy { it.kilograms }!!, height)
        minMaxBmiOverviewItem.right_value.setTextColor(Color.BLACK)
        minMaxBmiOverviewItem.right_value.textSize = 21f
        minMaxBmiOverviewItem.right_text.text = "BMI massimo"
    }

    private fun updateOverviews(weights: List<Weight>) {
        val today = DateTime()
        val finalValue = weights.last().kilograms

        updateOverview(weights, today.minusMonths(1), finalValue, monthVariationOverviewItem, "Variazione ultimi 30 giorni")
        updateOverview(weights, today.minusYears(1), finalValue, yearVariationOverviewItem, "Variazione ultimo anno")
        updateOverview(weights, weights.first().date(), finalValue, fullVariationOverviewItem, "Variazione totale")
        updateMinMaxOverview(weights)
    }

    private fun updateMinMaxOverview(weights: List<Weight>) {
        minMaxOverviewItem.left_value.text = Measure.of(weights.minBy { it.kilograms }!!.kilograms, "Kg", "")
        minMaxOverviewItem.left_value.textSize = 21f
        minMaxOverviewItem.left_text.text = "Peso minimo"

        minMaxOverviewItem.center_value.text = Measure.of(weights.last().kilograms, "Kg", "")
        minMaxOverviewItem.center_value.textSize = 26f
        minMaxOverviewItem.center_text.text = "Peso attuale"

        minMaxOverviewItem.right_value.text = Measure.of(weights.maxBy { it.kilograms }!!.kilograms, "Kg", "")
        minMaxOverviewItem.right_value.setTextColor(Color.BLACK)
        minMaxOverviewItem.right_value.textSize = 21f
        minMaxOverviewItem.right_text.text = "Peso massimo"
    }

    private fun updateOverview(weights: List<Weight>, fromDate: DateTime, finalValue: Double, view: View, label: String) {
        val initialValue = weights.lastOrNull { it.date() <= fromDate }
        if (initialValue != null) {
            val percentage = (finalValue / initialValue.kilograms * 100) - 100
            view.left_value.text = Measure.of(finalValue - initialValue.kilograms, "Kg")
            view.right_value.text = Measure.of(percentage, "%")
            if (percentage > 0) {
                view.right_value.setTextColor(Color.RED)
            }
        }
        else {
            view.left_value.text = "-"
            view.right_value.text = "-"
        }
        view.left_text.text = label
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.weight_stats_fragment, container, false)
        lineChart = view.line_chart
        monthVariationOverviewItem = view.month_variation_overview_item
        yearVariationOverviewItem = view.year_variation_overview_item
        fullVariationOverviewItem = view.full_variation_overview_item
        minMaxOverviewItem = view.min_max_overview_item
        minMaxBmiOverviewItem = view.min_max_bmi_overview_item

        val noItemLayout = view.no_item_layout.apply {
            label_text.text = resources.getString(R.string.empty_weight)
        }

        noItemListener = NoActivitiesListener(view.weight_stats_scroll, noItemLayout)
        WeightRepository.listen(listener, noItemListener)
        UserRepository.listen(userListener)

        return view
    }

    private fun updateChart(weights: List<Weight>) {
        WeightChart(lineChart).refresh(weights)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        WeightRepository.removeListener(listener, noItemListener)
        UserRepository.removeListener(userListener)
    }
}
