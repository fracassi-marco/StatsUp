package com.statsup

import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.statsup.Content.showActivitiesOrEmptyPage
import com.statsup.Variation.percentage
import com.statsup.databinding.ActivityDashboardFragmentBinding
import java.util.*


class ActivityDashboardFragment : Fragment() {

    private var _binding: ActivityDashboardFragmentBinding? = null
    private val binding get() = _binding!!
    private val labels: Queue<String> = LinkedList(listOf("frequency", "duration", "distance", "stamina"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityDashboardFragmentBinding.inflate(inflater, container, false)
        setHasOptionsMenu(false)

        val currentActivities = ActivityRepository.ofMonth(Month())
        binding.frequencyValue.text = Measure.frequency(Stats.FREQUENCY.provider(currentActivities))
        binding.durationValue.text = Measure.hm(currentActivities.sumOf { activity -> activity.durationInSeconds })
        binding.distanceValue.text = Measure.of(Stats.DISTANCE.provider(currentActivities), "Km", "", "- ")

        val previousActivities = ActivityRepository.ofMonth(Month().previous())
        val frequency = percentage(Stats.FREQUENCY.provider(currentActivities), Stats.FREQUENCY.provider(previousActivities)).toDouble()
        val duration = percentage(currentActivities.sumOf { a -> a.durationInSeconds }, previousActivities.sumOf { a -> a.durationInSeconds })
        val distance = percentage(Stats.DISTANCE.provider(currentActivities), Stats.DISTANCE.provider(previousActivities))
        val staminaValue = ((frequency * 2) + duration + (distance * 0.5)) / 3.5
        val stamina = addCircle(0f, requireContext().getColor(R.color.stamina), staminaValue.toFloat(), 64f)
        addCircle(74f, Stats.FREQUENCY.color, frequency.toFloat(), 32f)
        addCircle(116f, Stats.DURATION.color, duration.toFloat(), 32f)
        addCircle(158f, Stats.DISTANCE.color, distance.toFloat(), 32f)
        addLabel(stamina)

        binding.arc.setOnClickListener {
            val next = labels.remove()
            labels.add(next)
            val value = when (next) {
                "frequency" -> frequency
                "duration" -> duration.toDouble()
                "distance" -> distance.toDouble()
                else -> staminaValue
            }

            binding.percent.setTextColor(requireContext().getColor(resources.getIdentifier(next, "color", requireContext().packageName)))
            binding.percent.text = String.format("%.0f%%", value)
            binding.percentLabel.text = getString(resources.getIdentifier(next, "string", requireContext().packageName))
            binding.percentLabel.setTextColor(requireContext().getColor(resources.getIdentifier(next, "color", requireContext().packageName)))
        }

        showActivitiesOrEmptyPage(binding.noActivitiesLayout, binding.content)

        return binding.root
    }

    private fun addLabel(stamina: SeriesItem) {
        stamina.addArcSeriesItemListener(object : SeriesItem.SeriesItemListener {
            override fun onSeriesItemAnimationProgress(
                percentComplete: Float,
                currentPosition: Float
            ) {
                val percentFilled = (currentPosition - stamina.minValue) / (stamina.maxValue - stamina.minValue)
                binding.percent.text = String.format("%.0f%%", percentFilled * 100f)
                binding.percent.setTextColor(requireContext().getColor(R.color.stamina))
                binding.percentLabel.text = getString(R.string.stamina)
                binding.percentLabel.setTextColor(requireContext().getColor(R.color.stamina))
            }

            override fun onSeriesItemDisplayProgress(percentComplete: Float) {
            }
        })
    }

    private fun addCircle(inset: Float, color: Int, value: Float, width: Float): SeriesItem {
        binding.arc.addSeries(
            SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(0f, 100f, 100f)
                .setInset(PointF(inset, inset))
                .setLineWidth(width)
                .build()
        )

        val series = SeriesItem.Builder(color)
            .setRange(0f, 100f, 0f)
            .setInset(PointF(inset, inset))
            .setLineWidth(width)
            .build()

        val seriesIndex = binding.arc.addSeries(series)

        binding.arc.addEvent(DecoEvent.Builder(value).setIndex(seriesIndex).setDelay((inset.toLong() * 10) + 50).build())

        return series
    }
}