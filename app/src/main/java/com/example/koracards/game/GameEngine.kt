package com.example.koracards.game

import com.example.koracards.data.model.Player

/**
 * Pure game-logic engine — stateless singleton.
 *
 * All methods are side-effect-free transformations: they take the current [GameState],
 * apply a single game event, and return a new [GameState]. No I/O, no Android
 * dependencies. This makes the entire game logic fully unit-testable with no mocks.
 *
 * The [GameViewModel] calls these methods and then:
 * 1. Stores the returned state in `_state`.
 * 2. Sends the appropriate [com.example.koracards.network.GameMessage] to the peer.
 */
object GameEngine {

    // -----------------------------------------------------------------------
    // Round lifecycle
    // -----------------------------------------------------------------------

    /**
     * Sets up state for a new round (or a replay after "both wrong" MCQ result).
     * Preserves cumulative scores. Accepts the calling device's perspective
     * ([myCard] = this device's secret card, [opponentCard] = opponent's card to guess).
     */
    fun startRound(
        state: GameState,
        myCard: Player,
        opponentCard: Player,
        roundNumber: Int,
        firstPlayerId: String = "host"
    ): GameState = state.copy(
        phase = GamePhase.QA,
        roundNumber = roundNumber,
        isSuddenDeath = false,
        activePlayerId = firstPlayerId,
        myCard = myCard,
        opponentCard = opponentCard,
        hostFaults = 0,
        guestFaults = 0,
        questionCount = 0,
        mcqOptions = emptyList(),
        myMcqAnswer = null,
        roundWinnerId = "",
        timerSecondsLeft = state.config.turnTimerSeconds ?: 0
    )

    /**
     * Prepares state for the next round without re-dealing cards.
     * Scores are preserved; all per-round counters are reset.
     * Host calls this then immediately calls [startRound] to deal new cards.
     */
    fun resetForNextRound(state: GameState, nextRoundNumber: Int): GameState = state.copy(
        phase = GamePhase.QA,
        roundNumber = nextRoundNumber,
        isSuddenDeath = false,
        // Alternate who goes first each round (odd rounds → host, even → guest)
        activePlayerId = if (nextRoundNumber % 2 == 1) "host" else "guest",
        myCard = null,
        opponentCard = null,
        hostFaults = 0,
        guestFaults = 0,
        questionCount = 0,
        mcqOptions = emptyList(),
        myMcqAnswer = null,
        roundWinnerId = "",
        timerSecondsLeft = 0
    )

    // -----------------------------------------------------------------------
    // In-round events
    // -----------------------------------------------------------------------

    /**
     * The guesser ([GameState.activePlayerId]) guessed correctly.
     * The card-holder (opponent) calls this. The guesser wins the round.
     */
    fun applyCorrectGuess(state: GameState): GameState = state.copy(
        phase = GamePhase.CardReveal,
        roundWinnerId = state.activePlayerId,
        timerSecondsLeft = 0
    )

    /**
     * The guesser ([GameState.activePlayerId]) guessed incorrectly.
     * Increments the guesser's fault count. If [GameConfig.maxFaults] is reached
     * (and not in Sudden Death), the opponent wins the round (PRD §4.6 & §4.8).
     */
    fun applyWrongGuess(state: GameState): GameState {
        val guesser = state.activePlayerId
        val newHostFaults = if (guesser == "host") state.hostFaults + 1 else state.hostFaults
        val newGuestFaults = if (guesser == "guest") state.guestFaults + 1 else state.guestFaults

        // No fault limit during Sudden Death (PRD §4.8)
        if (state.isSuddenDeath) {
            return state.copy(hostFaults = newHostFaults, guestFaults = newGuestFaults)
        }

        val maxFaults = state.config.maxFaults
        return when {
            guesser == "host" && newHostFaults >= maxFaults -> state.copy(
                hostFaults = newHostFaults,
                phase = GamePhase.CardReveal,
                roundWinnerId = "guest",
                timerSecondsLeft = 0
            )
            guesser == "guest" && newGuestFaults >= maxFaults -> state.copy(
                guestFaults = newGuestFaults,
                phase = GamePhase.CardReveal,
                roundWinnerId = "host",
                timerSecondsLeft = 0
            )
            else -> state.copy(hostFaults = newHostFaults, guestFaults = newGuestFaults)
        }
    }

