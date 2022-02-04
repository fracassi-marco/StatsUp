package com.statsup

import android.graphics.Color.GREEN
import android.graphics.Color.RED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.statsup.databinding.BalanceFragmentBinding
import com.statsup.databinding.OverviewItemBinding
import org.joda.time.DateTime
import kotlin.math.absoluteValue


class BalanceFragment : Fragment() {

    private var _binding: BalanceFragmentBinding? = null
    private val binding get() = _binding!!

    private fun updateOverviews(weights: List<Weight>) {
        val today = DateTime()
        val finalValue = weights.last().kilograms

        updateOverview(
            weights,
            today.minusMonths(1),
            finalValue,
            binding.monthVariationOverviewItem,
            "Variazione ultimi 30 giorni"
        )
        updateOverview(
            weights,
            today.minusYears(1),
            finalValue,
            binding.yearVariationOverviewItem,
            "Variazione ultimo anno"
        )
        updateOverview(
            weights,
            weights.first().date(),
            finalValue,
            binding.fullVariationOverviewItem,
            "Variazione totale"
        )
        updateMinMaxOverview(weights)
        updateIdealWeightOverview(weights)
    }

    private fun updateIdealWeightOverview(weights: List<Weight>) {
        val view = binding.traviaWeightOverviewItem
        val height = UserRepository.user.height
        if (height == 0) {
            view.root.visibility = GONE
            return
        }

        val travia = (1.012 * height) - 107.5
        view.leftValue.text = Measure.of(travia, "Kg", "")
        view.leftText.setText(R.string.weight_travia)

        val delta = weights.last().kilograms - travia
        view.rightValue.text = Measure.of(delta, "Kg")
        view.rightText.setText(R.string.weight_travia_delta)
        view.rightValue.setTextColor(if (delta.absoluteValue > 5) RED else GREEN)
        view.rightValue.textSize = 21f
    }

    private fun updateMinMaxOverview(weights: List<Weight>) {
        val view = binding.bmiOxfordMinMaxOverviewItem
        view.leftValue.text = Measure.of(weights.minByOrNull { it.kilograms }!!.kilograms, "Kg", "")
        view.leftValue.textSize = 21f
        view.leftText.setText(R.string.weight_min)

        view.centerValue.text = Measure.of(weights.last().kilograms, "Kg", "")
        view.centerValue.textSize = 26f
        view.centerText.setText(R.string.weight_current)

        view.rightValue.text = Measure.of(weights.maxByOrNull { it.kilograms }!!.kilograms, "Kg", "")
        view.rightValue.textSize = 21f
        view.rightText.setText(R.string.weight_max)
    }

    private fun updateOverview(
        weights: List<Weight>,
        fromDate: DateTime,
        finalValue: Double,
        view: OverviewItemBinding,
        label: String
    ) {
        val initialValue = weights.lastOrNull { it.date() <= fromDate }
        if (initialValue != null) {
            val percentage = (finalValue / initialValue.kilograms * 100) - 100
            view.leftValue.text = Measure.of(finalValue - initialValue.kilograms, "Kg")
            view.rightValue.text = Measure.of(percentage, "%")
            view.rightValue.setTextColor(if (percentage > 0) RED else GREEN)
        } else {
            view.leftValue.text = "-"
            view.rightValue.text = "-"
        }
        view.rightValue.textSize = 21f
        view.leftText.text = label
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BalanceFragmentBinding.inflate(inflater, container, false)

        val weights = WeightRepository.all().sortedBy { it.dateInMillis }
        updateLastThreeMonthsChart(weights, binding.lastThreeMonthsChart)
        updateMonthlyChart(weights, binding.monthlyChart)
        updateOverviews(weights)
        updateSeekBar(weights)

        return binding.root
    }

    private fun updateSeekBar(weights: List<Weight>) {
        val max = weights.maxByOrNull { it.kilograms }!!.kilograms.toFloat()
        val min = weights.minByOrNull { it.kilograms }!!.kilograms.toFloat()
        binding.seekBar.setIndicatorTextDecimalFormat("0.00")
        binding.seekBar.setIndicatorText(weights.last().kilograms.toString())
        binding.seekBar.tickMarkTextArray = arrayOf(min.toString(), max.toString())
        binding.seekBar.setRange(min, max)
        binding.seekBar.setProgress(weights.last().kilograms.toFloat())
        binding.seekBar.isEnabled = false
    }

    private fun updateLastThreeMonthsChart(weights: List<Weight>, graph: LineChart) {
        val threeMonthsAgo = Month().previous().previous().previous()
        val lastThreeMonthsWeights = weights.filter { Month(it.date()).isAfterOrEqual(threeMonthsAgo) }
        BalanceChart(graph, DayAxisValueFormatter(lastThreeMonthsWeights.first().dateInMillis)).refresh(lastThreeMonthsWeights)
    }

    private fun updateMonthlyChart(weights: List<Weight>, graph: LineChart) {
        val byMonthAverage = weights
            .groupBy { Month(it.date()) }
            .map { group -> Weight(group.value.map { it.kilograms }.average(), group.key.firstDay().millis) }
        BalanceChart(graph, MonthAxisValueFormatter(byMonthAverage.first().dateInMillis)).refresh(byMonthAverage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}