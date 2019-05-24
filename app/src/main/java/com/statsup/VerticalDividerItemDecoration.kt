package com.statsup

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class VerticalDividerItemDecoration(private val verticalDividerHeight: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.bottom = verticalDividerHeight
    }
}