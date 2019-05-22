package com.statsup

class Group {
    fun byMonths(activities: List<Activity>) : Map<Month, List<Activity>> {

        if(activities.isEmpty()) {
            return emptyMap()
        }

        val ordered = activities.sortedBy { it.dateInMillis }

        val result = mutableMapOf<Month, List<Activity>>()
        val oldest = ordered.first().date()
        val newest = ordered.last().date()

        for (year in oldest.year..newest.year) {
            for (month in 1..12) {
                val filter = ordered.filter {
                    it.date().year == year && it.date().monthOfYear == month
                }
                result[Month(year, month)] = filter
            }
        }

        return result
    }
}