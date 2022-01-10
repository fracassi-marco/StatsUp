package com.statsup

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(var height: Int = 0) : Parcelable