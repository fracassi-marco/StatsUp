package com.statsup

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import com.statsup.Variation.percentage
import com.statsup.barchart.Bar
import kotlinx.android.synthetic.main.chart_pager_item.view.*
import kotlinx.android.synthetic.main.overview_item.view.*

class AnnualChartsPagerAdapter(private val context: Context) : ActivityPagerAdapter() {

    private lateinit var stats: Stats
    private lateinit var activities: Activities

    override fun update(stats: Stats, activities: List<Activity>) {
        this.stats = stats
        this.activities =  Activities(activities, stats.provider)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.chart_pager_item, container, false)

        val selectedActivities = activities.filterByYear(position)
        val year = selectedActivities.year()
        view.title.text = year.asString()
        view.previous_label.text =
            if (selectedActivities.isFirstYear()) "" else year.previous().asString()
        view.previous_image.visibility =
            if (selectedActivities.isFirstYear()) INVISIBLE else VISIBLE
        view.next_label.text =
            if (selectedActivities.isCurrentYear()) "" else year.next().asString()
        view.next_image.visibility = if (selectedActivities.isCurrentYear()) INVISIBLE else VISIBLE

        view.overview_item.apply {
            left_value.text = Measure.of(selectedActivities.total(), "", "")
            left_text.text = resources.getString(R.string.total)
            right_value.text = Measure.of(selectedActivities.average(), "", "")
            right_text.text = resources.getString(R.string.average)
        }

        updateTrendChart(view, selectedActivities)
        updateVsChart(view, selectedActivities)
        updateDayOfWeekChart(view, selectedActivities)
        updateSportBreakdownChart(view, selectedActivities)

        container.addView(view)

        return view
    }

    private fun updateTrendChart(view: View, selectedActivities: Activities) {
        AnnualChart(view.trend_chart, stats.color, stats.unit).refresh(selectedActivities)
    }

    private fun updateVsChart(view: View, activities: Activities) {
        if (!activities.isFirstYear()) {
            YearOverYearChart(view.vs_chart, view.vs_chart_title, stats.color).refresh(activities)
        } else {
            view.vs_chart.visibility = GONE
        }
    }

    private fun updateDayOfWeekChart(values: View, activities: Activities) {
        val bars = activities.byDayOfWeek().map {
            Bar(percentage(it.value, activities.total()), stats.color, it.key.label)
        }

        values.day_of_week_chart.setData(100, bars)
    }

    private fun updateSportBreakdownChart(values: View, activities: Activities) {
        val bars = activities.bySport().map {
            Bar(percentage(it.value, activities.total()), stats.color, values.resources.getString(it.key.title))
        }

        values.sport_breakdown_chart.setData(100, bars)
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
        val view = LayoutInflater.from(context).inflate(R.layout.chart_pager_item, container, false)

        val selectedActivities = activities
        view.title.text = container.resources.getString(R.string.period_ever)
        view.previous_label.visibility = GONE
        view.previous_image.visibility = GONE
        view.next_label.visibility = GONE
        view.next_image.visibility = GONE

        view.overview_item.apply {
            left_value.text = Measure.of(selectedActivities.total(), "", "")
            left_text.text = resources.getString(R.string.total)
            right_value.text = Measure.of(selectedActivities.average(), "", "")
            right_text.text = resources.getString(R.string.average)
        }

        updateTrendChart(view, selectedActivities)
        updateVsChart(view)
        updateDayOfWeekChart(view, selectedActivities)
        updateSportBreakdownChart(view, selectedActivities)

        container.addView(view)

        return view
    }

    private fun updateTrendChart(view: View, selectedActivities: Activities) {
        EverChart(view.trend_chart, stats.color, stats.unit).refresh(selectedActivities)
    }

    private fun updateVsChart(view: View) {
        view.vs_chart.visibility = GONE
    }

    private fun updateDayOfWeekChart(values: View, activities: Activities) {
        val bars = activities.byDayOfWeek().map {
            Bar(percentage(it.value, activities.total()), stats.color, it.key.label)
        }

        values.day_of_week_chart.setData(100, bars)
    }

    private fun updateSportBreakdownChart(values: View, activities: Activities) {
        val bars = activities.bySport().map {
            Bar(percentage(it.value, activities.total()), stats.color, values.resources.getString(it.key.title))
        }

        values.sport_breakdown_chart.setData(100, bars)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}

    override fun isViewFromObject(view: View, instance: Any) = view == instance

    override fun getCount() = 1
}

class MonthlyChartsPagerAdapter(
    private val context: Context
) : ActivityPagerAdapter() {

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
        val view = LayoutInflater.from(context).inflate(R.layout.chart_pager_item, container, false)

        val selectedActivities = activities.filterByMonth(position)
        val month = selectedActivities.month()
        view.title.text = month.asString()
        view.previous_label.text =
            if (selectedActivities.isFirstMonth()) "" else month.previous().asString()
        view.previous_image.visibility =
            if (selectedActivities.isFirstMonth()) INVISIBLE else VISIBLE
        view.next_label.text =
            if (selectedActivities.isCurrentMonth()) "" else month.next().asString()
        view.next_image.visibility = if (selectedActivities.isCurrentMonth()) INVISIBLE else VISIBLE

        view.overview_item.apply {
            left_value.text = Measure.of(selectedActivities.total(), "", "")
            left_text.text = resources.getString(R.string.total)
            right_value.text = Measure.of(selectedActivities.average(), "", "")
            right_text.text = resources.getString(R.string.average)
        }

        updateTrendChart(view, selectedActivities)
        updateVsChart(view, selectedActivities)
        updateDayOfWeekChart(view, selectedActivities)
        updateSportBreakdownChart(view, selectedActivities)

        container.addView(view)

        return view
    }

    private fun updateTrendChart(view: View, selectedActivities: Activities) {
        MonthlyChart(view.trend_chart, stats.color, stats.unit).refresh(selectedActivities)
    }

    private fun updateVsChart(view: View, activities: Activities) {
        if (!activities.isFirstMonth()) {
            MonthOverMonthChart(view.vs_chart, view.vs_chart_title, stats.color).refresh(activities)
        } else {
            view.vs_chart.visibility = GONE
        }
    }

    private fun updateDayOfWeekChart(values: View, activities: Activities) {
        val bars = activities.byDayOfWeek().map {
            Bar(percentage(it.value, activities.total()), stats.color, it.key.label)
        }

        values.day_of_week_chart.setData(100, bars)
    }

    private fun updateSportBreakdownChart(values: View, activities: Activities) {
        val bars = activities.bySport().map {
            Bar(percentage(it.value, activities.total()), stats.color, values.resources.getString(it.key.title))
        }

        values.sport_breakdown_chart.setData(100, bars)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}

    override fun isViewFromObject(view: View, instance: Any) = view == instance

    override fun getCount() = activities.months().size
}

abstract class ActivityPagerAdapter : PagerAdapter(){
    abstract fun update(stats: Stats, activities: List<Activity>)
}