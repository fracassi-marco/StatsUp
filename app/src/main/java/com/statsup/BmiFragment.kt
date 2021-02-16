package com.statsup

import android.graphics.Color.parseColor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.bmi_fragment.view.*
import kotlinx.android.synthetic.main.no_items_layout.view.*
import kotlinx.android.synthetic.main.overview_item.view.*
import mobi.gspd.segmentedbarview.Segment
import mobi.gspd.segmentedbarview.SegmentedBarView

class BmiFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.bmi_fragment, container, false)

        val weights = WeightRepository.all()
        val height = UserRepository.user.height
        setVisibleView(view.content, view.no_items_layout, height)

        if (height != 0) {
            val minWeight = weights.minByOrNull { it.kilograms }!!
            val currentWeight = weights.first()
            val maxWeight = weights.maxByOrNull { it.kilograms }!!

            updateOverviews(
                view.bmi_oxford_min_max_overview_item,
                Bmi.labelForOxford(minWeight, height),
                Bmi.labelForOxford(currentWeight, height),
                Bmi.labelForOxford(maxWeight, height)
            )
            updateChart(view.bmi_oxford_chart, Bmi.oxford(currentWeight, height))

            updateOverviews(
                view.bmi_classic_min_max_overview_item,
                Bmi.labelForClassic(minWeight, height),
                Bmi.labelForClassic(currentWeight, height),
                Bmi.labelForClassic(maxWeight, height)
            )
            updateChart(view.bmi_classic_chart, Bmi.classic(currentWeight, height))
        }

        return view
    }

    private fun updateOverviews(
        minMaxOverviewItem: View,
        min: String, current: String, max: String
    ) {
        minMaxOverviewItem.left_value.text = min
        minMaxOverviewItem.left_value.textSize = 21f
        minMaxOverviewItem.left_text.text = getString(R.string.bmi_min)

        minMaxOverviewItem.center_value.text = current
        minMaxOverviewItem.center_value.textSize = 26f
        minMaxOverviewItem.center_text.text = getString(R.string.bmi_current)

        minMaxOverviewItem.right_value.text = max
        minMaxOverviewItem.right_value.textSize = 21f
        minMaxOverviewItem.right_text.text = getString(R.string.bmi_max)
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
}
