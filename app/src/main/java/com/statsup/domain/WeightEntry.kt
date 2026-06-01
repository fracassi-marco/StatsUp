package com.statsup.domain

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entry", indices = [Index(value = ["date"])])
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val weightKg: Double
)
