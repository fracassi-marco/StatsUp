package com.statsup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckGoalAchievementUseCaseTest {

    private val useCase = CheckGoalAchievementUseCase()

    // -------------------------------------------------------------------------
    // No crossing → null
    // -------------------------------------------------------------------------

    @Test
    fun `returns null when both goals are below threshold`() {
        assertNull(useCase(previousDistance = 0.5f, currentDistance = 0.8f,
                           previousTraining = 0.3f, currentTraining = 0.7f))
    }

    @Test
    fun `returns null when distance was already above threshold`() {
        // Previous was already at 1f — not a crossing, just staying above
        assertNull(useCase(previousDistance = 1.0f, currentDistance = 1.2f,
                           previousTraining = 0.5f, currentTraining = 0.8f))
    }

    @Test
    fun `returns null when training was already above threshold`() {
        assertNull(useCase(previousDistance = 0.5f, currentDistance = 0.8f,
                           previousTraining = 1.0f, currentTraining = 1.5f))
    }

    @Test
    fun `returns null when neither goal changes significantly`() {
        assertNull(useCase(previousDistance = 0.9f, currentDistance = 0.99f,
                           previousTraining = 0.9f, currentTraining = 0.99f))
    }

    // -------------------------------------------------------------------------
    // DISTANCE crossing
    // -------------------------------------------------------------------------

    @Test
    fun `returns DISTANCE when only distance goal crosses 1`() {
        assertEquals(GoalAchievement.DISTANCE,
            useCase(previousDistance = 0.9f, currentDistance = 1.0f,
                    previousTraining = 0.5f, currentTraining = 0.7f))
    }

    @Test
    fun `returns DISTANCE when distance crosses well above 1`() {
        assertEquals(GoalAchievement.DISTANCE,
            useCase(previousDistance = 0.1f, currentDistance = 1.5f,
                    previousTraining = 0.5f, currentTraining = 0.9f))
    }

    @Test
    fun `returns null when distance is exactly 0_999 (just below threshold)`() {
        assertNull(useCase(previousDistance = 0.9f, currentDistance = 0.999f,
                           previousTraining = 0.5f, currentTraining = 0.7f))
    }

    // -------------------------------------------------------------------------
    // TRAINING_COUNT crossing
    // -------------------------------------------------------------------------

    @Test
    fun `returns TRAINING_COUNT when only training goal crosses 1`() {
        assertEquals(GoalAchievement.TRAINING_COUNT,
            useCase(previousDistance = 0.5f, currentDistance = 0.7f,
                    previousTraining = 0.9f, currentTraining = 1.0f))
    }

    @Test
    fun `returns TRAINING_COUNT when training crosses well above 1`() {
        assertEquals(GoalAchievement.TRAINING_COUNT,
            useCase(previousDistance = 0.5f, currentDistance = 0.8f,
                    previousTraining = 0.0f, currentTraining = 2.0f))
    }

    // -------------------------------------------------------------------------
    // BOTH crossing
    // -------------------------------------------------------------------------

    @Test
    fun `returns BOTH when both goals cross 1 simultaneously`() {
        assertEquals(GoalAchievement.BOTH,
            useCase(previousDistance = 0.9f, currentDistance = 1.0f,
                    previousTraining = 0.9f, currentTraining = 1.0f))
    }

    @Test
    fun `returns BOTH when both cross from low values`() {
        assertEquals(GoalAchievement.BOTH,
            useCase(previousDistance = 0.0f, currentDistance = 1.1f,
                    previousTraining = 0.0f, currentTraining = 1.1f))
    }

    // -------------------------------------------------------------------------
    // Edge cases at exact threshold
    // -------------------------------------------------------------------------

    @Test
    fun `crossing is inclusive — exactly 1_0f counts as crossed`() {
        assertEquals(GoalAchievement.DISTANCE,
            useCase(previousDistance = 0.99f, currentDistance = 1.0f,
                    previousTraining = 0.5f,  currentTraining = 0.5f))
    }

    @Test
    fun `previous exactly at 1_0f is NOT a crossing`() {
        // previousDistance = 1.0f → already reached, no new achievement
        assertNull(useCase(previousDistance = 1.0f, currentDistance = 1.0f,
                           previousTraining = 0.5f, currentTraining = 0.5f))
    }
}
