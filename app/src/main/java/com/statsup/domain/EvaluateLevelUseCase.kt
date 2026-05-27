package com.statsup.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

class EvaluateLevelUseCase {

    private data class LevelDef(val number: Int, val name: String, val emoji: String, val threshold: Int)

    private val thresholds = listOf(
        LevelDef(1,  "Principiante", "🌱", 0),
        LevelDef(2,  "Allenato",     "🏃", 200),
        LevelDef(3,  "Resistente",   "💪", 500),
        LevelDef(4,  "Atleta",       "⚡", 1_000),
        LevelDef(5,  "Esperto",      "🔥", 2_000),
        LevelDef(6,  "Elite",        "🌟", 4_000),
        LevelDef(7,  "Campione",     "🏅", 7_000),
        LevelDef(8,  "Maestro",      "🥇", 11_000),
        LevelDef(9,  "Leggenda",     "🏆", 16_000),
        LevelDef(10, "Immortale",    "👑", 25_000)
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
            name = currentDef.name,
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
