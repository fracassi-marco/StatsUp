package com.statsup.barchart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.*


class SmoothLineChart constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val mPaint: Paint
    private val mPath: Path
    private val mCircleSize: Float
    private val mStrokeSize: Float
    private val mBorder: Float
    private var mValues: Array<PointF> = emptyArray()
    private val mMinY = 0f
    private var mMaxY = 0f

    fun setData(values: Array<PointF>) {
        mValues = values
        if (values.isNotEmpty()) {
            mMaxY = values[0].y
            //mMinY = values[0].y;
            for (point in values) {
                val y = point.y
                if (y > mMaxY) mMaxY = y
                /*if (y < mMinY)
					mMinY = y;*/
            }
        }
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (mValues.isEmpty()) return
        val size = mValues.size
        val height = measuredHeight - 2 * mBorder
        val width = measuredWidth - 2 * mBorder
        val left = mValues[0].x
        val right = mValues[mValues.size - 1].x
        val dX: Float = if (right - left > 0) right - left else 2f
        val dY: Float = if (mMaxY - mMinY > 0) mMaxY - mMinY else 2f
        mPath.reset()

        // calculate point coordinates
        val points: MutableList<PointF> = ArrayList(size)
        for (point in mValues) {
            val x = mBorder + (point.x - left) * width / dX
            val y = mBorder + height - (point.y - mMinY) * height / dY
            points.add(PointF(x, y))
        }

        // calculate smooth path
        var lX = 0f
        var lY = 0f
        mPath.moveTo(points[0].x, points[0].y)
        for (i in 1 until size) {
            val p = points[i] // current point

            // first control point
            val p0 = points[i - 1] // previous point
            val d0 = Math.sqrt(
                Math.pow(
                    p.x - p0.x.toDouble(),
                    2.0
                ) + Math.pow(p.y - p0.y.toDouble(), 2.0)
            ).toFloat() // distance between p and p0
            val x1 = Math.min(
                p0.x + lX * d0,
                (p0.x + p.x) / 2
            ) // min is used to avoid going too much right
            val y1 = p0.y + lY * d0

            // second control point
            val p1 = points[if (i + 1 < size) i + 1 else i] // next point
            val d1 = Math.sqrt(
                Math.pow(
                    p1.x - p0.x.toDouble(),
                    2.0
                ) + Math.pow(p1.y - p0.y.toDouble(), 2.0)
            ).toFloat() // distance between p1 and p0 (length of reference line)
            lX =
                (p1.x - p0.x) / d1 * SMOOTHNESS // (lX,lY) is the slope of the reference line
            lY = (p1.y - p0.y) / d1 * SMOOTHNESS
            val x2 = Math.max(
                p.x - lX * d0,
                (p0.x + p.x) / 2
            ) // max is used to avoid going too much left
            val y2 = p.y - lY * d0

            // add line
            mPath.cubicTo(x1, y1, x2, y2, p.x, p.y)
        }


        // draw path
        mPaint.color = CHART_COLOR
        mPaint.style = Paint.Style.STROKE
        canvas.drawPath(mPath, mPaint)

        // draw area
        if (size > 0) {
            mPaint.style = Paint.Style.FILL
            mPaint.color = CHART_COLOR and 0xFFFFFF or 0x10000000
            mPath.lineTo(points[size - 1].x, height + mBorder)
            mPath.lineTo(points[0].x, height + mBorder)
            mPath.close()
            canvas.drawPath(mPath, mPaint)
        }

        // draw circles
        mPaint.color = CHART_COLOR
        mPaint.style = Paint.Style.FILL_AND_STROKE
        for (point in points) {
            canvas.drawCircle(point.x, point.y, mCircleSize / 2, mPaint)
        }
        mPaint.style = Paint.Style.FILL
        mPaint.color = Color.WHITE
        for (point in points) {
            canvas.drawCircle(point.x, point.y, (mCircleSize - mStrokeSize) / 2, mPaint)
        }
    }

    companion object {
        private const val CHART_COLOR = -0xff6634
        private const val CIRCLE_SIZE = 8
        private const val STROKE_SIZE = 2
        private const val SMOOTHNESS =
            0.3f // the higher the smoother, but don't go over 0.5
    }

    init {
        val scale = context.resources.displayMetrics.density
        mCircleSize = scale * CIRCLE_SIZE
        mStrokeSize = scale * STROKE_SIZE
        mBorder = mCircleSize
        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = mStrokeSize
        mPath = Path()
    }
}