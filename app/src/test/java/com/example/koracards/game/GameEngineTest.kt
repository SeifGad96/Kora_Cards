package com.example.koracards.game

import com.example.koracards.data.model.Player
import com.example.koracards.network.GameConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [GameEngine] — pure state-machine logic.
 *
 * No mocks required. All tests instantiate a [GameState] directly,
 * pass it through an engine method, and assert the returned state.
 */
class GameEngineTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun defaultConfig(
        roundsToWin: Int = 3,
        maxFaults: Int = 3,
        turnTimerSeconds: Int? = null,
        mcqEnabled: Boolean = true,
        mcqQuestionThreshold: Int = 5
    ) = GameConfig(roundsToWin, maxFaults, turnTimerSeconds, mcqEnabled, mcqQuestionThreshold)

    private fun baseState(
        config: GameConfig = defaultConfig(),
        phase: GamePhase = GamePhase.QA,
        activePlayerId: String = "host",
        hostFaults: Int = 0,
        guestFaults: Int = 0,
        questionCount: Int = 0,
        hostScore: Int = 0,
        guestScore: Int = 0,
        roundWinnerId: String = "",
        isSuddenDeath: Boolean = false,
        myCard: Player? = dummyPlayer("hCard"),
        opponentCard: Player? = dummyPlayer("gCard"),
        timerSecondsLeft: Int = 0
    ) = GameState(
        myRole = PlayerRole.Host,
        config = config,
        phase = phase,
        activePlayerId = activePlayerId,
        hostFaults = hostFaults,
        guestFaults = guestFaults,
        questionCount = questionCount,
        hostScore = hostScore,
        guestScore = guestScore,
        roundWinnerId = roundWinnerId,
        isSuddenDeath = isSuddenDeath,
        myCard = myCard,
        opponentCard = opponentCard,
        timerSecondsLeft = timerSecondsLeft
    )

    private fun dummyPlayer(id: String, name: String = "Player $id") = Player(
        id = id, name = name, photoUrl = "",
        age = 25, shirtNumber = 10,
        teamEn = "Team", teamAr = "فريق",
        nationalityEn = "English", nationalityAr = "إنجليزي",
        positionEn = "Forward", positionAr = "مهاجم",
        preferredFootEn = "Right", preferredFootAr = "يمين",
        leagueEn = "PL", leagueAr = "الدوري الإنجليزي"
    )

    // -----------------------------------------------------------------------
    // 1. Correct guess
    // -----------------------------------------------------------------------

    @Test
    fun `applyCorrectGuess sets roundWinnerId to activePlayerId`() {
        val state = baseState(activePlayerId = "guest")
        val result = GameEngine.applyCorrectGuess(state)
        assertEquals("guest", result.roundWinnerId)
    }

    @Test
    fun `applyCorrectGuess transitions to CardReveal phase`() {
        val result = GameEngine.applyCorrectGuess(baseState(activePlayerId = "host"))
        assertEquals(GamePhase.CardReveal, result.phase)
    }

    @Test
    fun `applyCorrectGuess clears timer`() {
        val state = baseState().copy(timerSecondsLeft = 20)
        val result = GameEngine.applyCorrectGuess(state)
        assertEquals(0, result.timerSecondsLeft)
    }

    // -----------------------------------------------------------------------
    // 2. Wrong guess — fault accumulation
    // -----------------------------------------------------------------------

    @Test
    fun `applyWrongGuess increments host fault when host is guessing`() {
        val state = baseState(activePlayerId = "host", hostFaults = 0)
        val result = GameEngine.applyWrongGuess(state)
        assertEquals(1, result.hostFaults)
        assertEquals(0, result.guestFaults)
        assertEquals(GamePhase.QA, result.phase)
    }

    @Test
    fun `applyWrongGuess increments guest fault when guest is guessing`() {
        val state = baseState(activePlayerId = "guest", guestFaults = 1)
        val result = GameEngine.applyWrongGuess(state)
        assertEquals(2, result.guestFaults)
        assertEquals(0, result.hostFaults)
        assertEquals(GamePhase.QA, result.phase)
    }

    @Test
    fun `applyWrongGuess at maxFaults ends round - host loses`() {
        val config = defaultConfig(maxFaults = 3)
        val state = baseState(config = config, activePlayerId = "host", hostFaults = 2)
        val result = GameEngine.applyWrongGuess(state)
        assertEquals(3, result.hostFaults)
        assertEquals("guest", result.roundWinnerId)
        assertEquals(GamePhase.CardReveal, result.phase)
    }

    @Test
    fun `applyWrongGuess at maxFaults ends round - guest loses`() {
        val config = defaultConfig(maxFaults = 2)
        val state = baseState(config = config, activePlayerId = "guest", guestFaults = 1)
        val result = GameEngine.applyWrongGuess(state)
        assertEquals(2, result.guestFaults)
        assertEquals("host", result.roundWinnerId)
        assertEquals(GamePhase.CardReveal, result.phase)
    }

    @Test
    fun `applyWrongGuess in SuddenDeath does NOT end round even past maxFaults`() {
        val config = defaultConfig(maxFaults = 1)
        val state = baseState(
            config = config,
            activePlayerId = "host",
            hostFaults = 1,        // already AT max
            isSuddenDeath = true
        )
        val result = GameEngine.applyWrongGuess(state)
        assertEquals(2, result.hostFaults)
        assertEquals(GamePhase.QA, result.phase)   // NOT CardReveal
        assertEquals("", result.roundWinnerId)
    }

    // -----------------------------------------------------------------------
    // 3. Turn advancement
    // -----------------------------------------------------------------------

    @Test
    fun `applyNextTurn flips active player from host to guest`() {
        val result = GameEngine.applyNextTurn(baseState(activePlayerId = "host"))
        assertEquals("guest", result.activePlayerId)
    }

    @Test
    fun `applyNextTurn flips active player from guest to host`() {
        val result = GameEngine.applyNextTurn(baseState(activePlayerId = "guest"))
        assertEquals("host", result.activePlayerId)
    }

    @Test
    fun `applyNextTurn increments questionCount`() {
        val state = baseState(questionCount = 3)
        val result = GameEngine.applyNextTurn(state)
        assertEquals(4, result.questionCount)
    }

    @Test
    fun `applyNextTurn triggers MCQ when threshold is reached`() {
        val config = defaultConfig(mcqEnabled = true, mcqQuestionThreshold = 3)
        // questionCount goes 2 → 3 on this turn → MCQ triggers
        val state = baseState(config = config, questionCount = 2, phase = GamePhase.QA)
        val result = GameEngine.applyNextTurn(state)
        assertEquals(GamePhase.MCQ, result.phase)
    }

    @Test
    fun `applyNextTurn does NOT trigger MCQ in SuddenDeath`() {
        val config = defaultConfig(mcqEnabled = true, mcqQuestionThreshold = 1)
        val state = baseState(config = config, questionCount = 1, isSuddenDeath = true, phase = GamePhase.QA)
        val result = GameEngine.applyNextTurn(state)
        assertEquals(GamePhase.QA, result.phase)
    }

    @Test
    fun `applyNextTurn does NOT trigger MCQ when mcqEnabled is false`() {
        val config = defaultConfig(mcqEnabled = false, mcqQuestionThreshold = 1)
        val state = baseState(config = config, questionCount = 5, phase = GamePhase.QA)
        val result = GameEngine.applyNextTurn(state)
        assertEquals(GamePhase.QA, result.phase)
    }

    // -----------------------------------------------------------------------
    // 4. MCQ resolution (PRD §4.7)
    // -----------------------------------------------------------------------

    @Test
    fun `applyMcqResult both correct triggers SuddenDeath`() {
        val state = baseState(phase = GamePhase.MCQ)
        val result = GameEngine.applyMcqResult(state, hostAnswerCorrect = true, guestAnswerCorrect = true)
        assertEquals(GamePhase.SuddenDeath, result.phase)
        assertEquals("", result.roundWinnerId)
    }

    @Test
    fun `applyMcqResult host correct guest wrong - host wins`() {
        val state = baseState(phase = GamePhase.MCQ)
        val result = GameEngine.applyMcqResult(state, hostAnswerCorrect = true, guestAnswerCorrect = false)
        assertEquals(GamePhase.CardReveal, result.phase)
        assertEquals("host", result.roundWinnerId)
    }

    @Test
    fun `applyMcqResult guest correct host wrong - guest wins`() {
        val state = baseState(phase = GamePhase.MCQ)
        val result = GameEngine.applyMcqResult(state, hostAnswerCorrect = false, guestAnswerCorrect = true)
        assertEquals(GamePhase.CardReveal, result.phase)
        assertEquals("guest", result.roundWinnerId)
    }

    @Test
    fun `applyMcqResult both wrong - round replays with cleared cards`() {
        val state = baseState(phase = GamePhase.MCQ)
        val result = GameEngine.applyMcqResult(state, hostAnswerCorrect = false, guestAnswerCorrect = false)
        assertEquals(GamePhase.QA, result.phase)
        assertEquals("", result.roundWinnerId)
        assertNull("myCard should be cleared for re-deal", result.myCard)
        assertNull("opponentCard should be cleared for re-deal", result.opponentCard)
        assertEquals(0, result.hostFaults)
        assertEquals(0, result.guestFaults)
    }

    // -----------------------------------------------------------------------
    // 5. Round / match end — score accumulation
    // -----------------------------------------------------------------------

    @Test
    fun `applyRoundEnd increments host score when host wins`() {
        val state = baseState(roundWinnerId = "host", hostScore = 1)
        val result = GameEngine.applyRoundEnd(state)
        assertEquals(2, result.hostScore)
        assertEquals(0, result.guestScore)
    }

    @Test
    fun `applyRoundEnd increments guest score when guest wins`() {
        val state = baseState(roundWinnerId = "guest", guestScore = 0)
        val result = GameEngine.applyRoundEnd(state)
        assertEquals(1, result.guestScore)
        assertEquals(0, result.hostScore)
    }

    @Test
    fun `applyRoundEnd transitions to RoundResult when match not over`() {
        val config = defaultConfig(roundsToWin = 3)
        val state = baseState(config = config, roundWinnerId = "host", hostScore = 1)
        val result = GameEngine.applyRoundEnd(state)
        assertEquals(GamePhase.RoundResult, result.phase)
        assertEquals("", result.matchWinnerId)
    }

    @Test
    fun `applyRoundEnd transitions to MatchOver when host reaches roundsToWin`() {
        val config = defaultConfig(roundsToWin = 3)
        val state = baseState(config = config, roundWinnerId = "host", hostScore = 2)
        val result = GameEngine.applyRoundEnd(state)
        assertEquals(3, result.hostScore)
        assertEquals(GamePhase.MatchOver, result.phase)
        assertEquals("host", result.matchWinnerId)
    }

    @Test
    fun `applyRoundEnd transitions to MatchOver when guest reaches roundsToWin`() {
        val config = defaultConfig(roundsToWin = 5)
        val state = baseState(config = config, roundWinnerId = "guest", guestScore = 4)
        val result = GameEngine.applyRoundEnd(state)
        assertEquals(5, result.guestScore)
        assertEquals(GamePhase.MatchOver, result.phase)
        assertEquals("guest", result.matchWinnerId)
    }

    // -----------------------------------------------------------------------
    // 6. resetForNextRound
    // -----------------------------------------------------------------------

    @Test
    fun `resetForNextRound preserves cumulative scores`() {
        val state = baseState(hostScore = 2, guestScore = 1)
        val result = GameEngine.resetForNextRound(state, nextRoundNumber = 3)
        assertEquals(2, result.hostScore)
        assertEquals(1, result.guestScore)
    }

    @Test
    fun `resetForNextRound sets new round number`() {
        val result = GameEngine.resetForNextRound(baseState(), nextRoundNumber = 4)
        assertEquals(4, result.roundNumber)
    }

    @Test
    fun `resetForNextRound clears per-round fields`() {
        val state = baseState(hostFaults = 2, guestFaults = 1, questionCount = 8)
        val result = GameEngine.resetForNextRound(state, nextRoundNumber = 2)
        assertEquals(0, result.hostFaults)
        assertEquals(0, result.guestFaults)
        assertEquals(0, result.questionCount)
        assertNull(result.myCard)
        assertNull(result.opponentCard)
        assertEquals("", result.roundWinnerId)
    }

    @Test
    fun `resetForNextRound alternates first player - odd rounds host goes first`() {
        val result = GameEngine.resetForNextRound(baseState(), nextRoundNumber = 3) // odd
        assertEquals("host", result.activePlayerId)
    }

    @Test
    fun `resetForNextRound alternates first player - even rounds guest goes first`() {
        val result = GameEngine.resetForNextRound(baseState(), nextRoundNumber = 2) // even
        assertEquals("guest", result.activePlayerId)
    }

    // -----------------------------------------------------------------------
    // 7. Timer expiry
    // -----------------------------------------------------------------------

    @Test
    fun `applyTimerExpiry advances turn and clears timerSecondsLeft`() {
        val state = baseState(activePlayerId = "host", timerSecondsLeft = 5)
        val result = GameEngine.applyTimerExpiry(state)
        assertEquals("guest", result.activePlayerId)  // flipped
        assertEquals(0, result.timerSecondsLeft)
    }

    @Test
    fun `applyTimerExpiry does not increment fault count`() {
        val state = baseState(activePlayerId = "host", hostFaults = 1)
        val result = GameEngine.applyTimerExpiry(state)
        assertEquals(1, result.hostFaults)  // unchanged — timer expiry is not a fault
    }

    // -----------------------------------------------------------------------
    // 8. startRound
    // -----------------------------------------------------------------------

    @Test
    fun `startRound resets faults and question count`() {
        val state = baseState(hostFaults = 2, guestFaults = 3, questionCount = 10, hostScore = 1)
        val result = GameEngine.startRound(
            state, myCard = dummyPlayer("a"), opponentCard = dummyPlayer("b"),
            roundNumber = 2, firstPlayerId = "guest"
        )
        assertEquals(0, result.hostFaults)
        assertEquals(0, result.guestFaults)
        assertEquals(0, result.questionCount)
        assertEquals(GamePhase.QA, result.phase)
        assertEquals(1, result.hostScore) // preserved
    }

    @Test
    fun `startRound sets provided cards`() {
        val myCard = dummyPlayer("myId", "My Player")
        val opponentCard = dummyPlayer("oppId", "Opp Player")
        val result = GameEngine.startRound(
            baseState(), myCard = myCard, opponentCard = opponentCard,
            roundNumber = 1, firstPlayerId = "host"
        )
        assertEquals(myCard, result.myCard)
        assertEquals(opponentCard, result.opponentCard)
    }

    // -----------------------------------------------------------------------
    // 9. Computed state properties
    // -----------------------------------------------------------------------

    @Test
    fun `isMyTurn is true when activePlayerId matches myPlayerId`() {
        val hostState = baseState(activePlayerId = "host").copy(myRole = PlayerRole.Host)
        assertTrue(hostState.isMyTurn)
        assertFalse(hostState.isCardHolder)
    }

    @Test
    fun `isCardHolder is true when opponent is guessing`() {
        val hostState = baseState(activePlayerId = "guest").copy(myRole = PlayerRole.Host)
        assertTrue(hostState.isCardHolder)
        assertFalse(hostState.isMyTurn)
    }

    @Test
    fun `didIWinRound is true when my player ID matches roundWinnerId`() {
        val state = baseState(roundWinnerId = "host").copy(myRole = PlayerRole.Host)
        assertTrue(state.didIWinRound)
    }
}
