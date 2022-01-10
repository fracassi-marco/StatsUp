package com.statsup

import android.content.Context
import android.view.LayoutInflater
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.statsup.databinding.ValueMarkerBinding


class ValueMarker(context: Context, private val xFormatter: ValueFormatter, private val yFormatter: ValueFormatter) : MarkerView(context, R.layout.value_marker) {

    constructor(context: Context): this(context, DefaultAxisValueFormatter(1), DefaultAxisValueFormatter(1))

    private var _binding: ValueMarkerBinding? = null
    private val binding get() = _binding!!

    init {
        _binding = ValueMarkerBinding.inflate(LayoutInflater.from(context), this, false)
    }

    override fun refreshContent(e: Entry, highlight: Highlight?) {
        binding.markerText.text = context.getString(R.string.weight_marker, xFormatter.getFormattedValue(e.x), yFormatter.getFormattedValue(e.y))
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}