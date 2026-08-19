package com.statsup.domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarked_training",
    foreignKeys = [
        ForeignKey(
            entity = Training::class,
            parentColumns = ["id"],
            childColumns = ["trainingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trainingId"], unique = true)]
)
data class BookmarkedTraining(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val trainingId: String,

    val note: String = "",

    val customTitle: String = "",

    val difficulty: String = "", // "easy", "medium", "hard", or empty

    val bookmarkedAt: Long = System.currentTimeMillis()
)

enum class Difficulty(val value: String) {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");
}

