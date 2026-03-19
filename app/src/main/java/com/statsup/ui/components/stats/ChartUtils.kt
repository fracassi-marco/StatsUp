package com.statsup.ui.components.stats

internal fun barValueFormatter(): (Float) -> String = { value -> if (value == 0f) "" else "%.0f".format(value) }
