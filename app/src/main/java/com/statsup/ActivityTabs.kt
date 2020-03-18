package com.statsup

import android.graphics.Color.rgb
import android.support.v4.app.Fragment

enum class ActivityTabs(val position: Int, val label: String, val color: Int, val fragment: Fragment) {
    FREQUENCY(0, "Frequenza", rgb(76, 175, 80), FrequencyFragment()),
    DURATION(1, "Durata", rgb(33, 150, 243), DurationFragment()),
    DISTANCE(2, "Distanza", rgb(244, 67, 54), DistanceFragment());

    companion object {
        fun at(position: Int): ActivityTabs {
            return values().single { it.position == position }
        }
    }
}
