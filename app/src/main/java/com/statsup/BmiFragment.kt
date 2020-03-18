package com.statsup

import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.bmi_fragment.view.*
import kotlinx.android.synthetic.main.no_items_layout.view.*
import kotlinx.android.synthetic.main.overview_item.view.*
import mobi.gspd.segmentedbarview.Segment
import mobi.gspd.segmentedbarview.SegmentedBarView

class BmiFragment : Fragment() {

    private lateinit var minMaxOverviewItem: View
    private lateinit var bmiChart: SegmentedBarView
    private lateinit var content: ConstraintLayout
    private lateinit var noItemsLayout: View
    private var weights = emptyList<Weight>()
    private var height: Int = 0

    private val userListener = object : Listener<User> {
        override fun update(subject: User) {
            height = subject.height
            setVisibleView()

            if(height != 0 && weights.isNotEmpty()) {
                updateBmiOverviews()
                updateBmiChart()
            }
        }
    }

    private fun setVisibleView() {
        if(height == 0) {
            content.visibility = View.GONE
            noItemsLayout.visibility = View.VISIBLE
        } else{
            content.visibility = View.VISIBLE
            noItemsLayout.visibility = View.GONE
        }
    }

    private val weightListener = object : Listener<List<Weight>> {
        override fun update(subject: List<Weight>) {
            weights = subject.sortedBy { it.dateInMillis }
            setVisibleView()

            if(height != 0 && weights.isNotEmpty()) {
                updateBmiOverviews()
                updateBmiChart()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bmi_fragment, container, false)
        bmiChart = view.bmiChart
        minMaxOverviewItem = view.min_max_overview_item
        content = view.content
        noItemsLayout = view.no_item_layout

        setVisibleView()

        UserRepository.listen(userListener)
        WeightRepository.listen("BmiFragment", weightListener)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        UserRepository.removeListener(userListener)
        WeightRepository.removeListener("BmiFragment")
    }

    private fun updateBmiOverviews() {
        minMaxOverviewItem.left_value.text = Bmi.labelFor(weights.minBy { it.kilograms }!!, height)
        minMaxOverviewItem.left_value.textSize = 21f
        minMaxOverviewItem.left_text.text = getString(R.string.bmi_min)

        minMaxOverviewItem.center_value.text = Bmi.labelFor(weights.last(), height)
        minMaxOverviewItem.center_value.textSize = 26f
        minMaxOverviewItem.center_text.text = getString(R.string.bmi_current)

        minMaxOverviewItem.right_value.text = Bmi.labelFor(weights.maxBy { it.kilograms }!!, height)
        minMaxOverviewItem.right_value.setTextColor(Color.BLACK)
        minMaxOverviewItem.right_value.textSize = 21f
        minMaxOverviewItem.right_text.text = getString(R.string.bmi_min)
    }

    private fun updateBmiChart() {
        bmiChart.setSegments(listOf(
            Segment(0f, 15.99f, "< 16", getString(R.string.bmi_too_low), Color.parseColor("#2196f3")),
            Segment(16f, 18.49f, "16 - 18.5", getString(R.string.bmi_low), Color.parseColor("#21daf3")),
            Segment(18.5f, 24.99f, "18.5 - 25", getString(R.string.bmi_normal), Color.parseColor("#4caf50")),
            Segment(25f, 29.99f, "25 - 30", getString(R.string.bmi_high), Color.parseColor("#f4c836")),
            Segment(30f, 50f, "> 30", getString(R.string.bmi_too_high), Color.parseColor("#f44336"))
        ))
        bmiChart.setValue(Bmi.valueFor(weights.last(), height).toFloat().round(2))
    }
}
