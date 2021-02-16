package com.statsup.barchart

import android.content.Context
import android.graphics.Color.BLACK
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.statsup.R
import com.statsup.R.styleable.*
import kotlinx.android.synthetic.main.bar.view.*

class HorizontalBarChart(mCtx: Context, attrs: AttributeSet) : FrameLayout(mCtx, attrs) {
    private var spacesBetweenBars = 0
    private var labelColor = 0
    private var barHeight = 0
    private var labelSize = 0f
    private var valueSuffix = ""

    init {
        val attributes = context.obtainStyledAttributes(attrs, HorizontalBarChart, 0, 0)
        barHeight = attributes.getDimensionPixelSize(
            HorizontalBarChart_bar_height,
            convertDpToPixel(20f, context)
        )
        labelSize = convertPixelsToDp(
            attributes.getDimensionPixelSize(
                HorizontalBarChart_label_size, convertDpToPixel(15f, context)
            ).toFloat(), context
        )
        labelColor = attributes.getColor(HorizontalBarChart_label_color, BLACK)
        spacesBetweenBars = attributes.getDimensionPixelSize(
            HorizontalBarChart_spaces_between_bars,
            convertDpToPixel(5f, context)
        )
        valueSuffix = attributes.getString(HorizontalBarChart_value_suffix) ?: ""
        attributes.recycle()
    }

    private fun initLayout(): LinearLayout {
        val linearParentLayout = LinearLayout(context)
        linearParentLayout.orientation = LinearLayout.VERTICAL
        linearParentLayout.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        linearParentLayout.gravity = Gravity.START
        return linearParentLayout
    }

    fun setData(maxBarValue: Int, bars: List<Bar>) {
        removeAllViewsInLayout()
        val linearParentLayout = initLayout()
        addView(linearParentLayout)
        val maxLabelSize = bars.map { it.label.length }.maxOrNull() ?: 0
        for (bar in bars) {
            getDimension(linearParentLayout, object : DimensionReceivedCallback {
                override fun onDimensionReceived(dimension: Int) {
                    createBar(maxBarValue + (7 * maxLabelSize), dimension, bar, linearParentLayout, maxLabelSize)
                }
            })
        }
    }

    private fun createBar(
        maxBarValue: Int,
        dimension: Int,
        bar: Bar,
        linearParentLayout: LinearLayout,
        maxLabelSize: Int
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.bar, linearParentLayout, false)
        view.linear_bar.setBackgroundColor(bar.color)
        view.text_view_bar_label.apply {
            text = bar.label.padEnd(maxLabelSize, ' ')
            textSize = labelSize
            setTextColor(labelColor)
        }
        view.text_view_raters.text = "${bar.value}${valueSuffix}"
        view.linear_bar.layoutParams.width = dimension * bar.value / maxBarValue
        view.layoutParams.height = barHeight
        (view.layoutParams as MarginLayoutParams).bottomMargin = spacesBetweenBars
        linearParentLayout.addView(view)
    }

    private fun getDimension(view: View, listener: DimensionReceivedCallback) {
        view.viewTreeObserver
            .addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    listener.onDimensionReceived(view.width)
                }
            })
    }

    private interface DimensionReceivedCallback {
        fun onDimensionReceived(dimension: Int)
    }

    private fun convertDpToPixel(dp: Float, context: Context): Int {
        return (dp * (context.resources.displayMetrics.densityDpi.toFloat() /
                DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    private fun convertPixelsToDp(px: Float, context: Context): Float {
        return (px / (context.resources
            .displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT))
    }
}