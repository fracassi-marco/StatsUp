package com.statsup

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE

class NoActivitiesListener<T>(private val viewWithItem: View, private val viewWithoutItem: View) :
    Listener<List<T>> {
    override fun update(subject: List<T>) {
        if(subject.isEmpty()) {
            viewWithoutItem.visibility = VISIBLE
            viewWithItem.visibility = GONE
        }
        else {
            viewWithoutItem.visibility = GONE
            viewWithItem.visibility = VISIBLE
        }
    }
}