package com.statsup.barchart

import android.content.Context
import android.graphics.Color
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
import java.util.*

class HorizontalBarChart(mCtx: Context, attrs: AttributeSet) : FrameLayout(mCtx, attrs) {
    private var mBarSpaces = 0
    private var mBarTextColor = 0
    private var mBarDimension = 0
    private var mBarTextSize = 0
    private lateinit var linearParentLayout: LinearLayout

    init {
        val attributes = context.obtainStyledAttributes(attrs, HorizontalBarChart, 0, 0)
        mBarDimension = attributes.getDimensionPixelSize(
            HorizontalBarChart_width,
            convertDpToPixel(20f, context)
        )
        mBarTextSize = convertPixelsToDp(
            attributes.getDimensionPixelSize(HorizontalBarChart_text_size, convertDpToPixel(15f, context)
            ).toFloat(), context
        )
        mBarTextColor = attributes.getColor(HorizontalBarChart_text_color, Color.BLACK)
        mBarSpaces = attributes.getDimensionPixelSize(HorizontalBarChart_spaces, convertDpToPixel(5f, context))
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
            textSize = mBarTextSize.toFloat()
            setTextColor(mBarTextColor)
        }
        view.text_view_raters.text = String.format(Locale.getDefault(), "%s", bar.value)
        view.linear_bar.layoutParams.width = dimension * bar.value / maxBarValue
        view.layoutParams.height = mBarDimension
        (view.layoutParams as MarginLayoutParams).bottomMargin = mBarSpaces
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

    private fun convertPixelsToDp(px: Float, context: Context): Int {
        return (px / (context.resources
            .displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }
}