package com.statsup

import android.view.View

class NoActivitiesListener(private val viewWithActivities: View, private val viewWithoutActivities: View) :
    Listener<List<Activity>> {
    override fun update(subject: List<Activity>) {
        if(subject.isEmpty()) {
            viewWithoutActivities.visibility = View.VISIBLE
            viewWithActivities.visibility = View.GONE
        }
        else {
            viewWithoutActivities.visibility = View.GONE
            viewWithActivities.visibility = View.VISIBLE
        }
    }

}