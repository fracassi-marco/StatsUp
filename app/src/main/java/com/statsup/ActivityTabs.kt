package com.statsup

import android.graphics.Color.rgb
import android.support.v4.app.Fragment

enum class ActivityTabs(val position: Int, val label: String, val color: Int, val fragment: Fragment, val unit: String,  val valueProvider: (Activities) -> Value) {
    FREQUENCY(0, "Frequenza", rgb(76, 175, 80), FrequencyFragment(), "Numero di allenamenti ", { Frequencies(it) }),
    DURATION(1, "Durata", rgb(33, 150, 243), DurationFragment(), "Ore di allenamento ", { Durations(it) }),
    DISTANCE(2, "Distanza", rgb(244, 67, 54), DistanceFragment(), "Chilometri percorsi ", { Distances(it) });

    companion object {
        fun at(position: Int): ActivityTabs {
            return values().single { it.position == position }
        }
    }
}
