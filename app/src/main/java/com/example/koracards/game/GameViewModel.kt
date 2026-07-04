package com.example.koracards.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koracards.data.repository.PlayerRepository
import com.example.koracards.network.GameConfig
import com.example.koracards.network.GameMessage
import com.example.koracards.network.NetworkManager
import com.example.koracards.network.Scores
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Central game orchestrator for both host and guest devices.
 *
 * Observes [NetworkManager.incomingMessages] and [NetworkManager.connectionState],
 * applies game events via [GameEngine], and exposes the result as [state].
 *
 * **Host** drives all game logic: starts rounds, advances turns, resolves MCQ, declares winners.
 * **Guest** receives state updates and can only trigger actions that concern their own device
 * (tapping Correct/Wrong as card-holder, submitting MCQ answers).
 *
 * ──────────────────────────────────────────────────────────────
 * Setup sequence (host)
 *   1. `setRole(PlayerRole.Host)`
 *   2. `applyNicknameSync(hostName, guestName)` after names are confirmed
 *   3. `applyConfig(config)` after game config screen
 *   4. `startRound()` to begin round 1
 *
 * Setup sequence (guest)
 *   1. `setRole(PlayerRole.Guest)`
 *   2. Wait — NicknameSync + ConfigSynced + RoundStarted arrive automatically
 * ──────────────────────────────────────────────────────────────
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val networkManager: NetworkManager,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    /** Live game state — collect in Compose with `collectAsStateWithLifecycle()`. */
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val engine = GameEngine
    private val timer = TurnTimerController()

    /** Player IDs used across all rounds in this match — prevents card repeats (PRD §7.1). */
    private val usedPlayerIds = mutableSetOf<String>()

    /**
     * (Host only) Stores the guest's MCQ answer while waiting for the host's own submission,
     * or vice-versa. Cleared after MCQ resolution.
     */
    private var pendingGuestMcqAnswer: String? = null

    init {
        viewModelScope.launch {
            networkManager.incomingMessages.collect { message ->
                handleIncomingMessage(message)
            }
        }
        viewModelScope.launch {
            networkManager.connectionState.collect { connState ->
                _state.update { it.copy(connectionState = connState) }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Setup — called from UI before the match starts
    // -----------------------------------------------------------------------

    /** Must be called once at game-setup time to tell the ViewModel which role this device plays. */
    fun setRole(role: PlayerRole) {
        _state.update { it.copy(myRole = role) }
    }

    /** Applies synced nicknames to the local state. Host calls this after both names are collected. */
    fun applyNicknameSync(hostName: String, guestName: String) {
        _state.update { it.copy(hostNickname = hostName, guestNickname = guestName) }
    }

    /** Applies the agreed game config to the local state. Host calls this after the Config screen. */
    fun applyConfig(config: GameConfig) {
        _state.update { it.copy(config = config) }
    }

    // -----------------------------------------------------------------------
    // Host — round management (public)
    // -----------------------------------------------------------------------

    /**
     * Host picks two unique random cards and starts the round.
     * Sends [GameMessage.RoundStarted] to the guest. Safe to call for replays and Sudden Death re-deals.
     *
     * Must only be called from the host device.
     */
    fun startRound() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _state.value
            val cards = playerRepository.getRandomPlayers(2, usedPlayerIds)
            val hostCard = cards[0]   // host's own secret card
            val guestCard = cards[1]  // guest's own secret card

            // Track used IDs for the whole match session
            usedPlayerIds += setOf(hostCard.id, guestCard.id)

            val firstPlayer = if (currentState.roundNumber % 2 == 1) "host" else "guest"

            _state.update { s ->
                engine.startRound(
                    state = s,
                    myCard = hostCard,        // host's perspective: myCard = hostCard
                    opponentCard = guestCard, // host guesses guest's card
                    roundNumber = s.roundNumber,
                    firstPlayerId = firstPlayer
                )
            }

            networkManager.send(GameMessage.RoundStarted(hostCard = hostCard, guestCard = guestCard))
            startTimerIfEnabled()
        }
    }

    /**
     * Host increments the round counter and starts a new round.
     * Called from the Round Result screen's "Next Round" button.
     */
    fun startNextRound() {
        _state.update { engine.resetForNextRound(it, it.roundNumber + 1) }
        pendingGuestMcqAnswer = null
        startRound()
    }

    // -----------------------------------------------------------------------
    // Both sides — in-game actions (public)
    // -----------------------------------------------------------------------

    /**
     * The card-holder confirms the opponent's guess was correct.
     *
     * Called by whichever device is currently acting as card-holder ([GameState.isCardHolder]).
     * Sends [GameMessage.GuessResult] to the peer; host also drives game advancement.
     */
    fun handleCorrectGuess() {
        val currentState = _state.value
        val newState = engine.applyCorrectGuess(currentState)
        _state.value = newState
        timer.cancel()

        viewModelScope.launch {
            networkManager.send(GameMessage.GuessResult(isCorrect = true, faultCount = 0))
            if (currentState.myRole == PlayerRole.Host) {
                advanceAfterRoundEnd(newState)
            }
        }
    }

    /**
     * The card-holder marks the opponent's guess as wrong.
     *
     * Increments the guesser's fault. If the fault limit is reached, the host also ends the round.
     * Sends [GameMessage.GuessResult] to the peer.
     */
    fun handleWrongGuess() {
        val currentState = _state.value
        val newState = engine.applyWrongGuess(currentState)
        _state.value = newState

        val guesser = currentState.activePlayerId
        val updatedFaults = if (guesser == "host") newState.hostFaults else newState.guestFaults

        viewModelScope.launch {
            networkManager.send(GameMessage.GuessResult(isCorrect = false, faultCount = updatedFaults))

            if (currentState.myRole == PlayerRole.Host) {
                when (newState.phase) {
                    GamePhase.CardReveal -> advanceAfterRoundEnd(newState) // fault limit reached
                    else -> advanceTurn()                                   // just a fault, continue
                }
            }
        }
    }

    /**
     * Submits this device's MCQ answer.
     *
     * - **Host** resolves MCQ internally as soon as both answers are available.
     * - **Guest** sends [GameMessage.McqAnswerSubmitted] to the host and waits for the result.
     */
    fun submitMcqAnswer(answer: String) {
        _state.update { it.copy(myMcqAnswer = answer) }
        viewModelScope.launch {
            if (_state.value.myRole == PlayerRole.Host) {
                resolveMcqIfReady(myAnswer = answer)
            } else {
                networkManager.send(
                    GameMessage.McqAnswerSubmitted(playerId = "guest", answer = answer)
                )
            }
        }
    }

    // -----------------------------------------------------------------------
    // Match lifecycle (public)
    // -----------------------------------------------------------------------

    /**
     * Resets all match state while preserving identity fields (role, config, nicknames, connection).
     * Both players should navigate back to the lobby/setup screen after calling this.
     */
    fun resetMatch() {
        timer.cancel()
        usedPlayerIds.clear()
        pendingGuestMcqAnswer = null
        val current = _state.value
        _state.value = GameState(
            myRole = current.myRole,
            config = current.config,
            hostNickname = current.hostNickname,
            guestNickname = current.guestNickname,
            connectionState = current.connectionState
        )
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    // -----------------------------------------------------------------------
    // Host — private game advancement
    // -----------------------------------------------------------------------

    private suspend fun advanceTurn() {
        val newState = engine.applyNextTurn(_state.value)
        _state.value = newState

        networkManager.send(GameMessage.TurnChanged(newState.activePlayerId))

        if (newState.phase == GamePhase.MCQ) {
            triggerMcq()
        } else {
            startTimerIfEnabled()
        }
    }

    private suspend fun triggerMcq() {
        val s = _state.value
        val hostCard = s.myCard ?: return      // host's own card (guest guesses this)
        val guestCard = s.opponentCard ?: return // guest's card (host guesses this)

        // Host guesses guest's card → hostOptions correct answer is guestCard
        val hostDecoys = withContext(Dispatchers.IO) {
            playerRepository.getMcqDecoys(correctPlayerId = guestCard.id, usedIds = usedPlayerIds)
        }
        val hostOptions = (listOf(guestCard) + hostDecoys).shuffled()

        // Guest guesses host's card → guestOptions correct answer is hostCard
        val guestDecoys = withContext(Dispatchers.IO) {
            playerRepository.getMcqDecoys(correctPlayerId = hostCard.id, usedIds = usedPlayerIds)
        }
        val guestOptions = (listOf(hostCard) + guestDecoys).shuffled()

        // Host stores its own MCQ options locally
        _state.update { it.copy(mcqOptions = hostOptions, phase = GamePhase.MCQ) }

        networkManager.send(
            GameMessage.McqTriggered(hostOptions = hostOptions, guestOptions = guestOptions)
        )
    }

    /**
     * Resolves MCQ as soon as both the host's and guest's answers are available.
     * [myAnswer] is the host's answer, [pendingGuestMcqAnswer] is the guest's answer.
     *
     * Returns without doing anything if one answer is still missing.
     */
    private fun resolveMcqIfReady(myAnswer: String) {
        val guestAnswer = pendingGuestMcqAnswer ?: return  // still waiting

        val s = _state.value
        val guestCard = s.opponentCard ?: return  // guest's card (what host must guess)
        val hostCard = s.myCard ?: return         // host's card (what guest must guess)

        val hostCorrect = myAnswer == guestCard.name
        val guestCorrect = guestAnswer == hostCard.name

        val newState = engine.applyMcqResult(s, hostCorrect, guestCorrect)
        _state.value = newState
        pendingGuestMcqAnswer = null

        viewModelScope.launch {
            when (newState.phase) {
                GamePhase.SuddenDeath -> {
                    networkManager.send(GameMessage.SuddenDeathTriggered)
                    launchSuddenDeath()
                }
                GamePhase.CardReveal -> advanceAfterRoundEnd(newState)
                GamePhase.QA -> {
                    // Both wrong — replay round with fresh cards
                    startRound()
                }
                else -> {}
            }
        }
    }

    private suspend fun launchSuddenDeath() {
        val cards = withContext(Dispatchers.IO) {
            playerRepository.getRandomPlayers(2, usedPlayerIds)
        }
        val hostCard = cards[0]
        val guestCard = cards[1]
        usedPlayerIds += setOf(hostCard.id, guestCard.id)

        _state.update { s ->
            engine.applySuddenDeath(s, myCard = hostCard, opponentCard = guestCard)
        }

        networkManager.send(GameMessage.RoundStarted(hostCard = hostCard, guestCard = guestCard))
        startTimerIfEnabled()
    }

    /**
     * Sends [GameMessage.CardRevealTrigger], then updates scores and sends [GameMessage.RoundEnded].
     * If the match is over, also sends [GameMessage.MatchWon].
     */
    private suspend fun advanceAfterRoundEnd(stateAtRoundEnd: GameState) {
        networkManager.send(GameMessage.CardRevealTrigger)

        val newState = engine.applyRoundEnd(stateAtRoundEnd)
        _state.value = newState

        networkManager.send(
            GameMessage.RoundEnded(
                winnerId = stateAtRoundEnd.roundWinnerId,
                scores = Scores(newState.hostScore, newState.guestScore)
            )
        )

        if (newState.phase == GamePhase.MatchOver) {
            networkManager.send(GameMessage.MatchWon(newState.matchWinnerId))
        }
    }

    // -----------------------------------------------------------------------
    // Incoming message routing (both sides)
    // -----------------------------------------------------------------------

    private fun handleIncomingMessage(message: GameMessage) {
        when (message) {
            is GameMessage.RoundStarted       -> handleRoundStarted(message)
            is GameMessage.TurnChanged        -> handleTurnChanged(message)
            is GameMessage.GuessResult        -> handleGuessResult(message)
            is GameMessage.McqTriggered       -> handleMcqTriggered(message)
            is GameMessage.McqAnswerSubmitted -> handleMcqAnswerSubmitted(message)
            is GameMessage.SuddenDeathTriggered -> {
                // Guest side: mark Sudden Death; new cards arrive in the next RoundStarted
                _state.update { it.copy(phase = GamePhase.SuddenDeath) }
            }
            is GameMessage.RoundEnded         -> handleRoundEnded(message)
            is GameMessage.CardRevealTrigger  -> {
                _state.update { it.copy(phase = GamePhase.CardReveal) }
            }
            is GameMessage.MatchWon           -> {
                _state.update { it.copy(matchWinnerId = message.winnerId, phase = GamePhase.MatchOver) }
            }
            is GameMessage.NicknameSync       -> applyNicknameSync(message.hostName, message.guestName)
            is GameMessage.ConfigSynced       -> applyConfig(message.config)
        }
    }

    private fun handleRoundStarted(message: GameMessage.RoundStarted) {
        val role = _state.value.myRole
        // Determine card perspective based on role
        val myCard = if (role == PlayerRole.Host) message.hostCard else message.guestCard
        val opponentCard = if (role == PlayerRole.Host) message.guestCard else message.hostCard
        // Preserve isSuddenDeath if it was already set (Sudden Death delivers a new RoundStarted)
        val wasSuddenDeath = _state.value.phase == GamePhase.SuddenDeath || _state.value.isSuddenDeath

        _state.update { s ->
            s.copy(
                phase = GamePhase.QA,
                isSuddenDeath = wasSuddenDeath,
                myCard = myCard,
                opponentCard = opponentCard,
                hostFaults = 0,
                guestFaults = 0,
                questionCount = 0,
                mcqOptions = emptyList(),
                myMcqAnswer = null,
                roundWinnerId = ""
            )
        }
        startTimerIfEnabled()
    }

    private fun handleTurnChanged(message: GameMessage.TurnChanged) {
        _state.update { it.copy(activePlayerId = message.activePlayerId) }
        startTimerIfEnabled()
    }

    private fun handleGuessResult(message: GameMessage.GuessResult) {
        val currentState = _state.value
        val newState = if (message.isCorrect) {
            engine.applyCorrectGuess(currentState)
        } else {
            engine.applyWrongGuess(currentState)
        }
        _state.value = newState
        timer.cancel()

        // Host drives advancement when it receives GuessResult from the guest
        // (i.e., host was guessing and guest was card-holder)
        if (currentState.myRole == PlayerRole.Host) {
            viewModelScope.launch {
                when {
                    newState.phase == GamePhase.CardReveal -> advanceAfterRoundEnd(newState)
                    !message.isCorrect -> advanceTurn() // fault recorded, continue
                }
            }
        }
    }

    private fun handleMcqTriggered(message: GameMessage.McqTriggered) {
        val role = _state.value.myRole
        val myOptions = if (role == PlayerRole.Host) message.hostOptions else message.guestOptions
        _state.update { it.copy(phase = GamePhase.MCQ, mcqOptions = myOptions) }
    }

    private fun handleMcqAnswerSubmitted(message: GameMessage.McqAnswerSubmitted) {
        // Only the host resolves MCQ — ignore on guest side
        if (_state.value.myRole != PlayerRole.Host) return
        pendingGuestMcqAnswer = message.answer
        val myAnswer = _state.value.myMcqAnswer
        if (myAnswer != null) resolveMcqIfReady(myAnswer)
    }

    private fun handleRoundEnded(message: GameMessage.RoundEnded) {
        val config = _state.value.config
        val isMatchOver = message.scores.hostScore >= config.roundsToWin ||
            message.scores.guestScore >= config.roundsToWin
        val matchWinnerId = when {
            message.scores.hostScore >= config.roundsToWin -> "host"
            message.scores.guestScore >= config.roundsToWin -> "guest"
            else -> ""
        }
        _state.update { s ->
            s.copy(
                roundWinnerId = message.winnerId,
                hostScore = message.scores.hostScore,
                guestScore = message.scores.guestScore,
                phase = if (isMatchOver) GamePhase.MatchOver else GamePhase.RoundResult,
                matchWinnerId = matchWinnerId
            )
        }
    }

    // -----------------------------------------------------------------------
    // Timer
    // -----------------------------------------------------------------------

    private fun startTimerIfEnabled() {
        val seconds = _state.value.config.turnTimerSeconds ?: return  // timer disabled
        timer.start(
            scope = viewModelScope,
            durationSeconds = seconds,
            onTick = { remaining -> _state.update { it.copy(timerSecondsLeft = remaining) } },
            onExpiry = {
                _state.update { it.copy(timerSecondsLeft = 0) }
                // Host advances turn; guest waits for TurnChanged from host
                if (_state.value.myRole == PlayerRole.Host) {
                    _state.update { engine.applyTimerExpiry(it) }
                    viewModelScope.launch { advanceTurn() }
                }
            }
        )
    }
}
