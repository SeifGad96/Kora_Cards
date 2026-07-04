package com.example.koracards.network

/**
 * Represents the current state of the peer-to-peer connection.
 *
 * Emitted by [NetworkManager.connectionState] as a [kotlinx.coroutines.flow.StateFlow].
 */
sealed class ConnectionState {

    /** A live connection to the peer device is established and healthy. */
    object Connected : ConnectionState()

    /**
     * The connection dropped unexpectedly. The client is retrying with exponential back-off.
     * @param attemptsLeft Number of retry attempts remaining before transitioning to [Lost].
     */
    data class Reconnecting(val attemptsLeft: Int) : ConnectionState()

    /**
     * All reconnection attempts exhausted. The game cannot continue.
     * The UI should navigate to the Connection Lost screen.
     */
    object Lost : ConnectionState()
}
