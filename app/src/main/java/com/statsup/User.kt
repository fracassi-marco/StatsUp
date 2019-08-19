package com.statsup

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Parcelize
data class User(
    var name: String = "Super uper",
    var image: String = "hero0",
    var height: Int
) : Parcelable {

    constructor() : this("Super uper", "hero0", 0)

    lateinit var id: String
}