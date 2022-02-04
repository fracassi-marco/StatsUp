package com.statsup

import android.graphics.Color.parseColor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.statsup.databinding.BmiFragmentBinding
import com.statsup.databinding.OverviewItemBinding
import com.statsup.segmentedbar.Segment
import com.statsup.segmentedbar.SegmentedBarView

class BmiFragment : Fragment() {

    private var _binding: BmiFragmentBinding? = null
    private val binding get() = _binding!!

    private fun setVisibleView(
        content: ConstraintLayout,
        noItemsLayout: ConstraintLayout,
        height: Int
    ) {
        if (height == 0) {
            content.visibility = View.GONE
            noItemsLayout.visibility = View.VISIBLE
        } else {
            content.visibility = View.VISIBLE
            noItemsLayout.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BmiFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        val weights = WeightRepository.all()
        val height = UserRepository.user.height
        setVisibleView(binding.content, binding.noItemsLayout.root, height)

        if (height != 0) {
            val minWeight = weights.minByOrNull { it.kilograms }!!
            val currentWeight = weights.first()
            val maxWeight = weights.maxByOrNull { it.kilograms }!!

            updateOverviews(
                binding.bmiOxfordMinMaxOverviewItem,
                Bmi.labelForOxford(minWeight, height),
                Bmi.labelForOxford(currentWeight, height),
                Bmi.labelForOxford(maxWeight, height)
            )
            updateChart(binding.bmiOxfordChart, Bmi.oxford(currentWeight, height))

            updateOverviews(
                binding.bmiClassicMinMaxOverviewItem,
                Bmi.labelForClassic(minWeight, height),
                Bmi.labelForClassic(currentWeight, height),
                Bmi.labelForClassic(maxWeight, height)
            )
            updateChart(binding.bmiClassicChart, Bmi.classic(currentWeight, height))
        }

        return view
    }

    private fun updateOverviews(
        minMaxOverviewItem: OverviewItemBinding,
        min: String, current: String, max: String
    ) {
        minMaxOverviewItem.leftValue.text = min
        minMaxOverviewItem.leftValue.textSize = 21f
        minMaxOverviewItem.leftText.text = getString(R.string.bmi_min)

        minMaxOverviewItem.centerValue.text = current
        minMaxOverviewItem.centerValue.textSize = 26f
        minMaxOverviewItem.centerText.text = getString(R.string.bmi_current)

        minMaxOverviewItem.rightValue.text = max
        minMaxOverviewItem.rightValue.textSize = 21f
        minMaxOverviewItem.rightText.text = getString(R.string.bmi_max)
    }

    private fun updateChart(
        bmiChart: SegmentedBarView,
        bmi: Double
    ) {
        bmiChart.setSegments(
            listOf(
                Segment(0f, 15.99f, "< 16", getString(R.string.bmi_too_low), parseColor("#2196f3")),
                Segment(16f, 18.49f, "16 - 18.5", getString(R.string.bmi_low), parseColor("#21daf3")),
                Segment(18.5f, 24.99f, "18.5 - 25", getString(R.string.bmi_normal), parseColor("#4caf50")),
                Segment(25f, 29.99f, "25 - 30", getString(R.string.bmi_high), parseColor("#f4c836")),
                Segment(30f, 50f, "> 30", getString(R.string.bmi_too_high), parseColor("#f44336"))
            )
        )
        bmiChart.setValue(bmi.toFloat().round(2))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
