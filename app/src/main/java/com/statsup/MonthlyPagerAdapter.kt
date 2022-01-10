package com.statsup

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.statsup.Variation.percentage
import com.statsup.barchart.Bar
import com.statsup.databinding.ChartPagerItemBinding

class AnnualChartsPagerAdapter(private val context: Context) : ActivityPagerAdapter() {

    private lateinit var stats: Stats
    private lateinit var activities: Activities

    override fun update(stats: Stats, activities: List<Activity>) {
        this.stats = stats
        this.activities =  Activities(activities, stats.provider)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = ChartPagerItemBinding.inflate(LayoutInflater.from(context), container, false)

        val selectedActivities = activities.filterByYear(position)
        val year = selectedActivities.year()
        binding.title.text = year.asString()
        binding.previousLabel.text =
            if (selectedActivities.isFirstYear()) "" else year.previous().asString()
        binding.previousImage.visibility =
            if (selectedActivities.isFirstYear()) INVISIBLE else VISIBLE
        binding.nextLabel.text =
            if (selectedActivities.isCurrentYear()) "" else year.next().asString()
        binding.nextImage.visibility = if (selectedActivities.isCurrentYear()) INVISIBLE else VISIBLE

        binding.overviewItem.apply {
            container.resources
            leftValue.text = Measure.of(selectedActivities.total(), "", "")
            leftText.text = container.resources.getString(R.string.total)
            rightValue.text = Measure.of(selectedActivities.average(), "", "")
            rightText.text = container.resources.getString(R.string.average)
        }

        updateTrendChart(binding, selectedActivities)
        updateVsChart(binding, selectedActivities)
        updateDayOfWeekChart(binding, selectedActivities)
        updateSportBreakdownChart(binding, selectedActivities, context.resources)

        container.addView(binding.root)

        return binding.root
    }

    private fun updateTrendChart(view: ChartPagerItemBinding, selectedActivities: Activities) {
        AnnualChart(view.trendChart, stats.color, stats.unit).refresh(selectedActivities)
    }

    private fun updateVsChart(view: ChartPagerItemBinding, activities: Activities) {
        if (!activities.isFirstYear()) {
            YearOverYearChart(view.vsChart, view.vsChartTitle, stats.color).refresh(activities)
        } else {
            view.vsChart.visibility = GONE
        }
    }

    private fun updateDayOfWeekChart(values: ChartPagerItemBinding, activities: Activities) {
        val bars = activities.byDayOfWeek().map {
            Bar(percentage(it.value, activities.total()), stats.color, it.key.label)
        }

        values.dayOfWeekChart.setData(100, bars)
    }

    private fun updateSportBreakdownChart(
        values: ChartPagerItemBinding,
        activities: Activities,
        resources: Resources
    ) {
        val bars = activities.bySport().map {
            Bar(percentage(it.value, activities.total()), stats.color, resources.getString(it.key.title))
        }

        values.sportBreakdownChart.setData(100, bars)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}

    override fun isViewFromObject(view: View, instance: Any) = view == instance

    override fun getCount() = activities.years().size
}

class EverChartsPagerAdapter(private val context: Context) : ActivityPagerAdapter() {

    private lateinit var stats: Stats
    private lateinit var activities: Activities

    override fun update(stats: Stats, activities: List<Activity>) {
        this.stats = stats
        this.activities =  Activities(activities, stats.provider)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = ChartPagerItemBinding.inflate(LayoutInflater.from(context), container, false)

        val selectedActivities = activities
        binding.title.text = container.resources.getString(R.string.period_ever)
        binding.previousLabel.visibility = GONE
        binding.previousImage.visibility = GONE
        binding.nextLabel.visibility = GONE
        binding.nextImage.visibility = GONE

        binding.overviewItem.apply {
            leftValue.text = Measure.of(selectedActivities.total(), "", "")
            leftText.text = container.resources.getString(R.string.total)
            rightValue.text = Measure.of(selectedActivities.average(), "", "")
            rightText.text = container.resources.getString(R.string.average)
        }

        updateTrendChart(binding, selectedActivities)
        updateVsChart(binding)
        updateDayOfWeekChart(binding, selectedActivities)
        updateSportBreakdownChart(binding, selectedActivities, container.resources)

        container.addView(binding.root)

        return binding.root
    }

