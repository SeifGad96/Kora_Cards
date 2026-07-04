package com.example.koracards.game

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [TurnTimerController].
 *
 * Uses `kotlinx-coroutines-test` virtual time so tests run instantly
 * without real-clock delays.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TurnTimerControllerTest {

    @Test
    fun `timer emits ticks from durationSeconds down to 1 then calls onExpiry`() = runTest {
        val controller = TurnTimerController()
        val ticks = mutableListOf<Int>()
        var expired = false

        controller.start(
            scope = this,
            durationSeconds = 3,
            onTick = { remaining -> ticks += remaining },
            onExpiry = { expired = true }
        )

        advanceUntilIdle()

        assertEquals("Expected ticks 3, 2, 1", listOf(3, 2, 1), ticks)
        assertTrue("Expected onExpiry to be called", expired)
    }

    @Test
    fun `cancel stops countdown before expiry`() = runTest {
        val controller = TurnTimerController()
        val ticks = mutableListOf<Int>()
        var expired = false

        controller.start(
            scope = this,
            durationSeconds = 10,
            onTick = { remaining -> ticks += remaining },
            onExpiry = { expired = true }
        )

        // Advance 3 seconds — should see ticks for 10, 9, 8
        advanceTimeBy(3_001L)
        controller.cancel()
        advanceUntilIdle()

        assertFalse("onExpiry must NOT be called after cancel()", expired)
        // Should have received at most 3 ticks (10, 9, 8)
        assertTrue("Should have received some ticks before cancel", ticks.isNotEmpty())
        assertFalse("Tick list should not contain 1 (timer was cancelled early)", 1 in ticks)
    }

    @Test
    fun `starting a second timer cancels the first`() = runTest {
        val controller = TurnTimerController()
        var firstExpired = false
        var secondExpired = false

        controller.start(
            scope = this,
            durationSeconds = 10,
            onTick = {},
            onExpiry = { firstExpired = true }
        )

        // Advance partway, then start a new shorter timer
        advanceTimeBy(2_000L)
        controller.start(
            scope = this,
            durationSeconds = 2,
            onTick = {},
            onExpiry = { secondExpired = true }
        )

        advanceUntilIdle()

        assertFalse("First timer should be cancelled when second starts", firstExpired)
        assertTrue("Second timer should complete normally", secondExpired)
    }
}
