package com.statsup

import java.util.*

object ActivityRepository {

    private val listeners: MutableList<Listener<List<Activity>>> = ArrayList()
    private var activities: MutableList<Activity> = mutableListOf()

    fun addAll(value: List<Activity>) {
        activities.addAll(value)
        listeners.forEach { it.update(activities) }
    }

    fun listen(listener: Listener<List<Activity>>) {
        listeners.add(listener)
    }
}
