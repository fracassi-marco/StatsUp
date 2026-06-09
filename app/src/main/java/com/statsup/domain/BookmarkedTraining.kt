package com.statsup.domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

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
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("trainingId")
    val trainingId: String,

    @SerializedName("note")
    val note: String = "",

    @SerializedName("customTitle")
    val customTitle: String = "",

    @SerializedName("difficulty")
    val difficulty: String = "", // "easy", "medium", "hard", or empty

    @SerializedName("bookmarkedAt")
    val bookmarkedAt: Long = System.currentTimeMillis()
)

enum class Difficulty(val value: String) {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    companion object {
        fun fromString(value: String): Difficulty? {
            return entries.find { it.value == value }
        }
    }
}

