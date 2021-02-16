package com.statsup

import android.content.Context
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.value_marker.view.*


class ValueMarker(context: Context, private val xFormatter: ValueFormatter, private val yFormatter: ValueFormatter) : MarkerView(context, R.layout.value_marker) {

    override fun refreshContent(e: Entry, highlight: Highlight?) {
        marker_text.text = context.getString(R.string.weight_marker, xFormatter.getFormattedValue(e.x), yFormatter.getFormattedValue(e.y))
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}