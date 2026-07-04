package com.example.koracards.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Coroutine-based countdown timer.
 *
 * Calls [onTick] once per second with the remaining seconds (counting down from
 * [durationSeconds] to 1), then calls [onExpiry] when the countdown reaches zero.
 *
 * The callbacks are invoked from within the launched coroutine on whatever dispatcher
 * the supplied [scope] uses. [GameViewModel] supplies `viewModelScope` (Main dispatcher),
 * so callback-driven `_state.update` calls are safe.
 *
 * Not Android-specific — uses only `kotlinx-coroutines-core`, making it fully unit-testable
 * with `runTest` and virtual time.
 */
class TurnTimerController {

    private var job: Job? = null

    /**
     * Starts (or restarts) the countdown.
     *
     * @param scope           Coroutine scope that owns the timer job.
     * @param durationSeconds Total countdown duration. Must be > 0.
     * @param onTick          Called each second with the remaining seconds (durationSeconds → 1).
     * @param onExpiry        Called once when the countdown finishes naturally (not on cancel).
     */
    fun start(
        scope: CoroutineScope,
        durationSeconds: Int,
        onTick: (remaining: Int) -> Unit,
        onExpiry: () -> Unit
    ) {
        cancel() // stop any in-progress countdown
        job = scope.launch {
            for (remaining in durationSeconds downTo 1) {
                onTick(remaining)
                delay(1_000L)
            }
            onExpiry()
        }
    }

    /**
     * Cancels the countdown without invoking [onExpiry].
     * Safe to call even if the timer is not running.
     */
    fun cancel() {
        job?.cancel()
        job = null
    }
}