    private fun updateTrendChart(view: ChartPagerItemBinding, selectedActivities: Activities) {
        EverChart(view.trendChart, stats.color, stats.unit).refresh(selectedActivities)
    }

    private fun updateVsChart(view: ChartPagerItemBinding) {
        view.vsChart.visibility = GONE
    }

    private fun updateDayOfWeekChart(values: ChartPagerItemBinding, activities: Activities) {
        val bars = activities.byDayOfWeek().map {
            Bar(percentage(it.value, activities.total()), stats.color, it.key.label)
        }

        values.dayOfWeekChart.setData(100, bars)
    }

    private fun updateSportBreakdownChart(
        values: ChartPagerItemBinding,
        activities: Activities,
        resources: Resources
    ) {
        val bars = activities.bySport().map {
            Bar(percentage(it.value, activities.total()), stats.color, resources.getString(it.key.title))
        }

        values.sportBreakdownChart.setData(100, bars)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}

    override fun isViewFromObject(view: View, instance: Any) = view == instance

    override fun getCount() = 1
}

class MonthlyChartsPagerAdapter(private val context: Context) : ActivityPagerAdapter() {

    private lateinit var stats: Stats
    private lateinit var activities: Activities

    override fun update(stats: Stats, activities: List<Activity>) {
        this.stats = stats
        this.activities =  Activities(activities, stats.provider)
    }

    override fun getItemPosition(obj: Any): Int {
        return POSITION_NONE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = ChartPagerItemBinding.inflate(LayoutInflater.from(context), container, false)

        val selectedActivities = activities.filterByMonth(position)
        val month = selectedActivities.month()
        binding.title.text = month.asString()
        binding.previousLabel.text =
            if (selectedActivities.isFirstMonth()) "" else month.previous().asString()
        binding.previousImage.visibility =
            if (selectedActivities.isFirstMonth()) INVISIBLE else VISIBLE
        binding.nextLabel.text =
            if (selectedActivities.isCurrentMonth()) "" else month.next().asString()
        binding.nextImage.visibility = if (selectedActivities.isCurrentMonth()) INVISIBLE else VISIBLE

        binding.overviewItem.apply {
            leftValue.text = Measure.of(selectedActivities.total(), "", "")
            leftText.text = container.resources.getString(R.string.total)
            rightValue.text = Measure.of(selectedActivities.average(), "", "")
            rightText.text = container.resources.getString(R.string.average)
        }

        updateTrendChart(binding, selectedActivities)
        updateVsChart(binding, selectedActivities)
        updateDayOfWeekChart(binding, selectedActivities)
        updateSportBreakdownChart(binding, selectedActivities, container.resources)

        container.addView(binding.root)

        return binding.root
    }

    private fun updateTrendChart(view: ChartPagerItemBinding, selectedActivities: Activities) {
        MonthlyChart(view.trendChart, stats.color, stats.unit).refresh(selectedActivities)
    }

    private fun updateVsChart(view: ChartPagerItemBinding, activities: Activities) {
        if (!activities.isFirstMonth()) {
            MonthOverMonthChart(view.vsChart, view.vsChartTitle, stats.color).refresh(activities)
        } else {
            view.vsChart.visibility = GONE
        }
    }

    private fun updateDayOfWeekChart(values: ChartPagerItemBinding, activities: Activities) {
        val bars = activities.byDayOfWeek().map {
            Bar(percentage(it.value, activities.total()), stats.color, it.key.label)
        }

        values.dayOfWeekChart.setData(100, bars)
    }

    private fun updateSportBreakdownChart(
        values: ChartPagerItemBinding,
        activities: Activities,
        resources: Resources
    ) {
        val bars = activities.bySport().map {
            Bar(percentage(it.value, activities.total()), stats.color, resources.getString(it.key.title))
        }

        values.sportBreakdownChart.setData(100, bars)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}

    override fun isViewFromObject(view: View, instance: Any) = view == instance

    override fun getCount() = activities.months().size
}

abstract class ActivityPagerAdapter : PagerAdapter(){
    abstract fun update(stats: Stats, activities: List<Activity>)
}