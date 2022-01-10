package com.statsup

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.statsup.databinding.WeightInfoFragmentBinding
import java.util.*


class WeightInfoFragment : Fragment() {

    private var _binding: WeightInfoFragmentBinding? = null
    private val binding get() = _binding!!

    private fun updateUi(weights: List<Weight>) {
        binding.weightOverviewItem.leftText.text = "Numero di pesate"
        binding.weightOverviewItem.leftValue.text = weights.size.toString()

        binding.howLongOverviewItem.leftText.text = "Prima pesata"
        binding.howLongOverviewItem.leftValue.text = weights.last().date().toString("dd/MM/yyyy")
        binding.howLongOverviewItem.leftValue.textSize = 21f
        binding.howLongOverviewItem.leftValue.setTextColor(Color.BLACK)

        binding.howLongOverviewItem.rightText.text = "Ultima pesata"
        binding.howLongOverviewItem.rightValue.text = weights.first().date().toString("dd/MM/yyyy")
        binding.howLongOverviewItem.rightValue.textSize = 21f

        binding.howLongOverviewItem.centerText.text = "Da"
        binding.howLongOverviewItem.centerValue.textSize = 16f
        binding.howLongOverviewItem.centerValue.text = since(weights)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WeightInfoFragmentBinding.inflate(inflater, container, false)

        updateUi(WeightRepository.all())

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
