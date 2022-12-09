package com.statsup

import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hookedonplay.decoviewlib.charts.SeriesItem
import com.hookedonplay.decoviewlib.events.DecoEvent
import com.statsup.Content.showActivitiesOrEmptyPage
import com.statsup.Variation.percentage
import com.statsup.calendar.CompactCalendarView
import com.statsup.databinding.ActivityDashboardFragmentBinding
import java.util.*
import java.util.Calendar.MONDAY


class ActivityDashboardFragment : Fragment() {

    private var _binding: ActivityDashboardFragmentBinding? = null
    private val binding get() = _binding!!
    private val labels: Queue<String> = LinkedList(listOf("stamina", "frequency", "duration", "distance"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActivityDashboardFragmentBinding.inflate(inflater, container, false)

        val curMonth = Month()
        val prevMonth = curMonth.previous()

        binding.month.text = curMonth.asString()

        val goals = Goals(ActivityRepository.all(), curMonth)
        binding.frequencyValue.text = goals.currentFrequency()
        binding.durationValue.text = goals.currentDuration()
        binding.distanceValue.text = goals.currentDistance()

        addCircle(0f, color(R.color.stamina), goals.staminaRatio(), 64f, curMonth)
        addCircle(74f, color(R.color.frequency), goals.frequencyRatio(), 32f, curMonth)
        addCircle(116f, color(R.color.duration), goals.durationRatio(), 32f, curMonth)
        addCircle(158f, color(R.color.distance), goals.distanceRatio(), 32f, curMonth)

        binding.arc.setOnClickListener {
            val next = labels.remove()
            labels.add(next)

            when (next) {
                "frequency" -> {
                    binding.percent.textSize = 30f
                    binding.percent.text = String.format("%.0f%%", goals.frequencyRatio())
                    binding.progress.text = goals.frequencyProgress()
                    binding.progress.visibility = VISIBLE
                }
                "duration" -> {
                    binding.percent.textSize = 30f
                    binding.percent.text = String.format("%.0f%%", goals.durationRatio())
                    binding.progress.text = goals.durationProgress()
                    binding.progress.visibility = VISIBLE
                }
                "distance" -> {
                    binding.percent.textSize = 30f
                    binding.percent.text = String.format("%.0f%%", goals.distanceRatio())
                    binding.progress.text = goals.distanceProgress()
                    binding.progress.visibility = VISIBLE
                }
                else -> {
                    binding.percent.textSize = 44f
                    binding.percent.text = String.format("%.0f%%", goals.staminaRatio())
                    binding.progress.visibility = GONE
                }
            }

            val color = color(resources.getIdentifier(next, "color", requireContext().packageName))
            binding.percent.setTextColor(color)
            binding.percentLabel.setTextColor(color)
            binding.progress.setTextColor(color)

            binding.percentLabel.text = getString(resources.getIdentifier(next, "string", requireContext().packageName))
        }

        binding.arc.performClick()

        val currentActivities = ActivityRepository.ofMonth(curMonth)
        val previousActivities = ActivityRepository.ofMonth(prevMonth)
        VsChart(binding.vsChart, binding.vsChartTitle, color(R.color.distance)).refresh(
            currentActivities.cumulativeByDay(curMonth, Stats.DISTANCE.provider),
            previousActivities.cumulativeByDay(prevMonth, Stats.DISTANCE.provider),
            curMonth.asString(), prevMonth.asString())

        fillCalendar(binding.calendar, currentActivities)

        showActivitiesOrEmptyPage(binding.noActivitiesLayout, binding.content)

        return binding.root
    }

    private fun fillCalendar(
        calendar: CompactCalendarView,
        currentActivities: List<Activity>
    ) {
        calendar.addEvents(currentActivities)
    }

    private fun color(color: Int) = requireContext().getColor(color)

    private fun addCircle(inset: Float, color: Int, value: Float, width: Float, curMonth: Month) {
        binding.arc.addSeries(
            SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                .setRange(0f, 100f, 100f)
                .setInset(PointF(inset, inset))
                .setLineWidth(width)
                .build()
        )

        val expectedValue = percentage(curMonth.numberOfDays(), curMonth.maxNumberOfDays()).toFloat()
        binding.arc.addSeries(
            SeriesItem.Builder(color)
                .setRange(0f, 100f, expectedValue)
                .setInset(PointF(inset, inset))
                .setLineWidth(width/2)
                .build()
        )

        val series = SeriesItem.Builder(color)
            .setRange(0f, 100f, 0f)
            .setInset(PointF(inset, inset))
            .setLineWidth(width)
            .build()

        val seriesIndex = binding.arc.addSeries(series)

        binding.arc.addEvent(DecoEvent.Builder(value).setIndex(seriesIndex).setDelay((inset.toLong() * 10) + 60).build())
    }
}