    /**
     * Advances to the next player's turn and increments the question count.
     * If [GameConfig.mcqEnabled] and [GameConfig.mcqQuestionThreshold] is reached (outside
     * Sudden Death), transitions to [GamePhase.MCQ].
     */
    fun applyNextTurn(state: GameState): GameState {
        val nextPlayer = if (state.activePlayerId == "host") "guest" else "host"
        val newQuestionCount = state.questionCount + 1

        val mcqTriggered = state.config.mcqEnabled &&
            !state.isSuddenDeath &&
            newQuestionCount >= state.config.mcqQuestionThreshold &&
            state.phase == GamePhase.QA

        return state.copy(
            activePlayerId = nextPlayer,
            questionCount = newQuestionCount,
            phase = if (mcqTriggered) GamePhase.MCQ else state.phase,
            // Reset timer for the new turn (0 if MCQ was just triggered)
            timerSecondsLeft = if (!mcqTriggered && state.config.turnTimerSeconds != null)
                state.config.turnTimerSeconds else 0
        )
    }

    /**
     * Timer expired — auto-advance turn with no fault (PRD §4.3).
     * Equivalent to [applyNextTurn] but clears the timer first.
     */
    fun applyTimerExpiry(state: GameState): GameState =
        applyNextTurn(state.copy(timerSecondsLeft = 0))

    // -----------------------------------------------------------------------
    // MCQ
    // -----------------------------------------------------------------------

    /**
     * Resolves MCQ answers per PRD §4.7:
     * - **Both correct** → [GamePhase.SuddenDeath]
     * - **One correct** → that player wins the round → [GamePhase.CardReveal]
     * - **Both wrong** → round replays with new cards → resets per-round fields (host will re-deal)
     */
    fun applyMcqResult(
        state: GameState,
        hostAnswerCorrect: Boolean,
        guestAnswerCorrect: Boolean
    ): GameState = when {
        hostAnswerCorrect && guestAnswerCorrect -> state.copy(
            phase = GamePhase.SuddenDeath,
            mcqOptions = emptyList()
        )
        hostAnswerCorrect -> state.copy(
            phase = GamePhase.CardReveal,
            roundWinnerId = "host",
            mcqOptions = emptyList()
        )
        guestAnswerCorrect -> state.copy(
            phase = GamePhase.CardReveal,
            roundWinnerId = "guest",
            mcqOptions = emptyList()
        )
        else -> state.copy(
            // Both wrong — replay round; ViewModel will call startRound() to deal new cards
            phase = GamePhase.QA,
            hostFaults = 0,
            guestFaults = 0,
            questionCount = 0,
            mcqOptions = emptyList(),
            myMcqAnswer = null,
            myCard = null,
            opponentCard = null,
            roundWinnerId = ""
        )
    }

    /**
     * Activates Sudden Death. New cards will be dealt by the ViewModel via [startRound];
     * this method just resets the round fields and sets [GameState.isSuddenDeath] = true.
     */
    fun applySuddenDeath(
        state: GameState,
        myCard: Player,
        opponentCard: Player
    ): GameState = state.copy(
        phase = GamePhase.QA,
        isSuddenDeath = true,
        myCard = myCard,
        opponentCard = opponentCard,
        hostFaults = 0,
        guestFaults = 0,
        questionCount = 0,
        mcqOptions = emptyList(),
        myMcqAnswer = null,
        activePlayerId = "host", // host always goes first after Sudden Death trigger
        timerSecondsLeft = state.config.turnTimerSeconds ?: 0
    )

    // -----------------------------------------------------------------------
    // Round / match end
    // -----------------------------------------------------------------------

    /**
     * Closes the current round: increments the winner's score and transitions to
     * [GamePhase.RoundResult] or [GamePhase.MatchOver] if the match is won.
     */
    fun applyRoundEnd(state: GameState): GameState {
        val winnerId = state.roundWinnerId
        val newHostScore = if (winnerId == "host") state.hostScore + 1 else state.hostScore
        val newGuestScore = if (winnerId == "guest") state.guestScore + 1 else state.guestScore

        val matchWinnerId = when {
            newHostScore >= state.config.roundsToWin -> "host"
            newGuestScore >= state.config.roundsToWin -> "guest"
            else -> ""
        }

        return state.copy(
            hostScore = newHostScore,
            guestScore = newGuestScore,
            phase = if (matchWinnerId.isNotEmpty()) GamePhase.MatchOver else GamePhase.RoundResult,
            matchWinnerId = matchWinnerId
        )
    }
}
