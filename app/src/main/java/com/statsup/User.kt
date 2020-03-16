package com.statsup

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(var height: Int = 0) : Parcelable