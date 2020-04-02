package com.statsup

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.overview_item.view.*
import kotlinx.android.synthetic.main.weight_info_fragment.view.*
import java.util.*


class WeightInfoFragment : Fragment() {

    private fun updateUi(
        weights: List<Weight>,
        weightOverviewItem: View,
        howLongOverviewItem: View
    ) {
        weightOverviewItem.left_text.text = "Numero di pesate"
        weightOverviewItem.left_value.text = weights.size.toString()

        howLongOverviewItem.left_text.text = "Prima pesata"
        howLongOverviewItem.left_value.text = weights.last().date().toString("dd/MM/yyyy")
        howLongOverviewItem.left_value.textSize = 21f
        howLongOverviewItem.left_value.setTextColor(Color.BLACK)

        howLongOverviewItem.right_text.text = "Ultima pesata"
        howLongOverviewItem.right_value.text = weights.first().date().toString("dd/MM/yyyy")
        howLongOverviewItem.right_value.textSize = 21f
        howLongOverviewItem.right_value.setTextColor(Color.BLACK)

        howLongOverviewItem.center_text.text = "Da"
        howLongOverviewItem.center_value.textSize = 16f
        howLongOverviewItem.center_value.text = since(weights)
    }

    private fun since(weights: List<Weight>): String {
        if(weights.isEmpty())
            return "0 anni 0 mesi"

        val firstMeasure = GregorianCalendar().apply { time = weights.last().date().toDate() }
        val today = GregorianCalendar().apply { time = Date() }
        val yearsInBetween = today.get(Calendar.YEAR) - firstMeasure.get(Calendar.YEAR)
        val monthsDiff = today.get(Calendar.MONTH) - firstMeasure.get(Calendar.MONTH)
        val ageInMonths = yearsInBetween * 12 + monthsDiff
        val years = ageInMonths / 12
        val months = ageInMonths - (12 * years)
        return "$years anni $months mesi"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.weight_info_fragment, container, false)
        val weightOverviewItem  = view.weightOverviewItem
        val howLongOverviewItem = view.howLongOverviewItem

        val weights = WeightRepository.all()

        updateUi(weights, weightOverviewItem, howLongOverviewItem)

        return view
    }
}
