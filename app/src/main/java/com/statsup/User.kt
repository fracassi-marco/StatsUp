package com.statsup

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class User(
    var name: String = "Super uper",
    var image: String = "hero0",
    var id: String = ""
) : Parcelable