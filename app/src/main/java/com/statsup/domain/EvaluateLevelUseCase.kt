package com.statsup.domain

import androidx.annotation.StringRes
import com.statsup.R
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

class EvaluateLevelUseCase {

    private data class LevelDef(val number: Int, @StringRes val nameResId: Int, val emoji: String, val threshold: Int)

    private val thresholds = listOf(
        LevelDef(1,  R.string.level_name_1,  "🌱", 0),
        LevelDef(2,  R.string.level_name_2,  "🏃", 200),
        LevelDef(3,  R.string.level_name_3,  "💪", 500),
        LevelDef(4,  R.string.level_name_4,  "⚡", 1_000),
        LevelDef(5,  R.string.level_name_5,  "🔥", 2_000),
        LevelDef(6,  R.string.level_name_6,  "🌟", 4_000),
        LevelDef(7,  R.string.level_name_7,  "🏅", 7_000),
        LevelDef(8,  R.string.level_name_8,  "🥇", 11_000),
        LevelDef(9,  R.string.level_name_9,  "🏆", 16_000),
        LevelDef(10, R.string.level_name_10, "👑", 25_000),
        LevelDef(11, R.string.level_name_11, "🔱", 36_000),
        LevelDef(12, R.string.level_name_12, "⭐", 52_000),
        LevelDef(13, R.string.level_name_13, "🦁", 73_000),
        LevelDef(14, R.string.level_name_14, "🌩️", 100_000),
        LevelDef(15, R.string.level_name_15, "🛡️", 135_000),
        LevelDef(16, R.string.level_name_16, "🚀", 180_000),
        LevelDef(17, R.string.level_name_17, "🦅", 240_000),
        LevelDef(18, R.string.level_name_18, "✨", 320_000),
        LevelDef(19, R.string.level_name_19, "🌌", 420_000),
        LevelDef(20, R.string.level_name_20, "♾️", 550_000)
    )

    operator fun invoke(trainings: List<Training>, now: LocalDate = LocalDate.now()): Level {
        val earnedXp = trainings.sumOf { it.xp() }

        val lastActivityDate = trainings.maxOfOrNull { it.date.toLocalDate() }
        val daysSinceLast = if (lastActivityDate != null)
            ChronoUnit.DAYS.between(lastActivityDate, now).toInt().coerceAtLeast(0)
        else
            0

        val totalXp = max(0, earnedXp - decayXp(daysSinceLast))

        val currentDef = thresholds.lastOrNull { totalXp >= it.threshold } ?: thresholds.first()
        val nextDef = thresholds.getOrNull(currentDef.number)

        val currentLevelXp = totalXp - currentDef.threshold
        val nextLevelXp = nextDef?.threshold?.minus(currentDef.threshold) ?: 0

        val isDecaying = daysSinceLast > 3
        val dailyDecayRate = when {
            daysSinceLast <= 3  -> 0
            daysSinceLast <= 30 -> 3
            else                -> 5
        }

        return Level(
            number = currentDef.number,
            nameResId = currentDef.nameResId,
            emoji = currentDef.emoji,
            totalXp = totalXp,
            currentLevelXp = currentLevelXp,
            nextLevelXp = nextLevelXp,
            isDecaying = isDecaying,
            dailyDecayRate = dailyDecayRate,
            daysSinceLastActivity = daysSinceLast
        )
    }

    private fun decayXp(daysSinceLast: Int): Int = when {
        daysSinceLast <= 3  -> 0
        daysSinceLast <= 30 -> (daysSinceLast - 3) * 3
        else                -> (27 * 3) + (daysSinceLast - 30) * 5
    }

    private fun Training.xp(): Int =
        (distanceInKilometers() + totalElevationGain / 100.0 + movingTime / 120.0).toInt()
}
