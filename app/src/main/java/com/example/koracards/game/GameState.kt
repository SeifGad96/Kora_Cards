package com.example.koracards.game

import com.example.koracards.data.model.Player
import com.example.koracards.network.ConnectionState
import com.example.koracards.network.GameConfig

/**
 * Complete, UI-observable game state.
 *
 * Held in a [kotlinx.coroutines.flow.StateFlow] inside [GameViewModel].
 * All fields are immutable — the ViewModel produces a new copy on every state change.
 *
 * Computed properties (`isMyTurn`, `isCardHolder`, etc.) derive from the raw fields so
 * the UI never needs to know about the internal "host" / "guest" player ID strings.
 */
data class GameState(

    // -----------------------------------------------------------------------
    // Identity — set once during game setup
    // -----------------------------------------------------------------------

    /** Role of this device in the current match. */
    val myRole: PlayerRole = PlayerRole.Host,

    /** Game rules chosen by the host. Synced to guest via [com.example.koracards.network.GameMessage.ConfigSynced]. */
    val config: GameConfig = GameConfig(),

    val hostNickname: String = "",
    val guestNickname: String = "",

    // -----------------------------------------------------------------------
    // Match progress
    // -----------------------------------------------------------------------

    val phase: GamePhase = GamePhase.Lobby,

    /** 1-based round counter. Incremented by the host before each new round. */
    val roundNumber: Int = 1,

    /**
     * True when both players answered the MCQ correctly and Sudden Death was triggered.
     * Disables the fault limit for the remainder of the round (PRD §4.8).
     */
    val isSuddenDeath: Boolean = false,

    /** Cumulative host score (rounds won). */
    val hostScore: Int = 0,

    /** Cumulative guest score (rounds won). */
    val guestScore: Int = 0,

    // -----------------------------------------------------------------------
    // Current round
    // -----------------------------------------------------------------------

    /**
     * Which player is currently asking / attempting a guess.
     * Raw value is `"host"` or `"guest"`. Use [isMyTurn] / [isCardHolder] in the UI.
     */
    val activePlayerId: String = "host",

    /**
     * This device's secret player card — displayed on screen throughout the round.
     * Null until [com.example.koracards.network.GameMessage.RoundStarted] is processed.
     */
    val myCard: Player? = null,

    /**
     * The opponent's player card — known locally but hidden from the UI until [GamePhase.CardReveal].
     * Null until [com.example.koracards.network.GameMessage.RoundStarted] is processed.
     */
    val opponentCard: Player? = null,

    /** Number of wrong guesses made by the host this round. */
    val hostFaults: Int = 0,

    /** Number of wrong guesses made by the guest this round. */
    val guestFaults: Int = 0,

    /** Total questions asked this round — used for the MCQ threshold. */
    val questionCount: Int = 0,

    // -----------------------------------------------------------------------
    // MCQ
    // -----------------------------------------------------------------------

    /**
     * This device's 3 MCQ options (1 correct + 2 decoys).
     * Populated when [phase] transitions to [GamePhase.MCQ].
     * The correct answer is the opponent's card name.
     */
    val mcqOptions: List<Player> = emptyList(),

    /** MCQ answer submitted by this device. Null until [GameViewModel.submitMcqAnswer] is called. */
    val myMcqAnswer: String? = null,

    // -----------------------------------------------------------------------
    // Results
    // -----------------------------------------------------------------------

    /**
     * Raw player ID (`"host"` / `"guest"`) of the round winner.
     * Empty string while the round is in progress.
     */
    val roundWinnerId: String = "",

    /**
     * Raw player ID of the match winner. Empty until [GamePhase.MatchOver].
     */
    val matchWinnerId: String = "",

    // -----------------------------------------------------------------------
    // Timer
    // -----------------------------------------------------------------------

    /** Seconds remaining on the turn timer. 0 when disabled or not running. */
    val timerSecondsLeft: Int = 0,

    // -----------------------------------------------------------------------
    // Network
    // -----------------------------------------------------------------------

    /** Mirrored from [com.example.koracards.network.NetworkManager.connectionState]. */
    val connectionState: ConnectionState = ConnectionState.Connected

) {
    // -----------------------------------------------------------------------
    // Computed UI helpers
    // -----------------------------------------------------------------------

    /** Internal player ID string for this device. */
    val myPlayerId: String get() = if (myRole == PlayerRole.Host) "host" else "guest"

    /** Internal player ID string for the opponent. */
    val opponentPlayerId: String get() = if (myRole == PlayerRole.Host) "guest" else "host"

    /** True when it is this device's turn to ask questions / attempt a guess. */
    val isMyTurn: Boolean get() = activePlayerId == myPlayerId

    /**
     * True when the opponent is guessing and this device is the card-holder.
     * In this state the "Correct ✓" and "Wrong ✗" buttons are shown.
     */
    val isCardHolder: Boolean get() = !isMyTurn

    /** Display name of the player on this device. */
    val myNickname: String get() = if (myRole == PlayerRole.Host) hostNickname else guestNickname

    /** Display name of the opponent player. */
    val opponentNickname: String get() = if (myRole == PlayerRole.Host) guestNickname else hostNickname

    /** Fault count for this device's player. */
    val myFaults: Int get() = if (myRole == PlayerRole.Host) hostFaults else guestFaults

    /** Fault count for the opponent. */
    val opponentFaults: Int get() = if (myRole == PlayerRole.Host) guestFaults else hostFaults

    /** Display-ready score for this device's player. */
    val myScore: Int get() = if (myRole == PlayerRole.Host) hostScore else guestScore

    /** Display-ready score for the opponent. */
    val opponentScore: Int get() = if (myRole == PlayerRole.Host) guestScore else hostScore

    /** True if this device's player won the current round. */
    val didIWinRound: Boolean get() = roundWinnerId == myPlayerId

    /** True if this device's player won the match. */
    val didIWinMatch: Boolean get() = matchWinnerId == myPlayerId
}
