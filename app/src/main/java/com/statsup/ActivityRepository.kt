package com.statsup

class ActivityRepository {

    companion object {
        private var activities: MutableList<Activity> = mutableListOf()
    }

    fun addAll(value: List<Activity>) {
        activities.addAll(value)
    }

    fun all(): List<Activity> {
        return activities
    }
}
