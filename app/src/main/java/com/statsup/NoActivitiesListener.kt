package com.statsup

import android.view.View

class NoActivitiesListener<T>(private val viewWithItem: View, private val viewWithoutItem: View) :
    Listener<List<T>> {
    override fun update(subject: List<T>) {
        if(subject.isEmpty()) {
            viewWithoutItem.visibility = View.VISIBLE
            viewWithItem.visibility = View.GONE
        }
        else {
            viewWithoutItem.visibility = View.GONE
            viewWithItem.visibility = View.VISIBLE
        }
    }
}