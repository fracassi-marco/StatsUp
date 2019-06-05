package com.statsup

import android.graphics.Color
import android.support.v4.app.Fragment

enum class Tabs(val position: Int, val label: String, val color: Int, val fragment: Fragment) {
    FREQUENCY(0, "Frequenza", Color.rgb(76, 175, 80), FrequencyFragment()),
    DURATION(1, "Durata", Color.rgb(33, 150, 243), DurationFragment()),
    DISTANCE(2, "Distanza", Color.rgb(244, 67, 54), DistanceFragment());

    companion object {
        fun at(position: Int): Tabs {
            return values().single { it.position == position }
        }
    }
}