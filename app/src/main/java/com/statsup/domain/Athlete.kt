package com.statsup.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Athlete(
    @PrimaryKey var id: Long,
    var username: String,
    var resourceState: Int? = null,
    var profileMedium: String? = null,
    var profile: String? = null
)