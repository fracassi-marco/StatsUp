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
    private lateinit var linearParentLayout: LinearLayout

    init {
        val attributes = context.obtainStyledAttributes(attrs, HorizontalBarChart, 0, 0)
        barHeight = attributes.getDimensionPixelSize(
            HorizontalBarChart_bar_height,
            convertDpToPixel(20f, context)
        )
        labelSize = convertPixelsToDp(
            attributes.getDimensionPixelSize(HorizontalBarChart_label_size, convertDpToPixel(15f, context)
            ).toFloat(), context
        )
        labelColor = attributes.getColor(HorizontalBarChart_label_color, BLACK)
        spacesBetweenBars = attributes.getDimensionPixelSize(HorizontalBarChart_spaces_between_bars, convertDpToPixel(5f, context))
        valueSuffix = attributes.getString(HorizontalBarChart_value_suffix) ?: ""
        attributes.recycle()
        initLayout()
    }

    private fun initLayout() {
        linearParentLayout = LinearLayout(context)
        linearParentLayout.orientation = LinearLayout.VERTICAL
        linearParentLayout.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        linearParentLayout.gravity = Gravity.START
        this.addView(linearParentLayout)
    }

    fun setData(maxBarValue: Int, bars: List<Bar>) {
        for (bar in bars) {
            addBar(maxBarValue + 20, bar)
        }
    }

    private fun createBar(maxBarValue: Int, dimension: Int, bar: Bar) {
        val view = LayoutInflater.from(context).inflate(R.layout.bar, linearParentLayout, false)
        view.linear_bar.setBackgroundColor(bar.color)
        view.text_view_bar_label.apply {
            text = bar.label
            textSize = labelSize
            setTextColor(labelColor)
        }
        view.text_view_raters.text =  "${bar.value}${valueSuffix}"
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

    private fun addBar(maxBarValue: Int, bar: Bar) {
        getDimension(linearParentLayout, object : DimensionReceivedCallback {
            override fun onDimensionReceived(dimension: Int) {
                createBar(maxBarValue, dimension, bar)
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